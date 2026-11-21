package ph.edu.wvsu.landas.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AlertSeverity
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
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
import ph.edu.wvsu.landas.util.formatRelative
import ph.edu.wvsu.landas.util.formatTime

@Composable
fun AdminDashboardScreen(
    admin: UserEntity?,
    isOnline: Boolean,
    activeAlert: AlertEntity?,
    alerts: List<AlertEntity>,
    users: List<UserEntity>,
    attendanceToday: List<AttendanceRecord>,
    distressSignals: List<DistressSignal>,
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    pendingSync: Int,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onOpenRoutes: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenAttendance: () -> Unit,
    onOpenDistress: () -> Unit,
    onOpenClassrooms: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenData: () -> Unit,
    onOpenMap: () -> Unit,
    onSync: () -> Unit,
    onSignOut: () -> Unit
) {
    val students = users.filter { it.role == Roles.STUDENT && it.isActive }
    val faculty = users.filter { it.role == Roles.FACULTY && it.isActive }
    val safeStudentIds = attendanceToday.filter { it.role == Roles.STUDENT }.map { it.userId }.toSet()
    val safeFacultyIds = attendanceToday.filter { it.role == Roles.FACULTY }.map { it.userId }.toSet()
    val activeSos = distressSignals.filter { it.status != "RESOLVED" }
    val blockedNodes = nodes.count { it.hazardBlocked }
    val blockedEdges = edges.count { it.hazardBlocked }
    val openAreas = nodes.filter { it.isEvacuationArea && !it.hazardBlocked }
    val evacuationAreas = nodes.filter { it.isEvacuationArea }
    val totalOccupancy = evacuationAreas.sumOf { it.occupancy }
    val totalCapacity = evacuationAreas.sumOf { it.capacity }
    val safeTotal = attendanceToday.map { it.userId }.distinct().size
    val accountabilityDenominator = students.size + faculty.size
    val accountability = if (accountabilityDenominator == 0) 0f else safeTotal.toFloat() / accountabilityDenominator

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0B2E33), TealDark, Color(0xFF0E4A50))))
            ) {
                Canvas(Modifier.matchParentSize()) {
                    drawCircle(
                        color = Gold.copy(alpha = 0.05f),
                        radius = 140.dp.toPx(),
                        center = Offset(size.width * 0.94f, 30.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.035f),
                        radius = 90.dp.toPx(),
                        center = Offset(size.width * 0.05f, 210.dp.toPx())
                    )
                }
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 17.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = onBack,
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(19.dp)
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Danger, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Dashboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
                        }
                        Spacer(Modifier.width(9.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "CDRRMO Control",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "WVSU – Pototan Campus operations",
                                color = Color.White.copy(alpha = 0.62f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        StatusPill(
                            text = if (isOnline) "Online" else "Offline",
                            color = if (isOnline) Success else Warning,
                            background = if (isOnline) SuccessSoft else WarningSoft,
                            icon = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(
                        "Campus safety overview",
                        color = Color.White.copy(alpha = 0.66f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        admin?.fullName ?: "Administrator",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Coordinator console · ${LocalDateTime.now().let { "%02d:%02d".format(it.hour, it.minute) }}",
                        color = Color.White.copy(alpha = 0.52f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(16.dp))
                    if (activeAlert == null) {
                        Surface(
                            color = Color.White.copy(alpha = 0.09f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Box(Modifier.size(8.dp).background(Color(0xFF8CE99A), CircleShape))
                                Text(
                                    "No active emergency · all routes available",
                                    color = Color.White.copy(alpha = 0.86f),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF8CE99A), modifier = Modifier.size(17.dp))
                            }
                        }
                    } else {
                        Surface(
                            onClick = onOpenAlerts,
                            color = if (activeAlert.severity == AlertSeverity.CRITICAL) Danger else Warning,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Icon(Icons.Rounded.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "ACTIVE ${activeAlert.type} ALERT",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        activeAlert.title,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    AdminMetric(
                        modifier = Modifier.weight(1f),
                        value = "$safeTotal",
                        label = "Marked safe",
                        icon = Icons.Rounded.CheckCircle,
                        accent = Success,
                        background = SuccessSoft
                    )
                    AdminMetric(
                        modifier = Modifier.weight(1f),
                        value = "${activeSos.size}",
                        label = "Active SOS",
                        icon = Icons.Rounded.Sos,
                        accent = if (activeSos.isEmpty()) Success else Danger,
                        background = if (activeSos.isEmpty()) SuccessSoft else DangerSoft
                    )
                    AdminMetric(
                        modifier = Modifier.weight(1f),
                        value = "${blockedNodes + blockedEdges}",
                        label = "Blocked routes",
                        icon = Icons.Rounded.WarningAmber,
                        accent = if (blockedNodes + blockedEdges == 0) Success else Warning,
                        background = if (blockedNodes + blockedEdges == 0) SuccessSoft else WarningSoft
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    AdminMetric(
                        modifier = Modifier.weight(1f),
                        value = "${students.count { it.id in safeStudentIds }}/${students.size}",
                        label = "Students accounted",
                        icon = Icons.Rounded.School,
                        accent = InfoBlue,
                        background = InfoSoft
                    )
                    AdminMetric(
                        modifier = Modifier.weight(1f),
                        value = "${faculty.count { it.id in safeFacultyIds }}/${faculty.size}",
                        label = "Faculty accounted",
                        icon = Icons.Rounded.Badge,
                        accent = Color(0xFF6D5FA8),
                        background = Color(0xFFEEEAF8)
                    )
                    AdminMetric(
                        modifier = Modifier.weight(1f),
                        value = "${distressSignals.count { it.synced }}",
                        label = "Synced signals",
                        icon = Icons.Rounded.Sync,
                        accent = TealPrimary,
                        background = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Accountability progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(
                                text = "${(accountability * 100).toInt()}%",
                                color = if (accountability >= 0.8f) Success else Warning,
                                background = if (accountability >= 0.8f) SuccessSoft else WarningSoft
                            )
                        }
                        LinearProgressIndicator(
                            progress = { accountability.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (accountability >= 0.8f) Success else Warning,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            "$safeTotal of $accountabilityDenominator active students and faculty are marked safe today. ${openAreas.size} evacuation area${if (openAreas.size == 1) "" else "s"} open with ${(totalCapacity - totalOccupancy).coerceAtLeast(0)} remaining spaces.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SectionHeader("Command modules")
                val modules = listOf(
                    AdminModule(Icons.Rounded.AltRoute, "Routes & hazards", "Edit locations, paths, capacities", TealPrimary, MaterialTheme.colorScheme.primaryContainer, onOpenRoutes),
                    AdminModule(Icons.Rounded.NotificationsActive, "Alerts & notices", "Broadcast emergencies and announcements", Danger, DangerSoft, onOpenAlerts),
                    AdminModule(Icons.Rounded.AssignmentTurnedIn, "Attendance", "Check-ins, manual entry, reset", Success, SuccessSoft, onOpenAttendance),
                    AdminModule(Icons.Rounded.Sos, "Distress response", "Monitor and resolve SOS signals", SafetyOrange, Color(0xFFFFF0E5), onOpenDistress),
                    AdminModule(Icons.Rounded.EventNote, "Class schedules", "Room overrides and class slots", InfoBlue, InfoSoft, onOpenClassrooms),
                    AdminModule(Icons.Rounded.ManageAccounts, "Users", "Activate accounts and reset passwords", Color(0xFF6D5FA8), Color(0xFFEEEAF8), onOpenUsers),
                    AdminModule(Icons.Rounded.Analytics, "Analytics", "Readiness, trends, and response metrics", Color(0xFFB7791F), WarningSoft, onOpenAnalytics),
                    AdminModule(Icons.Rounded.Storage, "Backup & data", "Export, restore, and sync queue", TealDark, Color(0xFFE4F1EF), onOpenData)
                )
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    modules.chunked(2).forEach { rowModules ->
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            rowModules.forEach { module ->
                                AdminModuleCard(
                                    module = module,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(126.dp)
                                )
                            }
                        }
                    }
                }

                SectionHeader("Live evacuation capacity")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        evacuationAreas.forEach { area ->
                            val ratio = if (area.capacity > 0) area.occupancy.toFloat() / area.capacity else 0f
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        area.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        if (area.hazardBlocked) "CLOSED" else "${area.occupancy}/${area.capacity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            area.hazardBlocked -> Danger
                                            ratio >= 0.9f -> Danger
                                            ratio >= 0.7f -> Warning
                                            else -> Success
                                        }
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = when {
                                        area.hazardBlocked -> Danger
                                        ratio >= 0.9f -> Danger
                                        ratio >= 0.7f -> Warning
                                        else -> Success
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = onOpenMap, modifier = Modifier.align(Alignment.End)) {
                            Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Open live map")
                        }
                    }
                }

                SectionHeader("Recent activity")
                if (activeSos.isNotEmpty()) {
                    activeSos.take(3).forEach { signal ->
                        Surface(
                            onClick = onOpenDistress,
                            shape = RoundedCornerShape(15.dp),
                            color = DangerSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(35.dp)
                                        .background(Danger, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Sos, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(signal.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "${signal.nearestLocation} · ${signal.timestamp.formatRelative()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(
                                    if (signal.status == "ACKNOWLEDGED") "Acknowledged" else "Active",
                                    if (signal.status == "ACKNOWLEDGED") Warning else Danger,
                                    if (signal.status == "ACKNOWLEDGED") WarningSoft else Color.White.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }

                val recentAttendance = attendanceToday.take(3)
                recentAttendance.forEach { record ->
                    Surface(
                        onClick = onOpenAttendance,
                        shape = RoundedCornerShape(15.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Avatar(record.fullName, size = 34.dp)
                            Column(Modifier.weight(1f)) {
                                Text(record.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${record.evacuationArea} · ${record.timestamp.formatTime()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusPill(
                                if (record.synced) "Synced" else "Queued",
                                if (record.synced) Success else Warning,
                                if (record.synced) SuccessSoft else WarningSoft
                            )
                        }
                    }
                }

                if (activeSos.isEmpty() && recentAttendance.isEmpty()) {
                    AppCard {
                        Text(
                            "No attendance or distress activity has been recorded today.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }

                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Sync, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Offline sync queue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            TextButton(onClick = onSync, enabled = !isSyncing) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isSyncing) "Syncing…" else "Sync now", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            if (pendingSync > 0) {
                                "$pendingSync record${if (pendingSync == 1) "" else "s"} waiting in the local queue. Records are visible to this console immediately."
                            } else {
                                "All local records are synchronized. The queue is empty."
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AppCard {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Avatar(admin?.fullName ?: "Administrator", size = 38.dp)
                        Column(Modifier.weight(1f)) {
                            Text(admin?.fullName ?: "Administrator", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${admin?.schoolId.orEmpty()} · CDRRMO coordinator",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = onSignOut) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = Danger, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sign out", color = Danger, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Text(
                    "LANDAS administrator console · prototype data",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

private data class AdminModule(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val background: Color,
    val onClick: () -> Unit
)

@Composable
private fun AdminModuleCard(module: AdminModule, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, onClick = module.onClick) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(module.background, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(module.icon, contentDescription = null, tint = module.accent, modifier = Modifier.size(19.dp))
            }
            Text(
                module.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                module.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun AdminMetric(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier
                    .size(27.dp)
                    .background(background, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp,
                maxLines = 2
            )
        }
    }
}
