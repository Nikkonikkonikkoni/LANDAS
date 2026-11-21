package ph.edu.wvsu.landas.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
import ph.edu.wvsu.landas.ui.components.EmptyState
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.SectionHeader
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.DangerSoft
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
import ph.edu.wvsu.landas.util.formatDateTime
import ph.edu.wvsu.landas.util.formatTime

private enum class AttendanceFilter(val label: String) {
    ALL("All records"),
    STUDENTS("Students"),
    FACULTY("Faculty"),
    QUEUED("Queued offline")
}

@Composable
fun AdminAttendanceScreen(
    attendanceToday: List<AttendanceRecord>,
    allAttendance: List<AttendanceRecord>,
    users: List<UserEntity>,
    nodes: List<CampusNodeEntity>,
    onBack: () -> Unit,
    onAddManual: (UserEntity, CampusNodeEntity) -> Unit,
    onClearToday: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AttendanceFilter.ALL) }
    var showManualDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val students = attendanceToday.filter { it.role == Roles.STUDENT }
    val faculty = attendanceToday.filter { it.role == Roles.FACULTY }
    val others = attendanceToday.filter { it.role != Roles.STUDENT && it.role != Roles.FACULTY }
    val queued = attendanceToday.filter { !it.synced }
    val activeAreas = nodes.filter { it.isEvacuationArea && !it.hazardBlocked }

    val filtered = attendanceToday
        .filter {
            query.isBlank() ||
                it.fullName.contains(query, ignoreCase = true) ||
                it.userId.contains(query, ignoreCase = true) ||
                it.evacuationArea.contains(query, ignoreCase = true)
        }
        .filter {
            when (filter) {
                AttendanceFilter.ALL -> true
                AttendanceFilter.STUDENTS -> it.role == Roles.STUDENT
                AttendanceFilter.FACULTY -> it.role == Roles.FACULTY
                AttendanceFilter.QUEUED -> !it.synced
            }
        }

    LandasScaffold(
        title = "Attendance & accountability",
        subtitle = "Emergency check-ins and manual headcount",
        onBack = onBack,
        actions = {
            StatusPill(
                "${attendanceToday.size} today",
                TealPrimary,
                MaterialTheme.colorScheme.primaryContainer,
                Icons.Rounded.AssignmentTurnedIn
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
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = TealDark
                ) {
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
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Today's accountability",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${attendanceToday.size} unique check-in${if (attendanceToday.size == 1) "" else "s"} recorded",
                                    color = Color.White.copy(alpha = 0.64f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            TextButton(onClick = { showManualDialog = true }) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HeaderStat("${students.size}", "Students", Modifier.weight(1f))
                            HeaderStat("${faculty.size}", "Faculty", Modifier.weight(1f))
                            HeaderStat("${others.size}", "Staff", Modifier.weight(1f))
                            HeaderStat("${queued.size}", "Queued", Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    AttendanceTile(
                        modifier = Modifier.weight(1f),
                        value = "${attendanceToday.map { it.userId }.distinct().size}",
                        label = "People accounted",
                        icon = Icons.Rounded.Groups,
                        accent = Success,
                        background = SuccessSoft
                    )
                    AttendanceTile(
                        modifier = Modifier.weight(1f),
                        value = "${allAttendance.size}",
                        label = "Historical records",
                        icon = Icons.Rounded.AssignmentTurnedIn,
                        accent = InfoBlue,
                        background = InfoSoft
                    )
                    AttendanceTile(
                        modifier = Modifier.weight(1f),
                        value = "${activeAreas.size}",
                        label = "Open areas",
                        icon = Icons.Rounded.Flag,
                        accent = SafetyOrange,
                        background = Color(0xFFFFF0E5)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search name, ID, or assembly area") },
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
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    AttendanceFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Check-in log",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${filtered.size} shown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.AssignmentTurnedIn,
                        title = "No check-ins to show",
                        message = "Records appear here as students mark themselves safe or as administrators add manual attendance."
                    )
                }
            }

            items(filtered, key = { it.id }) { record ->
                AttendanceRecordRow(record)
            }

            item {
                SectionHeader("Data management")
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(
                            "Clearing today's attendance removes today's check-ins and releases their spaces in assembly-area occupancy counts. Historical records from previous days are preserved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            onClick = { showClearDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = DangerSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Danger, modifier = Modifier.size(18.dp))
                                Text(
                                    "Clear today's attendance",
                                    color = Danger,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showManualDialog) {
        ManualAttendanceDialog(
            users = users,
            areas = activeAreas,
            alreadyMarkedUserIds = attendanceToday.map { it.userId }.toSet(),
            onDismiss = { showManualDialog = false },
            onAdd = { user, area ->
                onAddManual(user, area)
                showManualDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Danger) },
            title = { Text("Clear today's attendance?") },
            text = {
                Text(
                    "This removes ${attendanceToday.size} check-in${if (attendanceToday.size == 1) "" else "s"} recorded today and frees the corresponding assembly-area capacity. Records from previous days remain intact. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearToday()
                }) {
                    Text("Clear today", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun HeaderStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.09f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.58f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun AttendanceTile(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = background,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun AttendanceRecordRow(record: AttendanceRecord) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Avatar(record.fullName, size = 38.dp)
            Column(Modifier.weight(1f)) {
                Text(record.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(
                    "${record.userId} · ${record.role.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Flag, contentDescription = null, tint = Success, modifier = Modifier.size(12.dp))
                    Text(
                        record.evacuationArea,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                StatusPill(
                    if (record.synced) "Synced" else "Queued",
                    if (record.synced) Success else Warning,
                    if (record.synced) SuccessSoft else WarningSoft,
                    if (record.synced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
                )
                Text(
                    record.timestamp.formatTime(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
                Text(
                    if (record.method == "MANUAL") "Manual" else "One-tap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun ManualAttendanceDialog(
    users: List<UserEntity>,
    areas: List<CampusNodeEntity>,
    alreadyMarkedUserIds: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (UserEntity, CampusNodeEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var selectedArea by remember { mutableStateOf<CampusNodeEntity?>(null) }
    var step by remember { mutableStateOf(0) }

    val filteredUsers = users
        .filter { it.isActive }
        .filter {
            query.isBlank() ||
                it.fullName.contains(query, ignoreCase = true) ||
                it.schoolId.contains(query, ignoreCase = true)
        }
        .sortedWith(compareBy({ it.role }, { it.fullName }))

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (step == 0) "Manual attendance" else "Select assembly area",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (step == 0) "Step 1 of 2 · choose a person" else "Step 2 of 2 · ${selectedUser?.fullName.orEmpty()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                }

                if (step == 0) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search name or school ID") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.height(330.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            val already = user.id in alreadyMarkedUserIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !already) {
                                        selectedUser = user
                                        step = 1
                                    }
                                    .padding(vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Avatar(user.fullName, size = 32.dp)
                                Column(Modifier.weight(1f)) {
                                    Text(user.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${user.schoolId} · ${user.role.lowercase().replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (already) {
                                    StatusPill("Marked", Success, SuccessSoft)
                                } else {
                                    Icon(Icons.Rounded.Add, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        items(areas, key = { it.id }) { area ->
                            val selected = selectedArea?.id == area.id
                            Surface(
                                onClick = { selectedArea = area },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) TealPrimary else Color.Transparent
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Flag, contentDescription = null, tint = Success, modifier = Modifier.size(17.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(area.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${area.occupancy}/${area.capacity} occupied",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    if (areas.isEmpty()) {
                        Surface(color = WarningSoft, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                "No open evacuation areas are available. Reopen an area in Routes & hazards first.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(11.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            if (step == 0) onDismiss() else {
                                step = 0
                                selectedArea = null
                            }
                        }
                    ) {
                        Text(if (step == 0) "Cancel" else "Back")
                    }
                    Spacer(Modifier.weight(1f))
                    if (step == 1) {
                        PrimaryButton(
                            text = "Add attendance",
                            icon = Icons.Rounded.AssignmentTurnedIn,
                            enabled = selectedArea != null,
                            onClick = {
                                val user = selectedUser
                                val area = selectedArea
                                if (user != null && area != null) onAdd(user, area)
                            }
                        )
                    }
                }
            }
        }
    }
}
