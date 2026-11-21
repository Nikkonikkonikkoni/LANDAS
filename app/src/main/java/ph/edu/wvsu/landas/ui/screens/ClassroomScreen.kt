package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TransferWithinAStation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.ScheduleSlotEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.EmptyState
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.OnlineStatusPill
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.DangerSoft
import ph.edu.wvsu.landas.ui.theme.InfoBlue
import ph.edu.wvsu.landas.ui.theme.InfoSoft
import ph.edu.wvsu.landas.ui.theme.MutedInk
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.util.ClassroomAvailability
import ph.edu.wvsu.landas.util.RoomAvailability
import ph.edu.wvsu.landas.util.dayLabel
import ph.edu.wvsu.landas.util.formatMinuteShort
import java.time.DayOfWeek

private enum class RoomFilter(val label: String) {
    ALL("All rooms"),
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    TRANSITION("Transition")
}

private data class RoomWithStatus(
    val room: ClassroomEntity,
    val slots: List<ScheduleSlotEntity>,
    val status: ph.edu.wvsu.landas.util.RoomStatusInfo
)

@Composable
fun ClassroomScreen(
    classrooms: List<ClassroomEntity>,
    schedules: List<ScheduleSlotEntity>,
    isOnline: Boolean,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(RoomFilter.ALL) }
    var expandedRoomId by remember { mutableStateOf<String?>(null) }
    val now = LocalDateTime.now()

    val evaluated = remember(classrooms, schedules) {
        classrooms.map { room ->
            val roomSlots = schedules.filter { it.roomId == room.id }
            RoomWithStatus(
                room = room,
                slots = roomSlots,
                status = ClassroomAvailability.evaluate(
                    room = room,
                    todaysSlots = roomSlots.filter { it.dayOfWeek == now.dayOfWeek.value },
                    now = now
                )
            )
        }
    }

    val filtered = evaluated.filter { entry ->
        val matchesQuery = query.isBlank() ||
            entry.room.name.contains(query, ignoreCase = true) ||
            entry.room.buildingName.contains(query, ignoreCase = true) ||
            entry.room.buildingCode.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            RoomFilter.ALL -> true
            RoomFilter.AVAILABLE -> entry.status.availability == RoomAvailability.AVAILABLE
            RoomFilter.OCCUPIED -> entry.status.availability == RoomAvailability.OCCUPIED
            RoomFilter.TRANSITION -> entry.status.availability == RoomAvailability.TRANSITION
        }
        matchesQuery && matchesFilter
    }

    val availableNow = evaluated.count { it.status.availability == RoomAvailability.AVAILABLE }
    val occupiedNow = evaluated.count { it.status.availability == RoomAvailability.OCCUPIED }

    LandasScaffold(
        title = "Classroom availability",
        subtitle = "Live room status · offline class schedules",
        onBack = onBack,
        actions = { OnlineStatusPill(isOnline = isOnline) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    ClassStatTile(
                        modifier = Modifier.weight(1f),
                        value = "$availableNow",
                        label = "Available now",
                        color = Success,
                        background = SuccessSoft,
                        icon = Icons.Rounded.CheckCircle
                    )
                    ClassStatTile(
                        modifier = Modifier.weight(1f),
                        value = "$occupiedNow",
                        label = "In use",
                        color = Danger,
                        background = DangerSoft,
                        icon = Icons.Rounded.DoNotDisturbOn
                    )
                    ClassStatTile(
                        modifier = Modifier.weight(1f),
                        value = "${evaluated.size}",
                        label = "Total rooms",
                        color = InfoBlue,
                        background = InfoSoft,
                        icon = Icons.Rounded.MeetingRoom
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search room, building, or code") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    RoomFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = {
                                Text(
                                    option.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = MutedInk, modifier = Modifier.size(14.dp))
                    Text(
                        "As of ${String.format("%02d:%02d", now.hour, now.minute)} · ${dayLabel(now.dayOfWeek)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${filtered.size} room${if (filtered.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Search,
                        title = "No rooms match your filter",
                        message = "Try a different room name, building code, or availability filter."
                    )
                }
            }

            items(filtered, key = { it.room.id }) { entry ->
                RoomCard(
                    entry = entry,
                    expanded = expandedRoomId == entry.room.id,
                    onToggle = {
                        expandedRoomId = if (expandedRoomId == entry.room.id) null else entry.room.id
                    }
                )
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Class, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                        Text(
                            "Room status is computed from cached schedules and administrator overrides. Transition windows flag the 15 minutes before and after a class.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassStatTile(
    value: String,
    label: String,
    color: Color,
    background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = background,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun RoomCard(entry: RoomWithStatus, expanded: Boolean, onToggle: () -> Unit) {
    val (statusColor, statusBackground, statusIcon) = when (entry.status.availability) {
        RoomAvailability.AVAILABLE -> Triple(Success, SuccessSoft, Icons.Rounded.CheckCircle)
        RoomAvailability.OCCUPIED -> Triple(Danger, DangerSoft, Icons.Rounded.DoNotDisturbOn)
        RoomAvailability.TRANSITION -> Triple(Warning, WarningSoft, Icons.Rounded.TransferWithinAStation)
    }

    AppCard(onClick = onToggle) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(statusBackground, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MeetingRoom, contentDescription = null, tint = statusColor, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.room.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${entry.room.buildingName} · ${entry.room.floor}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusPill(
                    text = entry.status.availability.label,
                    color = statusColor,
                    background = statusBackground,
                    icon = statusIcon
                )
            }

            Text(
                entry.status.detail,
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.status.availability == RoomAvailability.OCCUPIED) Danger else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (entry.status.availability == RoomAvailability.AVAILABLE) FontWeight.Medium else FontWeight.Normal
            )

            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                RoomMeta(Icons.Rounded.Groups, "${entry.room.capacity} seats")
                RoomMeta(Icons.Rounded.EventNote, "${entry.slots.count { it.dayOfWeek == LocalDateTime.now().dayOfWeek.value }} classes today")
                if (entry.status.isOverride) {
                    StatusPill("Admin override", InfoBlue, InfoSoft)
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(11.dp)
                    ) {
                        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Chair, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                "Equipment: ${entry.room.equipment}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (entry.slots.isEmpty()) {
                        Text(
                            "No classes are scheduled in this room.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "Weekly schedule",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        entry.slots.sortedWith(compareBy({ it.dayOfWeek }, { it.startMinute })).forEach { slot ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    dayLabel(DayOfWeek.of(slot.dayOfWeek.coerceIn(1, 7))).take(3),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary,
                                    modifier = Modifier.width(30.dp)
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(slot.subject, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${slot.section} · ${slot.instructor}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    "${formatMinuteShort(slot.startMinute)}–${formatMinuteShort(slot.endMinute)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    if (expanded) "Tap to hide schedule" else "Tap to view schedule",
                    style = MaterialTheme.typography.labelSmall,
                    color = TealPrimary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RoomMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = MutedInk, modifier = Modifier.size(13.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MutedInk)
    }
}
