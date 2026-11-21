package ph.edu.wvsu.landas.util

import java.time.LocalDateTime
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.ScheduleSlotEntity

enum class RoomAvailability(val label: String) {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    TRANSITION("Transition")
}

data class RoomStatusInfo(
    val availability: RoomAvailability,
    val currentSlot: ScheduleSlotEntity?,
    val nextSlot: ScheduleSlotEntity?,
    val detail: String,
    val isOverride: Boolean = false
)

object ClassroomAvailability {
    fun evaluate(
        room: ClassroomEntity,
        todaysSlots: List<ScheduleSlotEntity>,
        now: LocalDateTime = LocalDateTime.now()
    ): RoomStatusInfo {
        val override = room.statusOverride
        if (override != null) {
            val availability = when (override) {
                "OCCUPIED" -> RoomAvailability.OCCUPIED
                "TRANSITION" -> RoomAvailability.TRANSITION
                else -> RoomAvailability.AVAILABLE
            }
            return RoomStatusInfo(
                availability = availability,
                currentSlot = null,
                nextSlot = todaysSlots.firstOrNull { it.startMinute > now.hour * 60 + now.minute },
                detail = "Status set by the administrator.",
                isOverride = true
            )
        }

        val minute = now.hour * 60 + now.minute
        val sorted = todaysSlots.sortedBy { it.startMinute }
        val current = sorted.firstOrNull { minute >= it.startMinute && minute < it.endMinute }
        if (current != null) {
            return RoomStatusInfo(
                availability = RoomAvailability.OCCUPIED,
                currentSlot = current,
                nextSlot = sorted.firstOrNull { it.startMinute >= current.endMinute },
                detail = "In use until ${formatMinuteOfDay(current.endMinute)} · ${current.subject}"
            )
        }
        val upcoming = sorted.firstOrNull { it.startMinute > minute }
        val justEnded = sorted.lastOrNull { it.endMinute <= minute }
        if (justEnded != null && minute - justEnded.endMinute <= 15) {
            return RoomStatusInfo(
                availability = RoomAvailability.TRANSITION,
                currentSlot = justEnded,
                nextSlot = upcoming,
                detail = "Class just ended · next transition in ${15 - (minute - justEnded.endMinute)} min"
            )
        }
        if (upcoming != null && upcoming.startMinute - minute <= 15) {
            return RoomStatusInfo(
                availability = RoomAvailability.TRANSITION,
                currentSlot = null,
                nextSlot = upcoming,
                detail = "Next class starts at ${formatMinuteOfDay(upcoming.startMinute)}"
            )
        }
        return RoomStatusInfo(
            availability = RoomAvailability.AVAILABLE,
            currentSlot = null,
            nextSlot = upcoming,
            detail = if (upcoming != null) {
                "Free until ${formatMinuteOfDay(upcoming.startMinute)}"
            } else {
                "No more classes scheduled today"
            }
        )
    }
}
