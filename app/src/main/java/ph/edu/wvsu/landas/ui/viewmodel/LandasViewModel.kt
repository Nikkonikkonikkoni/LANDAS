package ph.edu.wvsu.landas.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ph.edu.wvsu.landas.LandasApplication
import ph.edu.wvsu.landas.data.AuthOutcome
import ph.edu.wvsu.landas.data.CampusLocationPayload
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.DistressStatus
import ph.edu.wvsu.landas.data.MarkSafeOutcome
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.ScheduleSlotEntity
import ph.edu.wvsu.landas.data.StoredSession
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.util.CampusLocation
import ph.edu.wvsu.landas.util.BackupManager
import ph.edu.wvsu.landas.util.DrillScheduler

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null
)

data class MarkSafeUiState(
    val saving: Boolean = false,
    val outcome: MarkSafeOutcome? = null,
    val error: String? = null
)

data class SosUiState(
    val sending: Boolean = false,
    val sent: DistressSignal? = null,
    val error: String? = null
)

data class UiMessage(val text: String, val isError: Boolean = false)

class LandasViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as LandasApplication
    private val container = app.container
    private val repository = container.repository
    val locationHelper = container.locationHelper
    private val backupManager = BackupManager(application, repository)

    private val _bootstrapping = MutableStateFlow(true)
    val bootstrapping = _bootstrapping.asStateFlow()

    private val _bootstrapError = MutableStateFlow<String?>(null)
    val bootstrapError = _bootstrapError.asStateFlow()

    private val _session = MutableStateFlow<StoredSession?>(null)
    val session = _session.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()

    private val _markSafeState = MutableStateFlow(MarkSafeUiState())
    val markSafeState = _markSafeState.asStateFlow()

    private val _sosState = MutableStateFlow(SosUiState())
    val sosState = _sosState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow<Long?>(null)
    val lastSyncedAt = _lastSyncedAt.asStateFlow()

    private val _scheduledDrillAt = MutableStateFlow(DrillScheduler.scheduledAt(application))
    val scheduledDrillAt = _scheduledDrillAt.asStateFlow()

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 5)
    val messages = _messages.asSharedFlow()

    val isOnline: StateFlow<Boolean> = container.networkMonitor.isOnline.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        container.networkMonitor.isOnlineNow()
    )

    val alerts = repository.alerts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val activeAlert = repository.activeAlert.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val unreadAlertCount = repository.unreadAlertCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val announcements = repository.announcements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val campusNodes = repository.nodes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val campusEdges = repository.edges.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val classrooms = repository.classrooms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val schedules = repository.schedules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val users = repository.users.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val attendanceToday = repository.todayAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val allAttendance = repository.allAttendance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val distressSignals = repository.allDistress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val activeDistress = repository.activeDistress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val pendingSync = repository.pendingSyncCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val occupantsByRole = combine(attendanceToday, users) { attendance, allUsers ->
        val students = allUsers.count { it.role == Roles.STUDENT && it.isActive }
        val faculty = allUsers.count { it.role == Roles.FACULTY && it.isActive }
        val safeStudents = attendance.filter { it.role == Roles.STUDENT }.map { it.userId }.distinct().size
        val safeFaculty = attendance.filter { it.role == Roles.FACULTY }.map { it.userId }.distinct().size
        Triple(safeStudents, students, safeFaculty to faculty)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Triple(0, 0, 0 to 0))

    init {
        bootstrap()
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online && !_bootstrapping.value) {
                    syncPending(silent = true)
                }
            }
        }
    }

    private fun bootstrap() {
        viewModelScope.launch {
            try {
                repository.ensureSeeded()
                val stored = repository.session.first()
                if (stored != null) {
                    if (stored.isVisitor) {
                        _session.value = stored
                    } else {
                        val user = repository.findUser(stored.userId)
                        if (user != null && user.isActive) {
                            _session.value = stored
                            _currentUser.value = user
                        } else {
                            repository.signOut()
                        }
                    }
                }
                if (isOnline.value) syncPending(silent = true)
            } catch (error: Exception) {
                _bootstrapError.value = "LANDAS could not prepare its offline data: ${error.message}"
            } finally {
                _bootstrapping.value = false
            }
        }
    }

    fun login(schoolId: String, password: String, adminPortal: Boolean) {
        if (_loginState.value.loading) return
        viewModelScope.launch {
            _loginState.value = LoginUiState(loading = true)
            when (val outcome = repository.authenticate(schoolId, password, adminPortal)) {
                is AuthOutcome.Success -> {
                    _currentUser.value = outcome.user
                    _session.value = StoredSession(
                        userId = outcome.user.id,
                        schoolId = outcome.user.schoolId,
                        displayName = outcome.user.fullName,
                        role = outcome.user.role
                    )
                    _loginState.value = LoginUiState()
                }

                is AuthOutcome.Error -> {
                    _loginState.value = LoginUiState(error = outcome.message)
                }
            }
        }
    }

    fun continueAsVisitor() {
        viewModelScope.launch {
            repository.continueAsVisitor()
            _session.value = StoredSession(
                userId = "visitor",
                schoolId = "GUEST",
                displayName = "Campus Visitor",
                role = Roles.VISITOR,
                isVisitor = true
            )
            _currentUser.value = null
            _loginState.value = LoginUiState()
        }
    }

    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(error = null)
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _session.value = null
            _currentUser.value = null
            _markSafeState.value = MarkSafeUiState()
            _sosState.value = SosUiState()
        }
    }

    fun markSafe(area: CampusNodeEntity) {
        val user = _currentUser.value ?: return
        if (_markSafeState.value.saving) return
        viewModelScope.launch {
            _markSafeState.value = MarkSafeUiState(saving = true)
            try {
                val outcome = repository.markSafe(
                    user = user,
                    evacuationArea = area.name,
                    method = "ONE_TAP",
                    areaNodeId = area.id,
                    isOnline = isOnline.value
                )
                _markSafeState.value = MarkSafeUiState(outcome = outcome)
            } catch (error: Exception) {
                _markSafeState.value = MarkSafeUiState(error = error.message ?: "Unable to record attendance.")
            }
        }
    }

    fun clearMarkSafeState() {
        _markSafeState.value = MarkSafeUiState()
    }



    fun sendSos(location: CampusLocation, message: String) {
        val user = _currentUser.value ?: return
        if (_sosState.value.sending) return
        viewModelScope.launch {
            _sosState.value = SosUiState(sending = true)
            try {
                val node = nearestNode(location.mapX, location.mapY)
                val signal = repository.sendDistressSignal(
                    user = user,
                    location = CampusLocationPayload(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        mapX = location.mapX,
                        mapY = location.mapY,
                        nearestLocation = node?.name ?: "Campus grounds"
                    ),
                    message = message,
                    isOnline = isOnline.value
                )
                _sosState.value = SosUiState(sent = signal)
            } catch (error: Exception) {
                _sosState.value = SosUiState(error = error.message ?: "Unable to send the distress signal.")
            }
        }
    }

    fun resetSos() {
        _sosState.value = SosUiState()
    }

    fun currentUserSignal(): DistressSignal? {
        val user = _currentUser.value ?: return null
        return distressSignals.value.firstOrNull { it.userId == user.id && it.status != DistressStatus.RESOLVED }
    }

    fun resolveOwnDistressSignal(id: String) {
        viewModelScope.launch {
            repository.updateDistressStatus(id, DistressStatus.RESOLVED)
            _sosState.value = SosUiState()
            _messages.emit(UiMessage("Your distress signal has been cancelled."))
        }
    }

    fun nearestNode(x: Float, y: Float): CampusNodeEntity? {
        return campusNodes.value
            .filter { it.kind != NodeKind.JUNCTION }
            .minByOrNull { node ->
                val dx = node.x - x
                val dy = node.y - y
                dx * dx + dy * dy
            }
    }

    fun syncNow() {
        viewModelScope.launch { syncPending(silent = false) }
    }

    private suspend fun syncPending(silent: Boolean) {
        if (_isSyncing.value) return
        _isSyncing.value = true
        try {
            repository.syncPendingSignals()
            _lastSyncedAt.value = System.currentTimeMillis()
            if (!silent) {
                _messages.emit(UiMessage("Offline records synchronized successfully."))
            }
        } catch (error: Exception) {
            if (!silent) _messages.emit(UiMessage("Synchronization failed: ${error.message}", true))
        } finally {
            _isSyncing.value = false
        }
    }

    fun markAlertsRead() {
        viewModelScope.launch { repository.markAllAlertsRead() }
    }

    fun publishAlert(type: String, title: String, body: String, severity: String) {
        viewModelScope.launch {
            try {
                val alert = repository.publishAlert(type, title, body, severity, activate = true)
                container.notificationHelper.showAlert(alert)
                _messages.emit(UiMessage("Emergency alert published and broadcast."))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Could not publish alert: ${error.message}", true))
            }
        }
    }

    fun sendAllClear(title: String, body: String) {
        viewModelScope.launch {
            try {
                val alert = repository.sendAllClear(title, body)
                container.notificationHelper.showAlert(alert)
                _messages.emit(UiMessage("All-clear notice published."))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Could not send all-clear: ${error.message}", true))
            }
        }
    }

    fun scheduleDrill(triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis() + 5_000L) {
            emitMessage("Choose a drill time at least a few seconds in the future.", true)
            return
        }
        DrillScheduler.schedule(getApplication(), triggerAtMillis)
        _scheduledDrillAt.value = triggerAtMillis
        emitMessage("Campus drill scheduled. Keep notifications enabled to receive it.")
    }

    fun cancelScheduledDrill() {
        DrillScheduler.cancel(getApplication())
        _scheduledDrillAt.value = null
        emitMessage("Scheduled drill cancelled.")
    }

    fun addAnnouncement(title: String, body: String, category: String) {
        if (title.isBlank() || body.isBlank()) {
            emitMessage("Enter both an announcement title and message.", true)
            return
        }
        viewModelScope.launch {
            try {
                repository.addAnnouncement(title, body, category)
                _messages.emit(UiMessage("Announcement published."))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Could not publish announcement: ${error.message}", true))
            }
        }
    }

    fun resolveDistress(id: String, status: String = DistressStatus.RESOLVED) {
        viewModelScope.launch {
            repository.updateDistressStatus(id, status)
            _messages.emit(UiMessage("Distress signal status updated."))
        }
    }

    fun acknowledgeDistress(id: String) {
        viewModelScope.launch {
            repository.updateDistressStatus(id, DistressStatus.ACKNOWLEDGED)
            _messages.emit(UiMessage("Responder acknowledged the distress signal."))
        }
    }

    fun saveNode(
        originalId: String?,
        name: String,
        shortName: String,
        x: Float,
        y: Float,
        kind: String,
        buildingCode: String,
        isEvacuation: Boolean,
        capacity: Int,
        occupancy: Int,
        width: Float,
        height: Float,
        description: String
    ) {
        if (name.isBlank()) {
            emitMessage("Location name is required.", true)
            return
        }
        viewModelScope.launch {
            val node = CampusNodeEntity(
                id = originalId ?: "node-${UUID.randomUUID()}",
                name = name,
                shortName = shortName.ifBlank { name.take(10).uppercase() },
                x = x,
                y = y,
                kind = kind,
                buildingCode = buildingCode,
                isEvacuationArea = isEvacuation,
                capacity = capacity,
                occupancy = occupancy,
                width = width,
                height = height,
                colorHex = if (isEvacuation) "#2D6A4F" else "#0D5C63",
                description = description
            )
            repository.upsertNode(node)
            _messages.emit(UiMessage(if (originalId == null) "Route location added." else "Route location updated."))
        }
    }

    fun deleteNode(nodeId: String) {
        viewModelScope.launch {
            repository.deleteNode(nodeId)
            _messages.emit(UiMessage("Route location and connected paths removed."))
        }
    }

    fun toggleNodeHazard(nodeId: String, blocked: Boolean) {
        viewModelScope.launch { repository.setNodeHazard(nodeId, blocked) }
    }

    fun setAreaOccupancy(nodeId: String, occupancy: Int) {
        viewModelScope.launch { repository.setAreaOccupancy(nodeId, occupancy) }
    }

    fun addEdge(fromId: String, toId: String) {
        if (fromId == toId) {
            emitMessage("Choose two different locations.", true)
            return
        }
        viewModelScope.launch {
            repository.upsertEdge(fromId, toId)
            _messages.emit(UiMessage("Walking path added and routes recalculated."))
        }
    }

    fun deleteEdge(edgeId: String) {
        viewModelScope.launch {
            repository.deleteEdge(edgeId)
            _messages.emit(UiMessage("Walking path removed."))
        }
    }

    fun toggleEdgeHazard(edgeId: String, blocked: Boolean) {
        viewModelScope.launch { repository.setEdgeHazard(edgeId, blocked) }
    }

    fun addManualAttendance(user: UserEntity, area: CampusNodeEntity) {
        viewModelScope.launch {
            try {
                repository.addManualAttendance(user, area.name, area.id)
                _messages.emit(UiMessage("${user.fullName} added to the attendance record."))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Unable to add attendance: ${error.message}", true))
            }
        }
    }

    fun setClassroomOverride(room: ClassroomEntity, override: String?) {
        viewModelScope.launch {
            repository.setClassroomOverride(room.id, override)
            _messages.emit(UiMessage("${room.name} availability updated."))
        }
    }

    fun addScheduleSlot(room: ClassroomEntity, day: Int, start: Int, end: Int, subject: String, instructor: String, section: String) {
        if (subject.isBlank()) {
            emitMessage("Enter a subject title.", true)
            return
        }
        viewModelScope.launch {
            repository.addScheduleSlot(
                ScheduleSlotEntity(
                    id = "slot-${UUID.randomUUID()}",
                    roomId = room.id,
                    dayOfWeek = day,
                    startMinute = start,
                    endMinute = end,
                    subject = subject,
                    instructor = instructor,
                    section = section
                )
            )
            _messages.emit(UiMessage("Class schedule added for ${room.name}."))
        }
    }

    fun deleteScheduleSlot(slotId: String) {
        viewModelScope.launch {
            repository.deleteScheduleSlot(slotId)
            _messages.emit(UiMessage("Class schedule removed."))
        }
    }

    fun setUserActive(user: UserEntity, active: Boolean) {
        viewModelScope.launch {
            repository.setUserActive(user.id, active)
            _messages.emit(UiMessage(if (active) "${user.fullName} reactivated." else "${user.fullName} deactivated."))
        }
    }

    fun resetUserPassword(user: UserEntity) {
        viewModelScope.launch {
            repository.resetUserPassword(user.id)
            val default = if (user.role == Roles.ADMIN) "admin123" else "landas123"
            _messages.emit(UiMessage("Password reset to $default for ${user.fullName}."))
        }
    }

    fun clearTodayAttendance() {
        viewModelScope.launch {
            try {
                repository.clearTodayAttendance()
                _messages.emit(UiMessage("Today's attendance records were cleared."))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Unable to clear attendance: ${error.message}", true))
            }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                backupManager.exportTo(uri)
                _messages.emit(UiMessage("LANDAS backup exported successfully."))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Backup export failed: ${error.message}", true))
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            try {
                val summary = backupManager.importFrom(uri)
                _messages.emit(UiMessage("Backup restored: $summary"))
            } catch (error: Exception) {
                _messages.emit(UiMessage("Backup import failed: ${error.message}", true))
            }
        }
    }

    fun emitMessage(text: String, isError: Boolean = false) {
        viewModelScope.launch { _messages.emit(UiMessage(text, isError)) }
    }

    fun checkForExistingAttendance(): Boolean {
        val user = _currentUser.value ?: return false
        return attendanceToday.value.any { it.userId == user.id }
    }
}
