package ph.edu.wvsu.landas.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.DistressStatus
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.EmptyState
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
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.util.formatDateTime
import ph.edu.wvsu.landas.util.formatRelative

private enum class DistressFilter(val label: String) {
    ACTIVE("Active"),
    ACKNOWLEDGED("Acknowledged"),
    RESOLVED("Resolved"),
    ALL("All signals")
}

@Composable
fun AdminDistressScreen(
    signals: List<DistressSignal>,
    nodes: List<CampusNodeEntity>,
    onBack: () -> Unit,
    onAcknowledge: (String) -> Unit,
    onResolve: (String) -> Unit,
    onOpenMap: () -> Unit
) {
    var filter by remember { mutableStateOf(DistressFilter.ACTIVE) }
    var resolving by remember { mutableStateOf<DistressSignal?>(null) }

    val active = signals.filter { it.status == DistressStatus.ACTIVE }
    val acknowledged = signals.filter { it.status == DistressStatus.ACKNOWLEDGED }
    val resolved = signals.filter { it.status == DistressStatus.RESOLVED }
    val open = signals.filter { it.status != DistressStatus.RESOLVED }

    val filtered = when (filter) {
        DistressFilter.ACTIVE -> active
        DistressFilter.ACKNOWLEDGED -> acknowledged
        DistressFilter.RESOLVED -> resolved
        DistressFilter.ALL -> signals
    }.sortedByDescending { it.timestamp }

    val nearestArea = { signal: DistressSignal ->
        nodes.filter { it.isEvacuationArea }
            .minByOrNull { node ->
                val dx = node.x - signal.mapX
                val dy = node.y - signal.mapY
                dx * dx + dy * dy
            }
    }

    LandasScaffold(
        title = "Distress response",
        subtitle = "Monitor, acknowledge, and resolve SOS signals",
        onBack = onBack,
        actions = {
            StatusPill(
                text = if (open.isEmpty()) "No open SOS" else "${open.size} open",
                color = if (open.isEmpty()) Success else Danger,
                background = if (open.isEmpty()) SuccessSoft else DangerSoft,
                icon = if (open.isEmpty()) Icons.Rounded.CheckCircle else Icons.Rounded.Sos
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
                    shape = RoundedCornerShape(22.dp),
                    color = if (open.isEmpty()) TealDark else Danger
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
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (open.isEmpty()) Icons.Rounded.NotificationsActive else Icons.Rounded.Sos,
                                    contentDescription = null,
                                    tint = if (open.isEmpty()) Gold else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (open.isEmpty()) "No open distress signals" else "$open active request${if (open.size == 1) "" else "s"}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (open.isEmpty()) {
                                        "The response channel is clear. Signals appear here the moment a student or faculty member sends one."
                                    } else {
                                        "Acknowledge a signal to let the sender know help is on the way, then resolve it once responders reach the pin."
                                    },
                                    color = Color.White.copy(alpha = 0.72f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DistressStat("${active.size}", "Active", Color.White, Color.White.copy(alpha = 0.12f), Modifier.weight(1f))
                            DistressStat("${acknowledged.size}", "Acknowledged", Color.White, Color.White.copy(alpha = 0.12f), Modifier.weight(1f))
                            DistressStat("${resolved.size}", "Resolved", Color.White, Color.White.copy(alpha = 0.12f), Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                SectionHeader(
                    title = "Signal map",
                    actionText = "Open map",
                    onAction = onOpenMap
                )
                AppCard {
                    SignalMap(
                        nodes = nodes,
                        signals = signals,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(196.dp)
                            .padding(6.dp)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    DistressFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = {
                                Text(
                                    if (option == DistressFilter.ALL) option.label else "${option.label} (${when (option) {
                                        DistressFilter.ACTIVE -> active.size
                                        DistressFilter.ACKNOWLEDGED -> acknowledged.size
                                        DistressFilter.RESOLVED -> resolved.size
                                        DistressFilter.ALL -> signals.size
                                    }})",
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

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = if (filter == DistressFilter.ACTIVE) Icons.Rounded.CheckCircle else Icons.Rounded.Sos,
                        title = if (filter == DistressFilter.ACTIVE) "No active distress signals" else "Nothing here yet",
                        message = if (filter == DistressFilter.ACTIVE) {
                            "When someone sends an SOS, their identity, pin, and message will appear here for responder action."
                        } else {
                            "No signals match this filter."
                        }
                    )
                }
            }

            items(filtered, key = { it.id }) { signal ->
                DistressCard(
                    signal = signal,
                    nearestAreaName = nearestArea(signal)?.name,
                    onAcknowledge = { onAcknowledge(signal.id) },
                    onResolve = { resolving = signal }
                )
            }

            if (signals.isEmpty()) {
                item {
                    AppCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(28.dp))
                            Text(
                                "Distress monitoring is ready",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Students and faculty can trigger SOS from their dashboard. Each signal stores the sender, an approximate GPS or manual pin, the nearest landmark, and their message. This prototype stores everything locally, so signals are visible only on this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                Surface(color = InfoSoft, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(17.dp))
                        Text(
                            "Prototype note: signals are recorded in the local database only. A production build would forward them to CDRRMO with authenticated transport, delivery receipts, and dispatch integration.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    resolving?.let { signal ->
        AlertDialog(
            onDismissRequest = { resolving = null },
            icon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Success) },
            title = { Text("Resolve this signal?") },
            text = {
                Text(
                    "Mark ${signal.fullName}'s request as resolved. Only do this after responders have reached the pinned location or the sender has confirmed they are safe."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = signal.id
                    resolving = null
                    onResolve(id)
                }) {
                    Text("Mark resolved", color = Success, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { resolving = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DistressCard(
    signal: DistressSignal,
    nearestAreaName: String?,
    onAcknowledge: () -> Unit,
    onResolve: () -> Unit
) {
    val accent = when (signal.status) {
        DistressStatus.ACTIVE -> Danger
        DistressStatus.ACKNOWLEDGED -> Warning
        else -> Success
    }
    val background = when (signal.status) {
        DistressStatus.ACTIVE -> DangerSoft
        DistressStatus.ACKNOWLEDGED -> WarningSoft
        else -> SuccessSoft
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (signal.status == DistressStatus.RESOLVED) Icons.Rounded.CheckCircle else Icons.Rounded.Sos,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(signal.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Person, contentDescription = null, tint = MutedInk, modifier = Modifier.size(12.dp))
                        Text(
                            signal.userId,
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                StatusPill(
                    text = when (signal.status) {
                        DistressStatus.ACTIVE -> "Active"
                        DistressStatus.ACKNOWLEDGED -> "Acknowledged"
                        else -> "Resolved"
                    },
                    color = accent,
                    background = background
                )
            }

            Surface(color = background.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp)) {
                Text(
                    signal.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = Navy,
                    modifier = Modifier.padding(11.dp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        signal.nearestLocation,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Map pin (${signal.mapX.toInt()}, ${signal.mapY.toInt()}) · %.5f, %.5f".format(signal.latitude, signal.longitude),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (nearestAreaName != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Nearest area", style = MaterialTheme.typography.labelSmall, color = MutedInk)
                        Text(
                            nearestAreaName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = MutedInk, modifier = Modifier.size(14.dp))
                Text(
                    "${signal.timestamp.formatDateTime()} · ${signal.timestamp.formatRelative()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = if (signal.synced) "Synced" else "Queued",
                    color = if (signal.synced) Success else Warning,
                    background = if (signal.synced) SuccessSoft else WarningSoft
                )
            }

            if (signal.status != DistressStatus.RESOLVED) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (signal.status == DistressStatus.ACTIVE) {
                        Surface(
                            onClick = onAcknowledge,
                            shape = RoundedCornerShape(12.dp),
                            color = Warning,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Acknowledge", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Surface(
                        onClick = onResolve,
                        shape = RoundedCornerShape(12.dp),
                        color = Success,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Resolve", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalMap(
    nodes: List<CampusNodeEntity>,
    signals: List<DistressSignal>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(16.dp))) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val scaleX = size.width / 1000f
            val scaleY = size.height / 760f
            val scale = minOf(scaleX, scaleY)
            val offsetX = (size.width - 1000f * scale) / 2f
            val offsetY = (size.height - 760f * scale) / 2f

            nodes.filter { it.kind != NodeKind.JUNCTION }.forEach { node ->
                val topLeft = Offset(
                    x = offsetX + (node.x - node.width / 2f) * scale,
                    y = offsetY + (node.y - node.height / 2f) * scale
                )
                drawRoundRect(
                    color = if (node.isEvacuationArea) Success.copy(alpha = 0.35f) else Color(0xFFB9C6C4),
                    topLeft = topLeft,
                    size = Size((node.width * scale).coerceAtLeast(2f), (node.height * scale).coerceAtLeast(2f)),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            signals.forEach { signal ->
                val point = Offset(offsetX + signal.mapX * scale, offsetY + signal.mapY * scale)
                val color = when (signal.status) {
                    DistressStatus.ACTIVE -> Danger
                    DistressStatus.ACKNOWLEDGED -> Warning
                    else -> Success
                }
                if (signal.status == DistressStatus.ACTIVE) {
                    drawCircle(color = color.copy(alpha = 0.22f), radius = 15f, center = point)
                }
                drawCircle(color = color.copy(alpha = 0.35f), radius = 9f, center = point)
                drawCircle(color = color, radius = 5f, center = point)
                drawCircle(color = Color.White, radius = 2f, center = point)
            }
        }
        if (signals.isEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.GpsFixed, contentDescription = null, tint = MutedInk, modifier = Modifier.size(15.dp))
                    Text(
                        "No signal pins to display",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            MapLegendDot(Danger, "Active")
            MapLegendDot(Warning, "Acknowledged")
            MapLegendDot(Success, "Resolved")
        }
    }
}

@Composable
private fun MapLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MutedInk)
    }
}

@Composable
private fun DistressStat(value: String, label: String, valueColor: Color, background: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = background, shape = RoundedCornerShape(10.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = valueColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, color = valueColor.copy(alpha = 0.62f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
