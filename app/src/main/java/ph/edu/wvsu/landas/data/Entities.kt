package ph.edu.wvsu.landas.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object Roles {
    const val STUDENT = "STUDENT"
    const val FACULTY = "FACULTY"
    const val ADMIN = "ADMIN"
    const val VISITOR = "VISITOR"
}

object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["schoolId"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val fullName: String,
    val role: String,
    val program: String = "",
    val yearLevel: String = "",
    val passwordHash: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val role: String,
    val section: String,
    val timestamp: Long,
    val evacuationArea: String,
    val method: String,
    val synced: Boolean
)

@Entity(tableName = "distress_signals")
data class DistressSignal(
    @PrimaryKey val id: String,
    val userId: String,
    val fullName: String,
    val latitude: Double,
    val longitude: Double,
    val mapX: Float,
    val mapY: Float,
    val nearestLocation: String,
    val message: String,
    val timestamp: Long,
    val status: String,
    val synced: Boolean
)

object DistressStatus {
    const val ACTIVE = "ACTIVE"
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
    const val RESOLVED = "RESOLVED"
}

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val body: String,
    val issuedAt: Long,
    val severity: String,
    val isActive: Boolean,
    val isRead: Boolean = false
)

object AlertSeverity {
    const val CRITICAL = "CRITICAL"
    const val WARNING = "WARNING"
    const val INFO = "INFO"
    const val CLEAR = "CLEAR"
}

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val category: String,
    val timestamp: Long,
    val pinned: Boolean = false
)

@Entity(tableName = "campus_nodes")
data class CampusNodeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val x: Float,
    val y: Float,
    val kind: String,
    val buildingCode: String = "",
    val isEvacuationArea: Boolean = false,
    val capacity: Int = 0,
    val occupancy: Int = 0,
    val hazardBlocked: Boolean = false,
    val description: String = "",
    val width: Float = 80f,
    val height: Float = 55f,
    val colorHex: String = "#2A9D8F",
    val isRoutable: Boolean = true
)

object NodeKind {
    const val BUILDING = "BUILDING"
    const val EVACUATION = "EVACUATION"
    const val LANDMARK = "LANDMARK"
    const val GATE = "GATE"
    const val FACILITY = "FACILITY"
    const val JUNCTION = "JUNCTION"
}

@Entity(tableName = "campus_edges")
data class CampusEdgeEntity(
    @PrimaryKey val id: String,
    val fromNodeId: String,
    val toNodeId: String,
    val distanceMeters: Float,
    val hazardBlocked: Boolean = false,
    val accessible: Boolean = true
)

@Entity(tableName = "classrooms")
data class ClassroomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val buildingCode: String,
    val buildingName: String,
    val floor: String,
    val capacity: Int,
    val equipment: String,
    val statusOverride: String? = null
)

@Entity(tableName = "schedule_slots")
data class ScheduleSlotEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val dayOfWeek: Int,
    val startMinute: Int,
    val endMinute: Int,
    val subject: String,
    val instructor: String,
    val section: String
)

data class StoredSession(
    val userId: String,
    val schoolId: String,
    val displayName: String,
    val role: String,
    val isVisitor: Boolean = false
)
