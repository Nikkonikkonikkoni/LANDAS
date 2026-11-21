package ph.edu.wvsu.landas.data

import java.security.MessageDigest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.hypot
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object PasswordHasher {
    fun hash(password: String): String {
        val salted = "LANDAS::WVSU-PC::$password"
        return MessageDigest.getInstance("SHA-256")
            .digest(salted.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}

class SeedData(private val db: LandasDatabase) {
    private val seedMutex = Mutex()

    suspend fun ensureSeeded() = seedMutex.withLock {
        val firstRun = db.userDao().count() == 0
        if (firstRun) {
            seedUsers()
            seedAlertsAndAnnouncements()
        }
        if (db.campusNodeDao().count() == 0) seedCampusNetwork()
        if (db.classroomDao().count() == 0) seedClassrooms()
        if (db.scheduleDao().count() == 0) seedSchedules()
        if (firstRun) seedActivity()
    }

    private suspend fun seedUsers() {
        val studentPass = PasswordHasher.hash("landas123")
        val adminPass = PasswordHasher.hash("admin123")
        val now = System.currentTimeMillis()
        val users = listOf(
            UserEntity(
                id = "u-001", schoolId = "2023-00142", fullName = "Ana Marie Delos Reyes",
                role = Roles.STUDENT, program = "Bachelor of Secondary Education – Mathematics",
                yearLevel = "3rd Year", passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "u-002", schoolId = "2022-00411", fullName = "Mark Angelo Rivera",
                role = Roles.STUDENT, program = "BS Information Technology", yearLevel = "4th Year",
                passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "u-003", schoolId = "2024-00228", fullName = "Kyla Mae Fernandez",
                role = Roles.STUDENT, program = "Bachelor of Secondary Education – Science",
                yearLevel = "2nd Year", passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "u-004", schoolId = "2021-00087", fullName = "John Paul Villanueva",
                role = Roles.STUDENT, program = "BS in Agriculture", yearLevel = "4th Year",
                passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "u-005", schoolId = "2023-00310", fullName = "Shane Nicole Bautista",
                role = Roles.STUDENT, program = "BS Business Administration", yearLevel = "3rd Year",
                passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "u-006", schoolId = "2024-00402", fullName = "Angelo Miguel Tan",
                role = Roles.STUDENT, program = "Bachelor of Secondary Education – English",
                yearLevel = "2nd Year", passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "u-007", schoolId = "2020-00155", fullName = "Bea Cassandra Lim",
                role = Roles.STUDENT, program = "BS Information Technology", yearLevel = "4th Year",
                passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "f-001", schoolId = "F-2019-008", fullName = "Prof. Jovelyn Santillan",
                role = Roles.FACULTY, program = "College of Education",
                yearLevel = "Faculty", passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "f-002", schoolId = "F-2015-021", fullName = "Ma. Cristina Lopez",
                role = Roles.FACULTY, program = "College of Arts and Sciences",
                yearLevel = "Faculty", passwordHash = studentPass, createdAt = now
            ),
            UserEntity(
                id = "a-001", schoolId = "CDRRMO-ADMIN", fullName = "Engr. Rafael M. Cordero",
                role = Roles.ADMIN, program = "CDRRMO Coordinator",
                yearLevel = "Administrator", passwordHash = adminPass, createdAt = now
            )
        )
        db.userDao().upsertAll(users)
    }

    private suspend fun seedCampusNetwork() {
        val e = "E9C46A"
        val nodes = listOf(
            CampusNodeEntity("n_gate", "Main Gate", "MAIN GATE", 500f, 720f, NodeKind.GATE, "GATE", width = 115f, height = 42f, colorHex = "#546E7A", description = "Primary campus entrance along the national road."),
            CampusNodeEntity("n_admin", "Administration Building", "ADMIN", 490f, 625f, NodeKind.BUILDING, "ADM", width = 150f, height = 70f, colorHex = "#D9A441", description = "Registrar, cashier, and administrative offices."),
            CampusNodeEntity("n_gabaldon", "Gabaldon Hall", "GABALDON", 330f, 570f, NodeKind.BUILDING, "GAB", width = 170f, height = 85f, colorHex = "#C08552", description = "Heritage academic building and designated assembly point A."),
            CampusNodeEntity("n_education", "College of Education", "COED", 700f, 570f, NodeKind.BUILDING, "COED", width = 160f, height = 80f, colorHex = "#457B9D", description = "Teacher education classrooms and faculty offices."),
            CampusNodeEntity("n_cas", "College of Arts and Sciences", "CAS", 790f, 430f, NodeKind.BUILDING, "CAS", width = 150f, height = 80f, colorHex = "#6A994E", description = "Science laboratories and general education classrooms."),
            CampusNodeEntity("n_business", "College of Business & Management", "CBM", 650f, 320f, NodeKind.BUILDING, "CBM", width = 140f, height = 75f, colorHex = "#E76F51", description = "Business and management academic programs."),
            CampusNodeEntity("n_ict", "ICT Center", "ICT", 490f, 425f, NodeKind.BUILDING, "ICT", width = 110f, height = 60f, colorHex = "#2A9D8F", description = "Computer laboratories and information technology services."),
            CampusNodeEntity("n_library", "University Library", "LIBRARY", 350f, 445f, NodeKind.BUILDING, "LIB", width = 120f, height = 65f, colorHex = "#BC6C25", description = "Library, reading rooms, and discussion spaces."),
            CampusNodeEntity("n_gym", "Gymnasium", "GYM", 200f, 400f, NodeKind.BUILDING, "GYM", width = 150f, height = 105f, colorHex = "#7B6D8D", description = "Indoor sports and large assembly venue."),
            CampusNodeEntity("n_health", "Health Services", "HEALTH", 320f, 300f, NodeKind.BUILDING, "HLTH", width = 100f, height = 60f, colorHex = "#EF476F", description = "Campus clinic and first-aid station."),
            CampusNodeEntity("n_canteen", "Campus Canteen", "CANTEEN", 595f, 280f, NodeKind.BUILDING, "CANT", width = 100f, height = 60f, colorHex = "#F4A261", description = "Food concession area and open dining space."),
            CampusNodeEntity("n_student", "Student Center", "STUDENT CTR", 480f, 220f, NodeKind.BUILDING, "SC", width = 110f, height = 60f, colorHex = "#8E7DBE", description = "Student organization offices and activity areas."),
            CampusNodeEntity("n_agri", "Agri-Fishery Building", "AGRI", 770f, 240f, NodeKind.BUILDING, "AGRI", width = 150f, height = 75f, colorHex = "#A7C957", description = "Agriculture and fishery laboratories."),
            CampusNodeEntity("n_court", "Covered Court", "COURT", 400f, 355f, NodeKind.BUILDING, "COURT", width = 105f, height = 55f, colorHex = "#8E7DBE", description = "Covered multi-purpose court."),
            CampusNodeEntity("n_parking", "Campus Parking", "PARKING", 630f, 675f, NodeKind.FACILITY, "PARK", width = 100f, height = 45f, colorHex = "#718096", description = "Visitor and employee parking area."),
            CampusNodeEntity("n_evac_a", "Evacuation Area A – Gabaldon", "EVAC A", 150f, 555f, NodeKind.EVACUATION, "EVA", isEvacuationArea = true, capacity = 300, occupancy = 112, width = 120f, height = 90f, colorHex = "#2D6A4F", description = "Open hardcourt beside Gabaldon Hall. Primary assembly area for the west side of campus."),
            CampusNodeEntity("n_evac_b", "Evacuation Area B – Oval Field", "EVAC B", 300f, 150f, NodeKind.EVACUATION, "EVB", isEvacuationArea = true, capacity = 700, occupancy = 286, width = 220f, height = 130f, colorHex = "#2D6A4F", description = "Main oval field. High-capacity open area for campus-wide evacuation."),
            CampusNodeEntity("n_j_gate", "Gate Walkway", "J1", 500f, 678f, NodeKind.JUNCTION, width = 0f, height = 0f, isRoutable = false),
            CampusNodeEntity("n_j_central", "Central Walkway", "J2", 500f, 540f, NodeKind.JUNCTION, width = 0f, height = 0f, isRoutable = false),
            CampusNodeEntity("n_j_west", "West Walkway", "J3", 260f, 500f, NodeKind.JUNCTION, width = 0f, height = 0f, isRoutable = false),
            CampusNodeEntity("n_j_oval", "Oval Walkway", "J4", 390f, 255f, NodeKind.JUNCTION, width = 0f, height = 0f, isRoutable = false)
        )
        db.campusNodeDao().upsertAll(nodes)

        val position = nodes.associateBy { it.id }
        fun distance(a: String, b: String): Float {
            val n1 = position.getValue(a)
            val n2 = position.getValue(b)
            return (hypot(n1.x - n2.x, n1.y - n2.y) * 0.35f).coerceAtLeast(4f)
        }
        val pairs = listOf(
            "n_gate" to "n_j_gate",
            "n_j_gate" to "n_parking",
            "n_j_gate" to "n_admin",
            "n_j_gate" to "n_j_central",
            "n_j_central" to "n_admin",
            "n_j_central" to "n_gabaldon",
            "n_j_central" to "n_education",
            "n_j_central" to "n_ict",
            "n_admin" to "n_gabaldon",
            "n_admin" to "n_education",
            "n_gabaldon" to "n_evac_a",
            "n_gabaldon" to "n_gym",
            "n_gabaldon" to "n_j_west",
            "n_j_west" to "n_gym",
            "n_j_west" to "n_library",
            "n_library" to "n_ict",
            "n_library" to "n_court",
            "n_gym" to "n_court",
            "n_court" to "n_health",
            "n_court" to "n_j_oval",
            "n_health" to "n_j_oval",
            "n_j_oval" to "n_evac_b",
            "n_j_oval" to "n_gym",
            "n_j_oval" to "n_student",
            "n_student" to "n_canteen",
            "n_ict" to "n_canteen",
            "n_canteen" to "n_business",
            "n_canteen" to "n_agri",
            "n_business" to "n_education",
            "n_business" to "n_agri",
            "n_business" to "n_cas",
            "n_education" to "n_cas",
            "n_education" to "n_parking",
            "n_agri" to "n_cas"
        )
        val edges = pairs.mapIndexed { index, pair ->
            CampusEdgeEntity(
                id = "edge-${index + 1}",
                fromNodeId = pair.first,
                toNodeId = pair.second,
                distanceMeters = distance(pair.first, pair.second)
            )
        }
        db.campusEdgeDao().upsertAll(edges)
    }

    private suspend fun seedClassrooms() {
        val rooms = listOf(
            ClassroomEntity("room-gab-101", "GAB-101", "GAB", "Gabaldon Hall", "Ground Floor", 45, "Projector, Smart TV, Air-conditioned"),
            ClassroomEntity("room-gab-201", "GAB-201", "GAB", "Gabaldon Hall", "2nd Floor", 45, "Projector, Whiteboard"),
            ClassroomEntity("room-adm-101", "ADM-101", "ADM", "Administration Building", "Ground Floor", 40, "Smart TV, Air-conditioned"),
            ClassroomEntity("room-coed-101", "COED-101", "COED", "College of Education", "Ground Floor", 50, "Projector, Sound System"),
            ClassroomEntity("room-coed-204", "COED-204", "COED", "College of Education", "2nd Floor", 45, "Projector, Air-conditioned"),
            ClassroomEntity("room-cas-101", "CAS-101", "CAS", "College of Arts and Sciences", "Ground Floor", 50, "Laboratory benches, Smart TV"),
            ClassroomEntity("room-cas-202", "CAS-202", "CAS", "College of Arts and Sciences", "2nd Floor", 45, "Projector, Whiteboard"),
            ClassroomEntity("room-cbm-101", "CBM-101", "CBM", "College of Business & Management", "Ground Floor", 50, "Projector, Air-conditioned"),
            ClassroomEntity("room-ict-101", "ICT-101", "ICT", "ICT Center", "Ground Floor", 40, "40 workstations, Fiber internet"),
            ClassroomEntity("room-ict-202", "ICT-202", "ICT", "ICT Center", "2nd Floor", 40, "40 workstations, Projector"),
            ClassroomEntity("room-lib-disc", "LIB-DISC", "LIB", "University Library", "Ground Floor", 20, "Discussion room, Whiteboard"),
            ClassroomEntity("room-gym-court", "GYM-COURT", "GYM", "Gymnasium", "Main Floor", 120, "Full court, Bleachers, Sound System"),
            ClassroomEntity("room-agri-101", "AGRI-101", "AGRI", "Agri-Fishery Building", "Ground Floor", 40, "Laboratory equipment, Projector")
        )
        db.classroomDao().upsertAll(rooms)
    }

    private suspend fun seedSchedules() {
        val rooms = db.classroomDao().getAll()
        val templates = listOf(
            450 to 540,
            570 to 660,
            660 to 750,
            780 to 870,
            900 to 990,
            1020 to 1110
        )
        val subjects = listOf(
            "Mathematics in the Modern World",
            "Purposive Communication",
            "Understanding the Self",
            "Assessment in Learning 2",
            "Science, Technology and Society",
            "Technopreneurship",
            "Data Structures and Algorithms",
            "Financial Management",
            "Environmental Science",
            "Philippine History",
            "Web Systems and Technologies",
            "Research Methods"
        )
        val instructors = listOf(
            "Prof. J. Santillan", "Ma. Cristina Lopez", "Engr. R. Cordero",
            "Prof. A. Garcia", "Dr. M. Torres", "Prof. L. Navarro"
        )
        val sections = listOf("BSEd 2-A", "BSIT 3-B", "BSBA 3-A", "BSEd 3-A", "BSAgri 4-A", "BSIT 2-A")
        val slots = mutableListOf<ScheduleSlotEntity>()
        rooms.forEachIndexed { roomIndex, room ->
            DayOfWeek.entries.take(5).forEach { day ->
                repeat(2) { offset ->
                    val templateIndex = (roomIndex + day.value + offset * 3) % templates.size
                    val (start, end) = templates[templateIndex]
                    val subjectIndex = (roomIndex * 2 + day.value + offset) % subjects.size
                    slots += ScheduleSlotEntity(
                        id = "slot-${room.id}-${day.value}-$offset",
                        roomId = room.id,
                        dayOfWeek = day.value,
                        startMinute = start,
                        endMinute = end,
                        subject = subjects[subjectIndex],
                        instructor = instructors[(roomIndex + offset) % instructors.size],
                        section = sections[(roomIndex + day.value + offset) % sections.size]
                    )
                }
            }
        }
        db.scheduleDao().upsertAll(slots)
    }

    private suspend fun seedAlertsAndAnnouncements() {
        val now = System.currentTimeMillis()
        val alerts = listOf(
            AlertEntity(
                id = "alert-drill-${LocalDate.now()}",
                type = "EARTHQUAKE",
                title = "Campus-wide Earthquake Drill in Progress",
                body = "The simulation is underway. Protect your head, take the nearest safe route, and proceed to Evacuation Area A or B. Mark yourself safe once you arrive.",
                issuedAt = now - TimeUnit.MINUTES.toMillis(12),
                severity = AlertSeverity.WARNING,
                isActive = true
            ),
            AlertEntity(
                id = "alert-advisory-${LocalDate.now()}",
                type = "ADVISORY",
                title = "New Covered Walkway Now Open",
                body = "The newly completed walkway between the Library and Covered Court is open. Route data has been updated in LANDAS.",
                issuedAt = now - TimeUnit.DAYS.toMillis(1),
                severity = AlertSeverity.INFO,
                isActive = false,
                isRead = true
            ),
            AlertEntity(
                id = "alert-fire-past",
                type = "FIRE",
                title = "Fire Alarm Drill Completed",
                body = "The fire drill at the Canteen has concluded. All personnel accounted for. Campus operations have resumed.",
                issuedAt = now - TimeUnit.DAYS.toMillis(3),
                severity = AlertSeverity.CLEAR,
                isActive = false,
                isRead = true
            ),
            AlertEntity(
                id = "alert-typhoon-past",
                type = "WEATHER",
                title = "Typhoon Advisory Lifted",
                body = "Classes and campus operations will resume tomorrow. Please monitor official university channels for updates.",
                issuedAt = now - TimeUnit.DAYS.toMillis(5),
                severity = AlertSeverity.CLEAR,
                isActive = false,
                isRead = true
            )
        )
        alerts.forEach { db.alertDao().insert(it) }

        val announcements = listOf(
            AnnouncementEntity(
                id = "ann-1",
                title = "2nd Semester Enrollment Schedule",
                body = "Continuing students may enroll from January 6–10 through the Office of the Registrar. Bring your validated ID and advising slip.",
                category = "ACADEMIC",
                timestamp = now - TimeUnit.HOURS.toMillis(5),
                pinned = true
            ),
            AnnouncementEntity(
                id = "ann-2",
                title = "LANDAS Offline Maps Ready",
                body = "Campus routes and evacuation areas can now be accessed without internet. Open the Map module and download the latest route state while online.",
                category = "SYSTEM",
                timestamp = now - TimeUnit.HOURS.toMillis(26)
            ),
            AnnouncementEntity(
                id = "ann-3",
                title = "First Aid Orientation",
                body = "The Campus Health Services will conduct a free first aid orientation at the Gymnasium on Friday, 2:00 PM.",
                category = "SAFETY",
                timestamp = now - TimeUnit.DAYS.toMillis(2)
            )
        )
        db.announcementDao().insertAll(announcements)
    }

    private suspend fun seedActivity() {
        val now = System.currentTimeMillis()
        val records = listOf(
            AttendanceRecord("att-demo-1", "u-002", "Mark Angelo Rivera", Roles.STUDENT, "BSIT 3-B", now - TimeUnit.MINUTES.toMillis(17), "Evacuation Area B – Oval Field", "ONE_TAP", true),
            AttendanceRecord("att-demo-2", "u-003", "Kyla Mae Fernandez", Roles.STUDENT, "BSEd 2-A", now - TimeUnit.MINUTES.toMillis(21), "Evacuation Area A – Gabaldon", "ONE_TAP", true),
            AttendanceRecord("att-demo-3", "u-004", "John Paul Villanueva", Roles.STUDENT, "BSAgri 4-A", now - TimeUnit.MINUTES.toMillis(25), "Evacuation Area B – Oval Field", "ONE_TAP", false),
            AttendanceRecord("att-demo-4", "u-005", "Shane Nicole Bautista", Roles.STUDENT, "BSBA 3-A", now - TimeUnit.MINUTES.toMillis(31), "Evacuation Area A – Gabaldon", "ONE_TAP", true),
            AttendanceRecord("att-demo-5", "u-006", "Angelo Miguel Tan", Roles.STUDENT, "BSEd 2-A", now - TimeUnit.MINUTES.toMillis(34), "Evacuation Area B – Oval Field", "CLASS_REP", true),
            AttendanceRecord("att-demo-6", "f-002", "Ma. Cristina Lopez", Roles.FACULTY, "Faculty", now - TimeUnit.MINUTES.toMillis(39), "Evacuation Area B – Oval Field", "ONE_TAP", true)
        )
        db.attendanceDao().insertAll(records)

        val distress = listOf(
            DistressSignal(
                id = "sos-demo-active",
                userId = "u-004",
                fullName = "John Paul Villanueva",
                latitude = 10.9503,
                longitude = 122.6324,
                mapX = 480f,
                mapY = 220f,
                nearestLocation = "Student Center",
                message = "Trapped near the Student Center. Exit is blocked by debris.",
                timestamp = now - TimeUnit.MINUTES.toMillis(6),
                status = DistressStatus.ACTIVE,
                synced = true
            ),
            DistressSignal(
                id = "sos-demo-past",
                userId = "u-005",
                fullName = "Shane Nicole Bautista",
                latitude = 10.9496,
                longitude = 122.6320,
                mapX = 200f,
                mapY = 400f,
                nearestLocation = "Gymnasium",
                message = "Minor injury at the Gymnasium entrance.",
                timestamp = now - TimeUnit.DAYS.toMillis(1),
                status = DistressStatus.RESOLVED,
                synced = true
            )
        )
        distress.forEach { db.distressDao().insert(it) }
    }

    fun todayBounds(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val end = start + TimeUnit.DAYS.toMillis(1) - 1
        return start to end
    }

    fun newId(prefix: String): String = "$prefix-${UUID.randomUUID()}"
}
