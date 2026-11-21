package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
import ph.edu.wvsu.landas.ui.components.DetailRow
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.SectionHeader
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.DangerSoft
import ph.edu.wvsu.landas.ui.theme.Gold
import ph.edu.wvsu.landas.ui.theme.MutedInk
import ph.edu.wvsu.landas.ui.theme.SafetyOrange
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.ui.viewmodel.MarkSafeUiState
import ph.edu.wvsu.landas.util.formatTime

@Composable
fun MarkedSafeScreen(
    user: UserEntity?,
    nodes: List<CampusNodeEntity>,
    users: List<UserEntity>,
    attendanceToday: List<AttendanceRecord>,
    markSafeState: MarkSafeUiState,
    isOnline: Boolean,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onMarkSafe: (CampusNodeEntity) -> Unit,
    onDismissConfirmation: () -> Unit
) {
    val areas = nodes.filter { it.isEvacuationArea && !it.hazardBlocked }
    val existingRecord = user?.let { current ->
        attendanceToday.firstOrNull { it.userId == current.id }
    }
    val confirmationRecord = markSafeState.outcome?.record ?: existingRecord
    val alreadyMarked = markSafeState.outcome?.alreadyMarked ?: (existingRecord != null)
    var selectedAreaId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(areas, selectedAreaId) {
        if (areas.isEmpty()) {
            selectedAreaId = null
        } else if (selectedAreaId == null || areas.none { it.id == selectedAreaId }) {
            selectedAreaId = areas
                .filter { it.capacity == 0 || it.occupancy < it.capacity }
                .maxByOrNull { if (it.capacity > 0) it.capacity - it.occupancy else 0 }
                ?.id
                ?: areas.firstOrNull()?.id
        }
    }
    val selectedArea = areas.firstOrNull { it.id == selectedAreaId }

    LandasScaffold(
        title = "Marked safe",
        subtitle = "Identity-verified attendance check-in",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            AnimatedVisibility(visible = confirmationRecord != null) {
                if (confirmationRecord != null) {
                    SafeConfirmationCard(
                        record = confirmationRecord,
                        alreadyMarked = alreadyMarked,
                        onOpenMap = onOpenMap,
                        onDismiss = {
                            onDismissConfirmation()
                            onBack()
                        }
                    )
                }
            }

            if (confirmationRecord == null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .background(TealPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                        }
                        Text(
                            "Confirm your safety",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mark yourself safe after reaching an evacuation area. Your timestamp will help responders account for everyone.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                SectionHeader("1. Verify your identity")
                AppCard {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Avatar(user?.fullName ?: "LANDAS User", size = 46.dp)
                        Column(Modifier.weight(1f)) {
                            Text(user?.fullName ?: "Signed-in user", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${user?.schoolId.orEmpty()} · ${roleLabel(user?.role)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusPill(
                            "Verified",
                            Success,
                            SuccessSoft,
                            Icons.Rounded.CheckCircle
                        )
                    }
                }

                SectionHeader("2. Select your evacuation area")
                if (areas.isEmpty()) {
                    AppCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("No open evacuation area", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Danger)
                            Text(
                                "All assembly areas are currently blocked or unavailable. Contact the CDRRMO coordinator and move to a safe open space.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    areas.forEach { area ->
                        val selected = area.id == selectedAreaId
                        val free = (area.capacity - area.occupancy).coerceAtLeast(0)
                        val ratio = if (area.capacity > 0) area.occupancy.toFloat() / area.capacity else 0f
                        Surface(
                            onClick = { selectedAreaId = area.id },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(17.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (selected) 1.5.dp else 1.dp,
                                if (selected) TealPrimary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(11.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .border(
                                            2.dp,
                                            if (selected) TealPrimary else MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                        .background(if (selected) TealPrimary else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(area.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        area.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp),
                                        color = if (ratio > .85f) Danger else Success,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("$free", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (free == 0) Danger else Success)
                                    Text("spaces", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${area.occupancy}/${area.capacity}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                SectionHeader("3. Confirm attendance")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = if (isOnline) Success else Warning,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                if (isOnline) {
                                    "Online · attendance will sync immediately"
                                } else {
                                    "Offline · this check-in will be queued and synced later"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AnimatedVisibility(visible = markSafeState.error != null) {
                            Surface(color = DangerSoft, shape = RoundedCornerShape(10.dp)) {
                                Text(
                                    markSafeState.error.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Danger,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        Button(
                            onClick = { selectedArea?.let(onMarkSafe) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = selectedArea != null && !markSafeState.saving,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Success,
                                contentColor = Color.White
                            )
                        ) {
                            if (markSafeState.saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(9.dp))
                                Text("Recording your check-in…")
                            } else {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(21.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("I am safe · ${selectedArea?.shortName ?: "Select area"}", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Text(
                            "One tap records your identity, selected assembly area, and an automatic timestamp. Each account can submit one check-in per day.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (user?.role == Roles.FACULTY || user?.role == Roles.ADMIN) {
                    FacultyMonitoringCard(
                        students = users.filter { it.role == Roles.STUDENT && it.isActive },
                        attendanceToday = attendanceToday
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Rounded.Map, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                        Text(
                            "Not yet at an assembly area? Open the evacuation map and follow the nearest safe route first.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onOpenMap) { Text("Map", style = MaterialTheme.typography.labelSmall) }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SafeConfirmationCard(
    record: AttendanceRecord,
    alreadyMarked: Boolean,
    onOpenMap: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Success
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .background(Color.White.copy(alpha = 0.18f), CircleShape)
                        .border(3.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
                }
                Text(
                    if (alreadyMarked) "You are already marked safe" else "You are marked safe",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Your check-in has been recorded and is available to campus responders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center
                )
                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (record.synced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            if (record.synced) "Synced to campus records" else "Queued offline · will sync automatically",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Check-in details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                DetailRow("Name", record.fullName)
                DetailRow("Section", record.section)
                DetailRow("Assembly area", record.evacuationArea)
                DetailRow("Timestamp", record.timestamp.formatTime())
                DetailRow("Method", if (record.method == "ONE_TAP") "One-tap check-in" else "Administrator manual entry")
                DetailRow(
                    "Sync status",
                    if (record.synced) "Synchronized" else "Pending offline sync",
                    valueColor = if (record.synced) Success else Warning
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Button(
                onClick = onOpenMap,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(Icons.Rounded.AltRoute, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("View map")
            }
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun FacultyMonitoringCard(
    students: List<UserEntity>,
    attendanceToday: List<AttendanceRecord>
) {
    val safeIds = remember(attendanceToday) { attendanceToday.map { it.userId }.toSet() }
    val safeCount = students.count { it.id in safeIds }
    val progress by animateFloatAsState(
        targetValue = if (students.isEmpty()) 0f else safeCount.toFloat() / students.size,
        label = "faculty-monitor-progress"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("Faculty monitoring")
        AppCard {
            Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(39.dp)
                            .background(SuccessSoft, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Groups, contentDescription = null, tint = Success, modifier = Modifier.size(21.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Class representative view", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("$safeCount of ${students.size} students marked safe", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Success
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp),
                    color = Success,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    students.forEach { student ->
                        val marked = student.id in safeIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Avatar(student.fullName, size = 30.dp)
                            Column(Modifier.weight(1f)) {
                                Text(student.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(student.schoolId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(
                                if (marked) "Safe" else "Unaccounted",
                                if (marked) Success else Warning,
                                if (marked) SuccessSoft else WarningSoft
                            )
                        }
                    }
                }
                Text(
                    "This representative view is read-only. Manual headcount and missing-person tracking are available to administrators.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun roleLabel(role: String?): String = when (role) {
    Roles.STUDENT -> "Student"
    Roles.FACULTY -> "Faculty"
    Roles.ADMIN -> "Administrator"
    else -> "Campus member"
}
