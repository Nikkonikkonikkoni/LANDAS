package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.ScheduleSlotEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.EmptyState
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.SectionHeader
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.DangerSoft
import ph.edu.wvsu.landas.ui.theme.Gold
import ph.edu.wvsu.landas.ui.theme.InfoBlue
import ph.edu.wvsu.landas.ui.theme.InfoSoft
import ph.edu.wvsu.landas.ui.theme.MutedInk
import ph.edu.wvsu.landas.ui.theme.Navy
import ph.edu.wvsu.landas.ui.theme.SafetyOrange
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.TealDark
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.util.ClassroomAvailability
import ph.edu.wvsu.landas.util.RoomAvailability
import ph.edu.wvsu.landas.util.formatMinuteShort

@Composable
fun AdminClassroomsScreen(
    classrooms: List<ClassroomEntity>,
    schedules: List<ScheduleSlotEntity>,
    onBack: () -> Unit,
    onSetOverride: (ClassroomEntity, String?) -> Unit,
    onAddSlot: (ClassroomEntity, Int, Int, Int, String, String, String) -> Unit,
    onDeleteSlot: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var buildingFilter by remember { mutableStateOf("All") }
    var expandedRoomId by remember { mutableStateOf<String?>(null) }
    var addingSlotFor by remember { mutableStateOf<ClassroomEntity?>(null) }
    var deletingSlot by remember { mutableStateOf<ScheduleSlotEntity?>(null) }

    val today = LocalDate.now().dayOfWeek.value
    val buildings = listOf("All") + classrooms.map { it.buildingCode }.filter { it.isNotBlank() }.distinct().sorted()
    val statuses = classrooms.associateWith { room ->
        ClassroomAvailability.evaluate(room, schedules.filter { it.roomId == room.id && it.dayOfWeek == today })
    }
    val availableNow = statuses.values.count { it.availability == RoomAvailability.AVAILABLE }
    val occupiedNow = statuses.values.count { it.availability == RoomAvailability.OCCUPIED }
    val overridden = classrooms.count { it.statusOverride != null }

    val filtered = classrooms
        .filter { buildingFilter == "All" || it.buildingCode == buildingFilter }
        .filter {
            query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.buildingName.contains(query, ignoreCase = true) ||
                it.buildingCode.contains(query, ignoreCase = true)
        }
        .sortedWith(compareBy({ it.buildingCode }, { it.name }))

    LandasScaffold(
        title = "Class schedules",
        subtitle = "Room status and class slot management",
        onBack = onBack,
        actions = {
            StatusPill(
                "$availableNow open now",
                Success,
                SuccessSoft,
                Icons.Rounded.CheckCircle
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = TealDark) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Class, contentDescription = null, tint = Gold, modifier = Modifier.size(21.dp))
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Room availability control",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Status derives from the weekly timetable. Overrides apply until cleared.",
                                    color = Color.White.copy(alpha = 0.64f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClassroomStat("${classrooms.size}", "Rooms", Modifier.weight(1f))
                            ClassroomStat("$availableNow", "Available", Modifier.weight(1f))
                            ClassroomStat("$occupiedNow", "In class", Modifier.weight(1f))
                            ClassroomStat("$overridden", "Overrides", Modifier.weight(1f))
                        }
                    }
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
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    buildings.forEach { building ->
                        FilterChip(
                            selected = buildingFilter == building,
                            onClick = { buildingFilter = building },
                            label = { Text(if (building == "All") "All buildings" else building) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.MeetingRoom,
                        title = "No rooms match your filters",
                        message = "Adjust the search or building filter to see classroom schedules."
                    )
                }
            }

            items(filtered, key = { it.id }) { room ->
                val status = statuses.getValue(room)
                val roomSlots = schedules
                    .filter { it.roomId == room.id }
                    .sortedWith(compareBy({ it.dayOfWeek }, { it.startMinute }))
                val expanded = expandedRoomId == room.id
                RoomAdminCard(
                    room = room,
                    availability = status.availability,
                    detail = status.detail,
                    isOverride = status.isOverride,
                    slotCount = roomSlots.size,
                    expanded = expanded,
                    onToggleExpand = { expandedRoomId = if (expanded) null else room.id },
                    onSetOverride = { onSetOverride(room, it) },
                    onAddSlot = { addingSlotFor = room }
                )
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (roomSlots.isEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "No class slots yet. Add the first schedule to drive automatic availability.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(11.dp)
                                )
                            }
                        }
                        roomSlots.forEach { slot ->
                            ScheduleRow(slot = slot, onDelete = { deletingSlot = slot })
                        }
                    }
                }
            }

            item {
                SectionHeader("How status is computed")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusLegend(
                            RoomAvailability.AVAILABLE,
                            "No class in session and the next class is more than 15 minutes away."
                        )
                        StatusLegend(
                            RoomAvailability.TRANSITION,
                            "A class ended within the last 15 minutes, or the next class starts within 15 minutes."
                        )
                        StatusLegend(
                            RoomAvailability.OCCUPIED,
                            "A scheduled class is currently in session."
                        )
                        HorizontalDivider(Modifier.padding(vertical = 3.dp))
                        Text(
                            "Overrides take precedence over the timetable and are visible immediately on the student classroom screen. Example: close a room for repairs or open it for an event.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    addingSlotFor?.let { room ->
        AddSlotDialog(
            room = room,
            onDismiss = { addingSlotFor = null },
            onAdd = { day, start, end, subject, instructor, section ->
                onAddSlot(room, day, start, end, subject, instructor, section)
                addingSlotFor = null
            }
        )
    }

    deletingSlot?.let { slot ->
        AlertDialog(
            onDismissRequest = { deletingSlot = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Danger) },
            title = { Text("Delete this class slot?") },
            text = {
                Text("\"${slot.subject}\" on ${dayName(slot.dayOfWeek)} at ${formatMinuteShort(slot.startMinute)} will be removed from the timetable.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = slot.id
                    deletingSlot = null
                    onDeleteSlot(id)
                }) {
                    Text("Delete slot", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSlot = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RoomAdminCard(
    room: ClassroomEntity,
    availability: RoomAvailability,
    detail: String,
    isOverride: Boolean,
    slotCount: Int,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onSetOverride: (String?) -> Unit,
    onAddSlot: () -> Unit
) {
    val accent = when (availability) {
        RoomAvailability.AVAILABLE -> Success
        RoomAvailability.OCCUPIED -> Danger
        RoomAvailability.TRANSITION -> Warning
    }
    val background = when (availability) {
        RoomAvailability.AVAILABLE -> SuccessSoft
        RoomAvailability.OCCUPIED -> DangerSoft
        RoomAvailability.TRANSITION -> WarningSoft
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOverride) InfoBlue.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(background, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (availability) {
                            RoomAvailability.AVAILABLE -> Icons.Rounded.LightMode
                            RoomAvailability.OCCUPIED -> Icons.Rounded.DarkMode
                            RoomAvailability.TRANSITION -> Icons.Rounded.Tune
                        },
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(room.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "${room.buildingCode} · ${room.floor} · ${room.capacity} seats",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusPill(
                    text = availability.label,
                    color = accent,
                    background = background,
                    icon = if (isOverride) Icons.Rounded.Tune else null
                )
            }

            Text(
                detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.Air, contentDescription = null, tint = MutedInk, modifier = Modifier.size(13.dp))
                Text(
                    room.equipment.ifBlank { "Standard classroom" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isOverride) {
                    StatusPill("Manual override", InfoBlue, InfoSoft)
                }
            }

            HorizontalDivider()

            Text(
                "Status override",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf<Pair<String?, String>>(
                    null to "Auto",
                    "AVAILABLE" to "Force open",
                    "OCCUPIED" to "Force occupied"
                ).forEach { (value, label) ->
                    FilterChip(
                        selected = room.statusOverride == value,
                        onClick = { onSetOverride(value) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (value) {
                                "OCCUPIED" -> Danger
                                "AVAILABLE" -> Success
                                else -> TealPrimary
                            },
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onToggleExpand,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.EventNote, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(15.dp))
                        Text(
                            "$slotCount class slot${if (slotCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MutedInk,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Spacer(Modifier.width(7.dp))
                Surface(
                    onClick = onAddSlot,
                    shape = RoundedCornerShape(10.dp),
                    color = TealPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("Add class", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(slot: ScheduleSlotEntity, onDelete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        dayShort(slot.dayOfWeek),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                }
            }
            Column(Modifier.weight(1f)) {
                Text(slot.subject, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatMinuteShort(slot.startMinute)}–${formatMinuteShort(slot.endMinute)}${if (slot.section.isNotBlank()) " · ${slot.section}" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (slot.instructor.isNotBlank()) {
                    Text(
                        slot.instructor,
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete slot", tint = Danger, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AddSlotDialog(
    room: ClassroomEntity,
    onDismiss: () -> Unit,
    onAdd: (Int, Int, Int, String, String, String) -> Unit
) {
    var day by remember { mutableStateOf(LocalDate.now().dayOfWeek.value) }
    var start by remember { mutableStateOf(7 * 60 + 30) }
    var end by remember { mutableStateOf(8 * 60 + 30) }
    var subject by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }

    val times = remember { (6 * 60..20 * 60 step 30).toList() }
    val valid = subject.isNotBlank() && end > start

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 24.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Add class slot", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "${room.name} · ${room.buildingCode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                }

                Text("Day", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    (1..7).forEach { value ->
                        FilterChip(
                            selected = day == value,
                            onClick = { day = value },
                            label = { Text(dayShort(value), style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                Text("Starts", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    times.forEach { minute ->
                        FilterChip(
                            selected = start == minute,
                            onClick = {
                                start = minute
                                if (end <= minute) end = (minute + 60).coerceAtMost(21 * 60)
                            },
                            label = { Text(formatMinuteShort(minute), style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                Text("Ends", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    times.filter { it > start }.forEach { minute ->
                        FilterChip(
                            selected = end == minute,
                            onClick = { end = minute },
                            label = { Text(formatMinuteShort(minute), style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }

                if (end <= start) {
                    Surface(color = DangerSoft, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            "End time must be later than the start time.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Danger,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                PrimaryButton(
                    text = "Add class slot",
                    icon = Icons.Rounded.Add,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = valid,
                    onClick = {
                        onAdd(
                            day,
                            start,
                            end,
                            subject.trim(),
                            instructor.trim(),
                            section.trim()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ClassroomStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(alpha = 0.09f), shape = RoundedCornerShape(10.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                label,
                color = Color.White.copy(alpha = 0.58f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StatusLegend(availability: RoomAvailability, explanation: String) {
    val accent = when (availability) {
        RoomAvailability.AVAILABLE -> Success
        RoomAvailability.OCCUPIED -> Danger
        RoomAvailability.TRANSITION -> Warning
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(9.dp)
                .background(accent, CircleShape)
        )
        Column {
            Text(availability.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(
                explanation,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun dayShort(day: Int): String = when (day) {
    1 -> "Mon"
    2 -> "Tue"
    3 -> "Wed"
    4 -> "Thu"
    5 -> "Fri"
    6 -> "Sat"
    else -> "Sun"
}

private fun dayName(day: Int): String = when (day) {
    1 -> "Monday"
    2 -> "Tuesday"
    3 -> "Wednesday"
    4 -> "Thursday"
    5 -> "Friday"
    6 -> "Saturday"
    else -> "Sunday"
}
