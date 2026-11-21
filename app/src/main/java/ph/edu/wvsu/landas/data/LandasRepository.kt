package ph.edu.wvsu.landas.data

import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

sealed interface AuthOutcome {
    data class Success(val user: UserEntity) : AuthOutcome
    data class Error(val message: String) : AuthOutcome
}

data class MarkSafeOutcome(
    val record: AttendanceRecord,
    val alreadyMarked: Boolean,
    val queuedOffline: Boolean
)

class LandasRepository(
    private val db: LandasDatabase,
    val sessionStore: SessionStore
) {
    private val seedData = SeedData(db)

    val users: Flow<List<UserEntity>> = db.userDao().observeAll()
    val alerts: Flow<List<AlertEntity>> = db.alertDao().observeAll()
    val activeAlert: Flow<AlertEntity?> = db.alertDao().observeActive()
    val unreadAlertCount: Flow<Int> = db.alertDao().observeUnreadCount()
    val announcements: Flow<List<AnnouncementEntity>> = db.announcementDao().observeAll()
    val nodes: Flow<List<CampusNodeEntity>> = db.campusNodeDao().observeAll()
    val edges: Flow<List<CampusEdgeEntity>> = db.campusEdgeDao().observeAll()
    val classrooms: Flow<List<ClassroomEntity>> = db.classroomDao().observeAll()
    val schedules: Flow<List<ScheduleSlotEntity>> = db.scheduleDao().observeAll()
    val allAttendance: Flow<List<AttendanceRecord>> = db.attendanceDao().observeAll()
    val todayAttendance: Flow<List<AttendanceRecord>> = db.attendanceDao()
        .observeBetween(todayStart(), todayEnd())
    val allDistress: Flow<List<DistressSignal>> = db.distressDao().observeAll()
    val activeDistress: Flow<List<DistressSignal>> = db.distressDao().observeActive()
    val pendingSyncCount: Flow<Int> = combine(
        db.attendanceDao().observePendingCount(),
        db.distressDao().observePendingCount()
    ) { attendance, distress -> attendance + distress }
    val session: Flow<StoredSession?> = sessionStore.session

    suspend fun ensureSeeded() = seedData.ensureSeeded()

    suspend fun findUser(userId: String): UserEntity? = db.userDao().findById(userId)

    suspend fun authenticate(schoolId: String, password: String, adminPortal: Boolean): AuthOutcome {
        val normalized = schoolId.trim()
        if (normalized.isBlank() || password.isBlank()) {
            return AuthOutcome.Error("Enter your WVSU-PC ID and password.")
        }
        val user = db.userDao().findBySchoolId(normalized)
            ?: return AuthOutcome.Error("No campus record matches that WVSU-PC ID.")
        if (!user.isActive) {
            return AuthOutcome.Error("This account has been deactivated. Please contact the administrator.")
        }
        if (user.passwordHash != PasswordHasher.hash(password)) {
            return AuthOutcome.Error("Incorrect password. Your cached credentials are still available offline.")
        }
        if (adminPortal && user.role != Roles.ADMIN) {
            return AuthOutcome.Error("These credentials do not have administrator access.")
        }
        if (!adminPortal && user.role == Roles.ADMIN) {
            return AuthOutcome.Error("Administrators must sign in through the Admin portal.")
        }
        sessionStore.save(user)
        return AuthOutcome.Success(user)
    }

    suspend fun continueAsVisitor() = sessionStore.saveVisitor()

    suspend fun signOut() = sessionStore.clear()

    fun todayStart(): Long {
        val zone = ZoneId.systemDefault()
        return LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun todayEnd(): Long = todayStart() + TimeUnit.DAYS.toMillis(1) - 1

    suspend fun latestAttendanceForToday(userId: String): AttendanceRecord? {
        return db.attendanceDao().latestForUserBetween(userId, todayStart(), todayEnd())
    }

    suspend fun markSafe(
        user: UserEntity,
        evacuationArea: String,
        method: String,
        areaNodeId: String?,
        isOnline: Boolean
    ): MarkSafeOutcome {
        val now = System.currentTimeMillis()
        val existing = db.attendanceDao().latestForUserBetween(user.id, todayStart(), todayEnd())
        if (existing != null) {
            return MarkSafeOutcome(existing, alreadyMarked = true, queuedOffline = !existing.synced)
        }
        val record = AttendanceRecord(
            id = "att-${UUID.randomUUID()}",
            userId = user.id,
            fullName = user.fullName,
            role = user.role,
            section = when (user.role) {
                Roles.FACULTY -> "Faculty"
                Roles.ADMIN -> "Administration"
                else -> user.program.substringAfterLast("–").trim().ifBlank { "Student" }
            },
            timestamp = now,
            evacuationArea = evacuationArea,
            method = method,
            synced = isOnline
        )
        db.attendanceDao().insert(record)
        if (areaNodeId != null) {
            val area = db.campusNodeDao().findById(areaNodeId)
            if (area != null) {
                db.campusNodeDao().setOccupancy(area.id, (area.occupancy + 1).coerceAtMost(area.capacity))
            }
        }
        return MarkSafeOutcome(record, alreadyMarked = false, queuedOffline = !isOnline)
    }

    suspend fun addManualAttendance(
        user: UserEntity,
        evacuationArea: String,
        areaNodeId: String?
    ): AttendanceRecord {
        val record = AttendanceRecord(
            id = "att-${UUID.randomUUID()}",
            userId = user.id,
            fullName = user.fullName,
            role = user.role,
            section = if (user.role == Roles.FACULTY) "Faculty" else user.program.substringAfterLast("–").trim(),
            timestamp = System.currentTimeMillis(),
            evacuationArea = evacuationArea,
            method = "MANUAL",
            synced = true
        )
        db.attendanceDao().insert(record)
        if (areaNodeId != null) {
            val area = db.campusNodeDao().findById(areaNodeId)
            if (area != null) {
                db.campusNodeDao().setOccupancy(area.id, (area.occupancy + 1).coerceAtMost(area.capacity))
            }
        }
        return record
    }

    suspend fun clearTodayAttendance() {
        val todayStart = todayStart()
        val todayEnd = todayEnd()
        val today = db.attendanceDao().getBetween(todayStart, todayEnd)
        if (today.isEmpty()) return
        val clearedByArea = today.groupingBy { it.evacuationArea }.eachCount()
        val areas = db.campusNodeDao().getAll()
        db.attendanceDao().deleteBetween(todayStart, todayEnd)
        clearedByArea.forEach { (areaName, count) ->
            val area = areas.firstOrNull { it.isEvacuationArea && it.name == areaName } ?: return@forEach
            db.campusNodeDao().setOccupancy(area.id, (area.occupancy - count).coerceAtLeast(0))
        }
    }

    suspend fun sendDistressSignal(
        user: UserEntity,
        location: CampusLocationPayload,
        message: String,
        isOnline: Boolean
    ): DistressSignal {
        val signal = DistressSignal(
            id = "sos-${UUID.randomUUID()}",
            userId = user.id,
            fullName = user.fullName,
            latitude = location.latitude,
            longitude = location.longitude,
            mapX = location.mapX,
            mapY = location.mapY,
            nearestLocation = location.nearestLocation,
            message = message.ifBlank { "Emergency assistance requested." },
            timestamp = System.currentTimeMillis(),
            status = DistressStatus.ACTIVE,
            synced = isOnline
        )
        db.distressDao().insert(signal)
        return signal
    }

    suspend fun updateDistressStatus(id: String, status: String) {
        db.distressDao().updateStatus(id, status)
    }

    suspend fun syncPendingSignals() {
        db.attendanceDao().markAllSynced()
        db.distressDao().markAllSynced()
    }

    suspend fun markAllAlertsRead() = db.alertDao().markAllRead()

    suspend fun publishAlert(
        type: String,
        title: String,
        body: String,
        severity: String,
        activate: Boolean
    ): AlertEntity {
        if (activate) db.alertDao().deactivateAll()
        val alert = AlertEntity(
            id = "alert-${UUID.randomUUID()}",
            type = type,
            title = title,
            body = body,
            issuedAt = System.currentTimeMillis(),
            severity = severity,
            isActive = activate
        )
        db.alertDao().insert(alert)
        return alert
    }

    suspend fun sendAllClear(title: String, body: String): AlertEntity {
        db.alertDao().deactivateAll()
        val alert = AlertEntity(
            id = "alert-${UUID.randomUUID()}",
            type = "ALL_CLEAR",
            title = title,
            body = body,
            issuedAt = System.currentTimeMillis(),
            severity = AlertSeverity.CLEAR,
            isActive = false
        )
        db.alertDao().insert(alert)
        return alert
    }

    suspend fun addAnnouncement(title: String, body: String, category: String) {
        db.announcementDao().insert(
            AnnouncementEntity(
                id = "ann-${UUID.randomUUID()}",
                title = title,
                body = body,
                category = category,
                timestamp = System.currentTimeMillis(),
                pinned = false
            )
        )
    }

    suspend fun upsertNode(node: CampusNodeEntity) = db.campusNodeDao().upsert(node)

    suspend fun deleteNode(nodeId: String) {
        db.campusEdgeDao().deleteForNode(nodeId)
        db.campusNodeDao().delete(nodeId)
    }

    suspend fun setNodeHazard(nodeId: String, blocked: Boolean) =
        db.campusNodeDao().setHazardBlocked(nodeId, blocked)

    suspend fun setAreaOccupancy(nodeId: String, occupancy: Int) {
        val node = db.campusNodeDao().findById(nodeId) ?: return
        db.campusNodeDao().setOccupancy(nodeId, occupancy.coerceIn(0, node.capacity.coerceAtLeast(0)))
    }

    suspend fun upsertEdge(fromId: String, toId: String, blocked: Boolean = false, accessible: Boolean = true) {
        val from = db.campusNodeDao().findById(fromId) ?: return
        val to = db.campusNodeDao().findById(toId) ?: return
        val distance = (kotlin.math.hypot(from.x - to.x, from.y - to.y) * 0.35f).coerceAtLeast(4f)
        val edge = CampusEdgeEntity(
            id = "edge-${UUID.randomUUID()}",
            fromNodeId = fromId,
            toNodeId = toId,
            distanceMeters = distance,
            hazardBlocked = blocked,
            accessible = accessible
        )
        db.campusEdgeDao().upsert(edge)
    }

    suspend fun deleteEdge(edgeId: String) = db.campusEdgeDao().delete(edgeId)

    suspend fun setEdgeHazard(edgeId: String, blocked: Boolean) =
        db.campusEdgeDao().setHazardBlocked(edgeId, blocked)

    suspend fun setClassroomOverride(roomId: String, override: String?) =
        db.classroomDao().setStatusOverride(roomId, override)

    suspend fun addScheduleSlot(slot: ScheduleSlotEntity) = db.scheduleDao().upsert(slot)

    suspend fun deleteScheduleSlot(slotId: String) = db.scheduleDao().delete(slotId)

    suspend fun setUserActive(userId: String, active: Boolean) {
        db.userDao().setActive(userId, active)
    }

    suspend fun resetUserPassword(userId: String) {
        val user = db.userDao().findById(userId) ?: return
        val defaultPassword = if (user.role == Roles.ADMIN) "admin123" else "landas123"
        db.userDao().setPasswordHash(userId, PasswordHasher.hash(defaultPassword))
    }

    suspend fun getSnapshots() = SnapshotData(
        users = db.userDao().observeAll().first(),
        attendance = db.attendanceDao().observeAll().first(),
        distress = db.distressDao().observeAll().first(),
        alerts = db.alertDao().observeAll().first(),
        announcements = db.announcementDao().observeAll().first(),
        nodes = db.campusNodeDao().getAll(),
        edges = db.campusEdgeDao().getAll(),
        classrooms = db.classroomDao().getAll(),
        schedules = db.scheduleDao().getAll()
    )

    suspend fun restoreSnapshot(snapshot: SnapshotData) {
        db.userDao().upsertAll(snapshot.users)
        db.attendanceDao().insertAll(snapshot.attendance)
        db.distressDao().insertAll(snapshot.distress)
        snapshot.alerts.forEach { db.alertDao().insert(it) }
        db.announcementDao().insertAll(snapshot.announcements)
        db.campusNodeDao().upsertAll(snapshot.nodes)
        db.campusEdgeDao().upsertAll(snapshot.edges)
        db.classroomDao().upsertAll(snapshot.classrooms)
        db.scheduleDao().upsertAll(snapshot.schedules)
    }
}

data class CampusLocationPayload(
    val latitude: Double,
    val longitude: Double,
    val mapX: Float,
    val mapY: Float,
    val nearestLocation: String
)

data class SnapshotData(
    val users: List<UserEntity>,
    val attendance: List<AttendanceRecord>,
    val distress: List<DistressSignal>,
    val alerts: List<AlertEntity>,
    val announcements: List<AnnouncementEntity>,
    val nodes: List<CampusNodeEntity>,
    val edges: List<CampusEdgeEntity>,
    val classrooms: List<ClassroomEntity>,
    val schedules: List<ScheduleSlotEntity>
)
