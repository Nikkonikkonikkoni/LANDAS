package ph.edu.wvsu.landas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE schoolId = :schoolId LIMIT 1")
    suspend fun findBySchoolId(schoolId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY role, fullName")
    fun observeAll(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role AND isActive = 1 ORDER BY fullName")
    fun observeByRole(role: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'STUDENT' AND isActive = 1 ORDER BY fullName")
    suspend fun getActiveStudents(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(users: List<UserEntity>)

    @Query("UPDATE users SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: String, active: Boolean)

    @Query("UPDATE users SET passwordHash = :hash WHERE id = :id")
    suspend fun setPasswordHash(id: String, hash: String)
}

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>)

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun observeBetween(start: Long, end: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun observeForUser(userId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE userId = :userId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC LIMIT 1")
    suspend fun latestForUserBetween(userId: String, start: Long, end: Long): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getBetween(start: Long, end: Long): List<AttendanceRecord>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE timestamp BETWEEN :start AND :end")
    suspend fun countBetween(start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE attendance_records SET synced = 1 WHERE synced = 0")
    suspend fun markAllSynced()

    @Query("DELETE FROM attendance_records WHERE timestamp BETWEEN :start AND :end")
    suspend fun deleteBetween(start: Long, end: Long)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()
}

@Dao
interface DistressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(signal: DistressSignal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signals: List<DistressSignal>)

    @Query("SELECT * FROM distress_signals ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<DistressSignal>>

    @Query("SELECT * FROM distress_signals WHERE status != 'RESOLVED' ORDER BY timestamp DESC")
    fun observeActive(): Flow<List<DistressSignal>>

    @Query("SELECT COUNT(*) FROM distress_signals WHERE status != 'RESOLVED'")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM distress_signals WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    @Query("UPDATE distress_signals SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE distress_signals SET synced = 1 WHERE synced = 0")
    suspend fun markAllSynced()

    @Query("SELECT * FROM distress_signals WHERE status != 'RESOLVED' LIMIT 1")
    suspend fun activeSignal(): DistressSignal?
}

@Dao
interface AlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertEntity)

    @Query("SELECT * FROM alerts ORDER BY issuedAt DESC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE isActive = 1 ORDER BY issuedAt DESC LIMIT 1")
    fun observeActive(): Flow<AlertEntity?>

    @Query("SELECT COUNT(*) FROM alerts WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE alerts SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllRead()

    @Query("UPDATE alerts SET isActive = 0")
    suspend fun deactivateAll()

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface AnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(announcement: AnnouncementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<AnnouncementEntity>)

    @Query("SELECT * FROM announcements ORDER BY pinned DESC, timestamp DESC")
    fun observeAll(): Flow<List<AnnouncementEntity>>
}

@Dao
interface CampusNodeDao {
    @Query("SELECT * FROM campus_nodes ORDER BY kind, name")
    fun observeAll(): Flow<List<CampusNodeEntity>>

    @Query("SELECT * FROM campus_nodes")
    suspend fun getAll(): List<CampusNodeEntity>

    @Query("SELECT * FROM campus_nodes WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CampusNodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: CampusNodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(nodes: List<CampusNodeEntity>)

    @Query("DELETE FROM campus_nodes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM campus_nodes")
    suspend fun deleteAll()

    @Query("UPDATE campus_nodes SET hazardBlocked = :blocked WHERE id = :id")
    suspend fun setHazardBlocked(id: String, blocked: Boolean)

    @Query("UPDATE campus_nodes SET occupancy = :occupancy WHERE id = :id")
    suspend fun setOccupancy(id: String, occupancy: Int)

    @Query("SELECT COUNT(*) FROM campus_nodes")
    suspend fun count(): Int
}

@Dao
interface CampusEdgeDao {
    @Query("SELECT * FROM campus_edges")
    fun observeAll(): Flow<List<CampusEdgeEntity>>

    @Query("SELECT * FROM campus_edges")
    suspend fun getAll(): List<CampusEdgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(edge: CampusEdgeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(edges: List<CampusEdgeEntity>)

    @Query("DELETE FROM campus_edges WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM campus_edges WHERE fromNodeId = :nodeId OR toNodeId = :nodeId")
    suspend fun deleteForNode(nodeId: String)

    @Query("DELETE FROM campus_edges")
    suspend fun deleteAll()

    @Query("UPDATE campus_edges SET hazardBlocked = :blocked WHERE id = :id")
    suspend fun setHazardBlocked(id: String, blocked: Boolean)

    @Query("SELECT COUNT(*) FROM campus_edges")
    suspend fun count(): Int
}

@Dao
interface ClassroomDao {
    @Query("SELECT * FROM classrooms ORDER BY buildingName, name")
    fun observeAll(): Flow<List<ClassroomEntity>>

    @Query("SELECT * FROM classrooms")
    suspend fun getAll(): List<ClassroomEntity>

    @Query("SELECT COUNT(*) FROM classrooms")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: ClassroomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rooms: List<ClassroomEntity>)

    @Query("UPDATE classrooms SET statusOverride = :override WHERE id = :id")
    suspend fun setStatusOverride(id: String, override: String?)

    @Query("DELETE FROM classrooms WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_slots ORDER BY dayOfWeek, startMinute")
    fun observeAll(): Flow<List<ScheduleSlotEntity>>

    @Query("SELECT * FROM schedule_slots")
    suspend fun getAll(): List<ScheduleSlotEntity>

    @Query("SELECT COUNT(*) FROM schedule_slots")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(slot: ScheduleSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(slots: List<ScheduleSlotEntity>)

    @Query("DELETE FROM schedule_slots WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM schedule_slots WHERE roomId = :roomId")
    suspend fun deleteForRoom(roomId: String)
}
