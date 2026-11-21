package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Storm
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tsunami
import androidx.compose.material.icons.rounded.WarningAmber
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDateTime
import java.time.ZoneId
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AlertSeverity
import ph.edu.wvsu.landas.data.AnnouncementEntity
import ph.edu.wvsu.landas.ui.components.AppCard
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
import ph.edu.wvsu.landas.util.formatDateTime

private data class AlertTemplate(
    val type: String,
    val label: String,
    val severity: String,
    val defaultTitle: String,
    val defaultBody: String
)

private val alertTemplates = listOf(
    AlertTemplate(
        "EARTHQUAKE",
        "Earthquake",
        AlertSeverity.CRITICAL,
        "Earthquake detected — evacuate immediately",
        "A strong earthquake has been felt on campus. Drop, cover, and hold. Once the shaking stops, evacuate using the nearest safe route and proceed to an open assembly area. Avoid damaged buildings and wait for an all-clear."
    ),
    AlertTemplate(
        "FIRE",
        "Fire",
        AlertSeverity.CRITICAL,
        "Fire emergency — avoid the affected building",
        "A fire has been reported on campus. Evacuate the affected building immediately, use the stairs, and keep clear of access roads for responders. Proceed to your designated assembly area and mark yourself safe."
    ),
    AlertTemplate(
        "TYPHOON",
        "Typhoon",
        AlertSeverity.WARNING,
        "Typhoon advisory — prepare to shelter",
        "A typhoon advisory is in effect for Pototan. Secure loose objects, avoid windows, and prepare essential supplies. Classes and campus operations may be suspended. Monitor official advisories and await further instructions."
    ),
    AlertTemplate(
        "DRILL",
        "Drill",
        AlertSeverity.INFO,
        "Campus earthquake drill — participate now",
        "This is a scheduled earthquake drill. Practice drop, cover, and hold, then evacuate using the LANDAS map to your assigned assembly area. Mark yourself safe to help the campus validate its accountability process."
    )
)

private fun drillTriggerAt(option: Int): Long {
    val now = System.currentTimeMillis()
    return when (option) {
        0 -> now + 60_000L
        1 -> now + 5 * 60_000L
        2 -> now + 15 * 60_000L
        else -> LocalDateTime.now()
            .plusDays(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}

@Composable
fun AdminAlertsScreen(
    alerts: List<AlertEntity>,
    announcements: List<AnnouncementEntity>,
    activeAlert: AlertEntity?,
    scheduledDrillAt: Long?,
    onBack: () -> Unit,
    onPublishAlert: (String, String, String, String) -> Unit,
    onSendAllClear: (String, String) -> Unit,
    onAddAnnouncement: (String, String, String) -> Unit,
    onScheduleDrill: (Long) -> Unit,
    onCancelDrill: () -> Unit
) {
    var selectedType by remember { mutableStateOf(alertTemplates.first()) }
    var title by remember { mutableStateOf(alertTemplates.first().defaultTitle) }
    var body by remember { mutableStateOf(alertTemplates.first().defaultBody) }
    var severity by remember { mutableStateOf(alertTemplates.first().severity) }
    var showPublishConfirm by remember { mutableStateOf(false) }
    var showAllClear by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var drillOption by remember { mutableStateOf(1) }

    val upcomingDrill = scheduledDrillAt?.takeIf { it > System.currentTimeMillis() }

    fun applyTemplate(template: AlertTemplate) {
        selectedType = template
        title = template.defaultTitle
        body = template.defaultBody
        severity = template.severity
    }

    LandasScaffold(
        title = "Alerts & notices",
        subtitle = "Broadcast emergencies and campus announcements",
        onBack = onBack,
        actions = {
            StatusPill(
                text = if (activeAlert == null) "No active alert" else "Alert active",
                color = if (activeAlert == null) Success else Danger,
                background = if (activeAlert == null) SuccessSoft else DangerSoft,
                icon = if (activeAlert == null) Icons.Rounded.CheckCircle else Icons.Rounded.Emergency
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeAlert == null) TealDark else Danger
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (activeAlert == null) Icons.Rounded.NotificationsActive else Icons.Rounded.Emergency,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (activeAlert == null) "Broadcast channel ready" else "Active emergency broadcast",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (activeAlert != null) {
                            Text(
                                activeAlert.title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Issued ${activeAlert.issuedAt.formatDateTime()} · ${activeAlert.severity}",
                                color = Color.White.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            Surface(
                                onClick = { showAllClear = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(17.dp))
                                    Text(
                                        "Stand down · send all-clear",
                                        color = Navy,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                "Publishing an alert immediately activates the campus emergency banner and triggers a local notification on this device. Students see it on their dashboards after their next data refresh.",
                                color = Color.White.copy(alpha = 0.74f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Compose emergency alert")
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Alert type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            alertTemplates.chunked(2).forEach { rowTemplates ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    rowTemplates.forEach { template ->
                                        FilterChip(
                                            selected = selectedType.type == template.type,
                                            onClick = { applyTemplate(template) },
                                            label = {
                                                Text(
                                                    template.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    alertTypeIcon(template.type),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            },
                                            shape = RoundedCornerShape(50),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = TealPrimary,
                                                selectedLabelColor = Color.White,
                                                selectedLeadingIconColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Text("Severity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                AlertSeverity.CRITICAL,
                                AlertSeverity.WARNING,
                                AlertSeverity.INFO
                            ).forEach { option ->
                                FilterChip(
                                    selected = severity == option,
                                    onClick = { severity = option },
                                    label = { Text(option.lowercase().replaceFirstChar { it.uppercase() }) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Alert headline") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = body,
                            onValueChange = { body = it },
                            label = { Text("Message and instructions") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                        AnimatedVisibility(visible = severity == AlertSeverity.CRITICAL) {
                            Surface(color = DangerSoft, shape = RoundedCornerShape(10.dp)) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Danger, modifier = Modifier.size(16.dp))
                                    Text(
                                        "Critical alerts replace the current active alert and are shown in red across student dashboards.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        PrimaryButton(
                            text = "Review & publish alert",
                            icon = Icons.Rounded.Send,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = title.isNotBlank() && body.isNotBlank(),
                            containerColor = Danger,
                            onClick = { showPublishConfirm = true }
                        )
                    }
                }
            }

            item {
                SectionHeader("Scheduled drill")
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarningSoft
                            ) {
                                Icon(
                                    Icons.Rounded.Timer,
                                    contentDescription = null,
                                    tint = Warning,
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Drill automation",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "WorkManager delivers one drill reminder on this device.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (upcomingDrill != null) {
                            Surface(color = InfoSoft, shape = RoundedCornerShape(10.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Drill queued",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = InfoBlue
                                        )
                                        Text(
                                            upcomingDrill.formatDateTime(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = onCancelDrill) {
                                        Text("Cancel", color = Danger, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            Text(
                                "When it fires, LANDAS publishes an active drill alert and posts a notification. Timings are inexact and may be delayed by battery optimizations.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "Choose when the campus should practise its earthquake response. The drill appears in alert history and triggers the same banner as a manually published drill.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val options = listOf("In 1 min", "In 5 min", "In 15 min", "Tomorrow 9 AM")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                options.chunked(2).forEachIndexed { rowIndex, rowOptions ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        rowOptions.forEachIndexed { columnIndex, option ->
                                            val index = rowIndex * 2 + columnIndex
                                            FilterChip(
                                                selected = drillOption == index,
                                                onClick = { drillOption = index },
                                                label = {
                                                    Text(
                                                        option,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        maxLines = 1
                                                    )
                                                },
                                                shape = RoundedCornerShape(50)
                                            )
                                        }
                                    }
                                }
                            }
                            PrimaryButton(
                                text = "Schedule drill",
                                icon = Icons.Rounded.Timer,
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Warning,
                                onClick = { onScheduleDrill(drillTriggerAt(drillOption)) }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Alert history")
            }
            if (alerts.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            "No alerts have been issued yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }
            }
            items(alerts, key = { it.id }) { alert ->
                AlertHistoryRow(alert = alert)
            }

            item {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("General announcements", modifier = Modifier.weight(1f))
                    TextButton(onClick = { showAnnouncementDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New")
                    }
                }
            }
            items(announcements.take(8), key = { it.id }) { announcement ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusPill(
                                announcement.category,
                                TealPrimary,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                announcement.timestamp.formatDateTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedInk
                            )
                        }
                        Text(announcement.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            announcement.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            if (announcements.isEmpty()) {
                item {
                    AppCard {
                        Text(
                            "No campus announcements yet. Use “New” to publish one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(15.dp)
                        )
                    }
                }
            }
        }
    }

    if (showPublishConfirm) {
        AlertDialog(
            onDismissRequest = { showPublishConfirm = false },
            icon = {
                Icon(
                    alertTypeIcon(selectedType.type),
                    contentDescription = null,
                    tint = if (severity == AlertSeverity.CRITICAL) Danger else Warning
                )
            },
            title = { Text("Publish this ${selectedType.label.lowercase()} alert?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(body, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "This replaces any active alert and marks every student dashboard with the emergency banner.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Danger
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPublishConfirm = false
                    onPublishAlert(selectedType.type, title.trim(), body.trim(), severity)
                }) {
                    Text("Publish now", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showAllClear) {
        AlertDialog(
            onDismissRequest = { showAllClear = false },
            icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Success) },
            title = { Text("Send all-clear?") },
            text = {
                Text("The active emergency banner will be removed and students will be informed that it is safe to resume normal operations.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showAllClear = false
                    onSendAllClear(
                        "All clear — campus operations resume",
                        "Campus responders have declared the situation safe. Resume normal operations, report any injuries or damage, and continue to follow official guidance."
                    )
                }) {
                    Text("Send all-clear", color = Success, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAllClear = false }) { Text("Cancel") }
            }
        )
    }

    if (showAnnouncementDialog) {
        AnnouncementDialog(
            onDismiss = { showAnnouncementDialog = false },
            onPublish = { announcementTitle, announcementBody, category ->
                onAddAnnouncement(announcementTitle, announcementBody, category)
                showAnnouncementDialog = false
            }
        )
    }
}

@Composable
private fun AlertHistoryRow(alert: AlertEntity) {
    val accent = when (alert.severity) {
        AlertSeverity.CRITICAL -> Danger
        AlertSeverity.WARNING -> Warning
        AlertSeverity.CLEAR -> Success
        else -> InfoBlue
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(alertTypeIcon(alert.type), contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    alert.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${alert.type} · ${alert.issuedAt.formatDateTime()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(
                text = if (alert.isActive) "Active" else alert.severity,
                color = accent,
                background = accent.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
private fun AnnouncementDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }
    val categories = listOf("GENERAL", "ACADEMIC", "SAFETY", "EVENT")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 30.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("New announcement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Visible on every campus dashboard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
                Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
                PrimaryButton(
                    text = "Publish announcement",
                    icon = Icons.Rounded.Campaign,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && body.isNotBlank(),
                    onClick = { onPublish(title.trim(), body.trim(), category) }
                )
            }
        }
    }
}

private fun alertTypeIcon(type: String) = when (type) {
    "EARTHQUAKE" -> Icons.Rounded.Tsunami
    "FIRE" -> Icons.Rounded.LocalFireDepartment
    "TYPHOON" -> Icons.Rounded.Storm
    "DRILL" -> Icons.Rounded.Campaign
    "ALL_CLEAR" -> Icons.Rounded.CheckCircle
    else -> Icons.Rounded.Info
}
