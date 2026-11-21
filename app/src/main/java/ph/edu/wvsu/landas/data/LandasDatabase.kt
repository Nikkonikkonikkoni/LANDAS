package ph.edu.wvsu.landas.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        AttendanceRecord::class,
        DistressSignal::class,
        AlertEntity::class,
        AnnouncementEntity::class,
        CampusNodeEntity::class,
        CampusEdgeEntity::class,
        ClassroomEntity::class,
        ScheduleSlotEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class LandasDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun distressDao(): DistressDao
    abstract fun alertDao(): AlertDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun campusNodeDao(): CampusNodeDao
    abstract fun campusEdgeDao(): CampusEdgeDao
    abstract fun classroomDao(): ClassroomDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var instance: LandasDatabase? = null

        fun getInstance(context: Context): LandasDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LandasDatabase::class.java,
                    "landas.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
