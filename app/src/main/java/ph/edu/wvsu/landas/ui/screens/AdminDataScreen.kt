package ph.edu.wvsu.landas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Dataset
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AnnouncementEntity
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.ScheduleSlotEntity
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.LandasScaffold
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
import ph.edu.wvsu.landas.ui.theme.TealSoft
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.util.formatDateTime

@Composable
fun AdminDataScreen(
    isOnline: Boolean,
    pendingSync: Int,
    isSyncing: Boolean,
    lastSyncedAt: Long?,
    users: List<UserEntity>,
    attendance: List<AttendanceRecord>,
    distressSignals: List<DistressSignal>,
    alerts: List<AlertEntity>,
    announcements: List<AnnouncementEntity>,
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    classrooms: List<ClassroomEntity>,
    schedules: List<ScheduleSlotEntity>,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onExport: (android.net.Uri) -> Unit,
    onImport: (android.net.Uri) -> Unit
) {
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showDemoAccounts by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExport(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    val queuedAttendance = attendance.count { !it.synced }
    val queuedSignals = distressSignals.count { !it.synced }
    val totalRecords = users.size + attendance.size + distressSignals.size + alerts.size +
        announcements.size + nodes.size + edges.size + classrooms.size + schedules.size
    val openAreas = nodes.count { it.isEvacuationArea && !it.hazardBlocked }

    LandasScaffold(
        title = "Backup & data",
        subtitle = "Local storage, sync queue, and recovery",
        onBack = onBack,
        actions = {
            StatusPill(
                text = if (isOnline) "Online" else "Offline",
                color = if (isOnline) Success else Warning,
                background = if (isOnline) SuccessSoft else WarningSoft,
                icon = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(22.dp), color = TealDark) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(17.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Storage, contentDescription = null, tint = Gold, modifier = Modifier.size(23.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Offline-first data store",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "$totalRecords records across 9 local tables",
                                    color = Color.White.copy(alpha = 0.64f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            StatusPill(
                                text = if (pendingSync == 0) "In sync" else "$pendingSync queued",
                                color = if (pendingSync == 0) Success else Warning,
                                background = Color.White
                            )
                        }
                        Text(
                            "LANDAS keeps maps, schedules, alerts, accounts, attendance, and distress records in a Room database on this device. Backup and restore use portable JSON files that you choose through the system file picker.",
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DataStat("${attendance.size}", "Attendance", Modifier.weight(1f))
                            DataStat("${distressSignals.size}", "Signals", Modifier.weight(1f))
                            DataStat("${nodes.size}", "Map nodes", Modifier.weight(1f))
                            DataStat("${schedules.size}", "Class slots", Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                SectionHeader("Offline sync queue")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(if (pendingSync > 0) WarningSoft else SuccessSoft, RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (pendingSync > 0) Icons.Rounded.CloudSync else Icons.Rounded.CloudDone,
                                    contentDescription = null,
                                    tint = if (pendingSync > 0) Warning else Success,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (pendingSync == 0) "Queue is empty" else "$pendingSync record${if (pendingSync == 1) "" else "s"} waiting",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    lastSyncedAt?.let { "Last sync ${it.formatDateTime()}" } ?: "No sync has run in this session.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onSync, enabled = !isSyncing) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isSyncing) "Syncing…" else "Sync now")
                            }
                        }
                        HorizontalDivider()
                        QueueLine("Attendance records", queuedAttendance, attendance.size)
                        QueueLine("Distress signals", queuedSignals, distressSignals.size)
                        val syncedRatio = if (attendance.isEmpty() && distressSignals.isEmpty()) 1f else {
                            val synced = attendance.count { it.synced } + distressSignals.count { it.synced }
                            synced.toFloat() / (attendance.size + distressSignals.size)
                        }
                        LinearProgressIndicator(
                            progress = { syncedRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp),
                            color = if (syncedRatio >= 0.999f) Success else Warning,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            "This prototype marks queued records as delivered through a local routine. No network request is made and no server exists; the toggle exists to demonstrate offline-first queue behaviour.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Backup & recovery")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.Backup, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Text("Portable JSON snapshot", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "A backup includes accounts, attendance, distress history, alerts, announcements, the campus graph, classrooms, and class schedules. The exported file includes personal data, so store it securely.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { exportLauncher.launch("landas-backup-${System.currentTimeMillis()}.json") },
                                shape = RoundedCornerShape(13.dp),
                                color = TealPrimary,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 11.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Export backup", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                                shape = RoundedCornerShape(13.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 11.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = Navy, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Restore backup", color = Navy, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Surface(color = WarningSoft, shape = RoundedCornerShape(10.dp)) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
                                Text(
                                    "Restoring replaces the current dataset. Rows are upserted by ID, so a backup made earlier can reintroduce old attendance or alerts. Export the current state first if you need a rollback point.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Local database contents")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        DataRow(Icons.Rounded.Fingerprint, "Users", "${users.size}", "${users.count { it.isActive }} active")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Dataset, "Campus nodes", "${nodes.size}", "$openAreas open assembly area${if (openAreas == 1) "" else "s"}")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Dataset, "Walking paths", "${edges.size}", "${edges.count { it.hazardBlocked }} blocked")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Dataset, "Classrooms", "${classrooms.size}", "${classrooms.count { it.statusOverride != null }} manual overrides")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Dataset, "Class slots", "${schedules.size}", "Weekly timetable")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Dataset, "Alerts", "${alerts.size}", "${alerts.count { it.isActive }} active")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Dataset, "Announcements", "${announcements.size}", "Campus notices")
                        HorizontalDivider()
                        DataRow(Icons.Rounded.Storage, "Total records", "$totalRecords", "Approximate row count")
                    }
                }
            }

            item {
                SectionHeader("Data health checks")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HealthLine("All users have a unique school ID", users.map { it.schoolId }.distinct().size == users.size)
                        HealthLine("Every assembly area has a capacity", nodes.filter { it.isEvacuationArea }.all { it.capacity > 0 })
                        HealthLine("Occupancy never exceeds capacity", nodes.none { it.occupancy > it.capacity })
                        HealthLine("Route graph connects both assembly areas", edges.isNotEmpty() && nodes.count { it.isEvacuationArea } >= 2)
                        HealthLine("Every class slot belongs to a room", schedules.all { slot -> classrooms.any { it.id == slot.roomId } })
                        HealthLine("Only one alert can be active", alerts.count { it.isActive } <= 1)
                    }
                }
            }

            item {
                SectionHeader("Prototype access")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "These seeded accounts exist for evaluation. They are stored in the local database and should never be used in a real deployment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showDemoAccounts = !showDemoAccounts }) {
                            Icon(Icons.Rounded.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(if (showDemoAccounts) "Hide demo credentials" else "Show demo credentials")
                        }
                        if (showDemoAccounts) {
                            DemoCredential("Student", "2023-00142", "landas123", InfoBlue, InfoSoft)
                            DemoCredential("Faculty", "F-2019-008", "landas123", TealPrimary, TealSoft)
                            DemoCredential("Administrator", "CDRRMO-ADMIN", "admin123", Danger, DangerSoft)
                            Text(
                                "Enable the administrator portal toggle on the sign-in screen before using the admin account.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedInk
                            )
                        }
                    }
                }
            }

            item {
                Surface(color = InfoSoft, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier.padding(13.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(17.dp))
                        Column {
                            Text(
                                "Privacy reminder",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = InfoBlue
                            )
                            Text(
                                "Backups contain names, school IDs, attendance, and approximate locations. There is no remote server in this prototype, so protecting the exported file is the administrator's responsibility.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showImportConfirm && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportUri = null
            },
            icon = { Icon(Icons.Rounded.Storage, contentDescription = null, tint = Warning) },
            title = { Text("Restore this backup?") },
            text = {
                Text("The selected file will replace matching rows in the current local database. Export a backup of the present state first if you may need to undo this.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    showImportConfirm = false
                    pendingImportUri = null
                    if (uri != null) onImport(uri)
                }) {
                    Text("Restore backup", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    pendingImportUri = null
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DataStat(value: String, label: String, modifier: Modifier = Modifier) {
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
private fun QueueLine(label: String, queued: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            if (queued == 0) "All synced" else "$queued of $total queued",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (queued == 0) Success else Warning
        )
    }
}

@Composable
private fun DataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    detail: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(15.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(
                detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TealPrimary)
    }
}

@Composable
private fun HealthLine(label: String, ok: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = if (ok) Success else Warning,
            modifier = Modifier.size(16.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            if (ok) "OK" else "Check",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (ok) Success else Warning
        )
    }
}

@Composable
private fun DemoCredential(role: String, id: String, password: String, accent: Color, background: Color) {
    Surface(color = background, shape = RoundedCornerShape(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                role,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$id · $password",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
