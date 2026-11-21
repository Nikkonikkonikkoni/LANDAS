package ph.edu.wvsu.landas.util

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AnnouncementEntity
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.LandasRepository
import ph.edu.wvsu.landas.data.ScheduleSlotEntity
import ph.edu.wvsu.landas.data.SnapshotData
import ph.edu.wvsu.landas.data.UserEntity

class BackupManager(
    private val context: Context,
    private val repository: LandasRepository
) {
    suspend fun exportTo(uri: Uri) {
        val snapshots = repository.getSnapshots()
        val root = JSONObject().apply {
            put("format", "LANDAS_BACKUP")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("campus", "West Visayas State University – Pototan Campus")
            put("users", snapshots.users.toJsonArray { userJson(it) })
            put("attendance", snapshots.attendance.toJsonArray { attendanceJson(it) })
            put("distress", snapshots.distress.toJsonArray { distressJson(it) })
            put("alerts", snapshots.alerts.toJsonArray { alertJson(it) })
            put("announcements", snapshots.announcements.toJsonArray { announcementJson(it) })
            put("nodes", snapshots.nodes.toJsonArray { nodeJson(it) })
            put("edges", snapshots.edges.toJsonArray { edgeJson(it) })
            put("classrooms", snapshots.classrooms.toJsonArray { roomJson(it) })
            put("schedules", snapshots.schedules.toJsonArray { scheduleJson(it) })
        }
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(root.toString(2).toByteArray(Charsets.UTF_8))
        } ?: error("Cannot open the selected destination.")
    }

    suspend fun importFrom(uri: Uri): String {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Cannot read the selected backup file.")
        val root = JSONObject(text)
        if (root.optString("format") != "LANDAS_BACKUP") {
            error("This file is not a LANDAS backup.")
        }
        val users = root.getJSONArray("users").mapObjects { json ->
            UserEntity(
                id = json.getString("id"),
                schoolId = json.getString("schoolId"),
                fullName = json.getString("fullName"),
                role = json.getString("role"),
                program = json.optString("program"),
                yearLevel = json.optString("yearLevel"),
                passwordHash = json.getString("passwordHash"),
                isActive = json.optBoolean("isActive", true),
                createdAt = json.optLong("createdAt")
            )
        }
        val attendance = root.getJSONArray("attendance").mapObjects { json ->
            AttendanceRecord(
                id = json.getString("id"),
                userId = json.getString("userId"),
                fullName = json.getString("fullName"),
                role = json.getString("role"),
                section = json.optString("section"),
                timestamp = json.optLong("timestamp"),
                evacuationArea = json.optString("evacuationArea"),
                method = json.optString("method"),
                synced = json.optBoolean("synced")
            )
        }
        val distress = root.getJSONArray("distress").mapObjects { json ->
            DistressSignal(
                id = json.getString("id"),
                userId = json.getString("userId"),
                fullName = json.getString("fullName"),
                latitude = json.optDouble("latitude"),
                longitude = json.optDouble("longitude"),
                mapX = json.optDouble("mapX").toFloat(),
                mapY = json.optDouble("mapY").toFloat(),
                nearestLocation = json.optString("nearestLocation"),
                message = json.optString("message"),
                timestamp = json.optLong("timestamp"),
                status = json.optString("status"),
                synced = json.optBoolean("synced")
            )
        }
        val alerts = root.getJSONArray("alerts").mapObjects { json ->
            AlertEntity(
                id = json.getString("id"),
                type = json.optString("type"),
                title = json.optString("title"),
                body = json.optString("body"),
                issuedAt = json.optLong("issuedAt"),
                severity = json.optString("severity"),
                isActive = json.optBoolean("isActive"),
                isRead = json.optBoolean("isRead")
            )
        }
        val announcements = root.getJSONArray("announcements").mapObjects { json ->
            AnnouncementEntity(
                id = json.getString("id"),
                title = json.optString("title"),
                body = json.optString("body"),
                category = json.optString("category"),
                timestamp = json.optLong("timestamp"),
                pinned = json.optBoolean("pinned")
            )
        }
        val nodes = root.getJSONArray("nodes").mapObjects { json ->
            CampusNodeEntity(
                id = json.getString("id"),
                name = json.optString("name"),
                shortName = json.optString("shortName"),
                x = json.optDouble("x").toFloat(),
                y = json.optDouble("y").toFloat(),
                kind = json.optString("kind"),
                buildingCode = json.optString("buildingCode"),
                isEvacuationArea = json.optBoolean("isEvacuationArea"),
                capacity = json.optInt("capacity"),
                occupancy = json.optInt("occupancy"),
                hazardBlocked = json.optBoolean("hazardBlocked"),
                description = json.optString("description"),
                width = json.optDouble("width", 80.0).toFloat(),
                height = json.optDouble("height", 55.0).toFloat(),
                colorHex = json.optString("colorHex", "#2A9D8F"),
                isRoutable = json.optBoolean("isRoutable", true)
            )
        }
        val edges = root.getJSONArray("edges").mapObjects { json ->
            CampusEdgeEntity(
                id = json.getString("id"),
                fromNodeId = json.getString("fromNodeId"),
                toNodeId = json.getString("toNodeId"),
                distanceMeters = json.optDouble("distanceMeters").toFloat(),
                hazardBlocked = json.optBoolean("hazardBlocked"),
                accessible = json.optBoolean("accessible", true)
            )
        }
        val classrooms = root.getJSONArray("classrooms").mapObjects { json ->
            ClassroomEntity(
                id = json.getString("id"),
                name = json.optString("name"),
                buildingCode = json.optString("buildingCode"),
                buildingName = json.optString("buildingName"),
                floor = json.optString("floor"),
                capacity = json.optInt("capacity"),
                equipment = json.optString("equipment"),
                statusOverride = if (json.isNull("statusOverride")) null else json.getString("statusOverride")
            )
        }
        val schedules = root.getJSONArray("schedules").mapObjects { json ->
            ScheduleSlotEntity(
                id = json.getString("id"),
                roomId = json.getString("roomId"),
                dayOfWeek = json.optInt("dayOfWeek"),
                startMinute = json.optInt("startMinute"),
                endMinute = json.optInt("endMinute"),
                subject = json.optString("subject"),
                instructor = json.optString("instructor"),
                section = json.optString("section")
            )
        }
        val snapshot = SnapshotData(
            users = users,
            attendance = attendance,
            distress = distress,
            alerts = alerts,
            announcements = announcements,
            nodes = nodes,
            edges = edges,
            classrooms = classrooms,
            schedules = schedules
        )
        repository.restoreSnapshot(snapshot)
        return "${users.size} users, ${nodes.size} route nodes, ${edges.size} paths, ${classrooms.size} rooms restored."
    }

    private fun userJson(user: UserEntity) = JSONObject().apply {
        put("id", user.id)
        put("schoolId", user.schoolId)
        put("fullName", user.fullName)
        put("role", user.role)
        put("program", user.program)
        put("yearLevel", user.yearLevel)
        put("passwordHash", user.passwordHash)
        put("isActive", user.isActive)
        put("createdAt", user.createdAt)
    }

    private fun attendanceJson(record: AttendanceRecord) = JSONObject().apply {
        put("id", record.id)
        put("userId", record.userId)
        put("fullName", record.fullName)
        put("role", record.role)
        put("section", record.section)
        put("timestamp", record.timestamp)
        put("evacuationArea", record.evacuationArea)
        put("method", record.method)
        put("synced", record.synced)
    }

    private fun distressJson(signal: DistressSignal) = JSONObject().apply {
        put("id", signal.id)
        put("userId", signal.userId)
        put("fullName", signal.fullName)
        put("latitude", signal.latitude)
        put("longitude", signal.longitude)
        put("mapX", signal.mapX)
        put("mapY", signal.mapY)
        put("nearestLocation", signal.nearestLocation)
        put("message", signal.message)
        put("timestamp", signal.timestamp)
        put("status", signal.status)
        put("synced", signal.synced)
    }

    private fun alertJson(alert: AlertEntity) = JSONObject().apply {
        put("id", alert.id)
        put("type", alert.type)
        put("title", alert.title)
        put("body", alert.body)
        put("issuedAt", alert.issuedAt)
        put("severity", alert.severity)
        put("isActive", alert.isActive)
        put("isRead", alert.isRead)
    }

    private fun announcementJson(announcement: AnnouncementEntity) = JSONObject().apply {
        put("id", announcement.id)
        put("title", announcement.title)
        put("body", announcement.body)
        put("category", announcement.category)
        put("timestamp", announcement.timestamp)
        put("pinned", announcement.pinned)
    }

    private fun nodeJson(node: CampusNodeEntity) = JSONObject().apply {
        put("id", node.id)
        put("name", node.name)
        put("shortName", node.shortName)
        put("x", node.x)
        put("y", node.y)
        put("kind", node.kind)
        put("buildingCode", node.buildingCode)
        put("isEvacuationArea", node.isEvacuationArea)
        put("capacity", node.capacity)
        put("occupancy", node.occupancy)
        put("hazardBlocked", node.hazardBlocked)
        put("description", node.description)
        put("width", node.width)
        put("height", node.height)
        put("colorHex", node.colorHex)
        put("isRoutable", node.isRoutable)
    }

    private fun edgeJson(edge: CampusEdgeEntity) = JSONObject().apply {
        put("id", edge.id)
        put("fromNodeId", edge.fromNodeId)
        put("toNodeId", edge.toNodeId)
        put("distanceMeters", edge.distanceMeters)
        put("hazardBlocked", edge.hazardBlocked)
        put("accessible", edge.accessible)
    }

    private fun roomJson(room: ClassroomEntity) = JSONObject().apply {
        put("id", room.id)
        put("name", room.name)
        put("buildingCode", room.buildingCode)
        put("buildingName", room.buildingName)
        put("floor", room.floor)
        put("capacity", room.capacity)
        put("equipment", room.equipment)
        if (room.statusOverride == null) put("statusOverride", JSONObject.NULL) else put("statusOverride", room.statusOverride)
    }

    private fun scheduleJson(slot: ScheduleSlotEntity) = JSONObject().apply {
        put("id", slot.id)
        put("roomId", slot.roomId)
        put("dayOfWeek", slot.dayOfWeek)
        put("startMinute", slot.startMinute)
        put("endMinute", slot.endMinute)
        put("subject", slot.subject)
        put("instructor", slot.instructor)
        put("section", slot.section)
    }

    private fun <T> List<T>.toJsonArray(mapper: (T) -> JSONObject): JSONArray {
        val array = JSONArray()
        forEach { array.put(mapper(it)) }
        return array
    }

    private fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> {
        val result = mutableListOf<T>()
        for (index in 0 until length()) result += mapper(getJSONObject(index))
        return result
    }
}
