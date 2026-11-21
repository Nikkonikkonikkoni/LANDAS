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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Tsunami
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AlertSeverity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.EmptyState
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
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
import ph.edu.wvsu.landas.util.formatRelative

@Composable
fun AlertsScreen(
    alerts: List<AlertEntity>,
    activeAlert: AlertEntity?,
    unreadCount: Int,
    isOnline: Boolean,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    var selectedAlert by remember { mutableStateOf<AlertEntity?>(null) }
    var markedRead by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!markedRead && unreadCount > 0) {
            markedRead = true
            onMarkAllRead()
        }
    }

    LandasScaffold(
        title = "Alerts & advisories",
        subtitle = "Emergency broadcasts and campus notices",
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (activeAlert != null) {
                        if (activeAlert.severity == AlertSeverity.CRITICAL) Danger else Warning
                    } else {
                        TealDark
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (activeAlert == null) Icons.Rounded.Notifications else Icons.Rounded.Emergency,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (activeAlert == null) "No active emergency" else "Active emergency broadcast",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (activeAlert == null) {
                                    "The campus is operating normally. You will be notified when an alert is issued."
                                } else {
                                    activeAlert.title
                                },
                                color = Color.White.copy(alpha = 0.76f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (activeAlert != null) {
                            Surface(
                                onClick = onOpenMap,
                                shape = CircleShape,
                                color = Color.White
                            ) {
                                Icon(
                                    Icons.Rounded.AltRoute,
                                    contentDescription = "Open evacuation map",
                                    tint = Danger,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(19.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Alert history",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (unreadCount > 0) {
                        TextButton(onClick = onMarkAllRead) {
                            Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mark all read")
                        }
                    }
                }
            }

            if (alerts.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Notifications,
                        title = "No alerts issued",
                        message = "Campus advisories and drill notices will appear here."
                    )
                }
            }

            items(alerts, key = { it.id }) { alert ->
                AlertCard(alert = alert, onClick = { selectedAlert = alert })
            }

            item {
                AppCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("How alerts work", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "LANDAS caches alerts locally so they remain readable without a connection. During a real emergency, follow official responders and posted evacuation signs over any in-app instruction.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    selectedAlert?.let { alert ->
        AlertDetailDialog(
            alert = alert,
            isActive = alert.id == activeAlert?.id,
            onDismiss = { selectedAlert = null },
            onOpenMap = {
                selectedAlert = null
                onOpenMap()
            }
        )
    }
}

@Composable
private fun AlertCard(alert: AlertEntity, onClick: () -> Unit) {
    val accent = alertAccent(alert)
    val background = alertBackground(alert)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            if (alert.isActive) 1.5.dp else 1.dp,
            if (alert.isActive) accent else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(background, RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(alertIcon(alert.type), contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            alertTypeLabel(alert.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        if (!alert.isRead) {
                            Box(Modifier.size(6.dp).background(accent, CircleShape))
                        }
                    }
                    Text(
                        alert.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (alert.isActive) {
                    StatusPill("ACTIVE", accent, background)
                }
            }
            Text(
                alert.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    alert.issuedAt.formatRelative(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "View details",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AlertDetailDialog(
    alert: AlertEntity,
    isActive: Boolean,
    onDismiss: () -> Unit,
    onOpenMap: () -> Unit
) {
    val accent = alertAccent(alert)
    val background = alertBackground(alert)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(alertIcon(alert.type), contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            alertTypeLabel(alert.type),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            alert.issuedAt.formatDateTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isActive) StatusPill("ACTIVE", accent, background)
                }
                Text(alert.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    alert.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Safety instructions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                AlertSafetySteps(alert.type).forEachIndexed { index, step ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            step,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isActive) {
                    PrimaryButton(
                        text = "Open evacuation map",
                        icon = Icons.Rounded.AltRoute,
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = accent,
                        onClick = onOpenMap
                    )
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(if (isActive) "Close" else "Dismiss")
                }
            }
        }
    }
}

private fun alertTypeLabel(type: String): String = when (type) {
    "EARTHQUAKE" -> "EARTHQUAKE"
    "FIRE" -> "FIRE EMERGENCY"
    "TYPHOON" -> "TYPHOON ADVISORY"
    "DRILL" -> "CAMPUS DRILL"
    "ALL_CLEAR" -> "ALL CLEAR"
    else -> type
}

private fun alertIcon(type: String): ImageVector = when (type) {
    "EARTHQUAKE" -> Icons.Rounded.Tsunami
    "FIRE" -> Icons.Rounded.LocalFireDepartment
    "TYPHOON" -> Icons.Rounded.WarningAmber
    "ALL_CLEAR" -> Icons.Rounded.CheckCircle
    "DRILL" -> Icons.Rounded.Campaign
    else -> Icons.Rounded.Notifications
}

private fun alertAccent(alert: AlertEntity): Color = when (alert.severity) {
    AlertSeverity.CRITICAL -> Danger
    AlertSeverity.WARNING -> Warning
    AlertSeverity.CLEAR -> Success
    else -> InfoBlue
}

private fun alertBackground(alert: AlertEntity): Color = when (alert.severity) {
    AlertSeverity.CRITICAL -> DangerSoft
    AlertSeverity.WARNING -> WarningSoft
    AlertSeverity.CLEAR -> SuccessSoft
    else -> InfoSoft
}

private fun AlertSafetySteps(type: String): List<String> = when (type) {
    "EARTHQUAKE" -> listOf(
        "Drop, cover, and hold until the shaking stops.",
        "Check for injuries and hazards before moving.",
        "Evacuate using the nearest safe route once it is clear.",
        "Proceed to an open assembly area and mark yourself safe.",
        "Do not re-enter buildings until an all-clear is issued."
    )

    "FIRE" -> listOf(
        "Activate the nearest fire alarm.",
        "Leave the building immediately using the stairs.",
        "Stay low if there is smoke and avoid elevator use.",
        "Call the Pototan Fire Station or 911.",
        "Move away from the building and wait for responders."
    )

    "TYPHOON" -> listOf(
        "Move indoors and away from windows.",
        "Secure loose objects and prepare a go-bag.",
        "Monitor official weather advisories.",
        "Follow evacuation instructions from the MDRRMO.",
        "Report emergencies through the distress module or hotline."
    )

    "ALL_CLEAR" -> listOf(
        "Continue to follow official instructions.",
        "Report injuries or damage to campus responders.",
        "Return to normal operations only when advised.",
        "Keep emergency contacts and supplies ready."
    )

    else -> listOf(
        "Stay calm and assess your surroundings.",
        "Follow instructions from official responders.",
        "Use the evacuation map if you need to relocate.",
        "Call 911 for immediate danger."
    )
}
