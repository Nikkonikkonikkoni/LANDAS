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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsBasketball
import androidx.compose.material.icons.rounded.Wc
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.SectionHeader
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.map.CampusMap
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

@Composable
fun CampusGuideScreen(
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    visitorMode: Boolean,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenHotline: () -> Unit,
    onOpenQr: () -> Unit
) {
    val buildings = nodes
        .filter { !it.isEvacuationArea && it.kind != NodeKind.JUNCTION }
        .sortedWith(compareBy({ kindOrder(it.kind) }, { it.name }))
    val evacuationAreas = nodes.filter { it.isEvacuationArea }

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
                    .background(TealDark)
                    .padding(horizontal = 18.dp, vertical = 52.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = onBack,
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.13f)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(9.dp))
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = Gold, modifier = Modifier.size(25.dp))
                        }
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text(
                                "WVSU – Pototan Campus",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Campus guide & visitor portal",
                                color = Color.White.copy(alpha = 0.66f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Text(
                        if (visitorMode) "Welcome, visitor." else "Know your campus.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "West Visayas State University's Pototan campus has served the town and nearby communities since its post-war refresher classes in 1945. Use this guide to find offices, check safety information, and navigate the grounds offline.",
                        color = Color.White.copy(alpha = 0.74f),
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 19.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        GuideBadge(Icons.Rounded.Map, "Offline map")
                        GuideBadge(Icons.Rounded.Shield, "Safety ready")
                        GuideBadge(Icons.Rounded.Wc, "Visitor friendly")
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (visitorMode) {
                    Surface(
                        color = InfoSoft,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(13.dp),
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Rounded.Badge, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(19.dp))
                            Column {
                                Text(
                                    "Visiting the campus?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy
                                )
                                Text(
                                    "Register at the guard house, present a valid ID, and secure a visitor pass. In an emergency, follow responder instructions and proceed to the nearest assembly area.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                CampusMap(
                    nodes = nodes,
                    edges = edges,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    PrimaryButton(
                        text = "Evacuation route",
                        icon = Icons.Rounded.AltRoute,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenMap
                    )
                    Surface(
                        onClick = onOpenHotline,
                        shape = RoundedCornerShape(15.dp),
                        color = SafetyOrange.copy(alpha = 0.16f),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Phone, contentDescription = null, tint = SafetyOrange, modifier = Modifier.size(18.dp))
                            Text(
                                "Hotlines",
                                style = MaterialTheme.typography.labelLarge,
                                color = Navy,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Surface(
                    onClick = onOpenQr,
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(TealSoft, RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Visitor QR & official links", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Generate a visitor code or scan campus QR signage.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                    }
                }

                SectionHeader("Campus directory")
                Text(
                    "Tap a location to see its description. Distances shown on the map are walking estimates from the prototype graph.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(buildings, key = { it.id }) { node ->
            DirectoryRow(node = node)
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader("Assembly areas")
                evacuationAreas.forEach { area ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (area.hazardBlocked) MaterialTheme.colorScheme.errorContainer else SuccessSoft
                    ) {
                        Row(
                            modifier = Modifier.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Flag,
                                contentDescription = null,
                                tint = if (area.hazardBlocked) MaterialTheme.colorScheme.error else Success,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(area.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    area.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusPill(
                                if (area.hazardBlocked) "Closed" else "Open",
                                if (area.hazardBlocked) MaterialTheme.colorScheme.error else Success,
                                if (area.hazardBlocked) MaterialTheme.colorScheme.errorContainer else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Campaign, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Visitor reminders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        listOf(
                            "Wear your visitor pass at all times.",
                            "Do not enter laboratories or offices without staff assistance.",
                            "Keep pathways clear and follow posted signs.",
                            "In an emergency, call 911 or approach campus security."
                        ).forEach { reminder ->
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(15.dp))
                                Text(
                                    reminder,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = WarningSoft,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = Warning, modifier = Modifier.size(17.dp))
                        Text(
                            "Campus geometry, office locations, and contact details are prototype data. Confirm them with the campus administration before relying on them for a real visit.",
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
private fun DirectoryRow(node: CampusNodeEntity) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(15.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(nodeAccent(node.kind).copy(alpha = 0.12f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(nodeIcon(node.kind), contentDescription = null, tint = nodeAccent(node.kind), modifier = Modifier.size(19.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        node.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (node.hazardBlocked) {
                        StatusPill("Closed", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
                    }
                }
                Text(
                    node.description.ifBlank {
                        when (node.kind) {
                            NodeKind.GATE -> "Campus entrance and security checkpoint"
                            NodeKind.FACILITY -> "Campus facility"
                            else -> "Campus building"
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) 4 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (expanded && node.buildingCode.isNotBlank()) {
                    Text(
                        "Building code: ${node.buildingCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = MutedInk,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun GuideBadge(icon: ImageVector, label: String) {
    Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.74f), modifier = Modifier.size(12.dp))
            Text(label, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun kindOrder(kind: String): Int = when (kind) {
    NodeKind.GATE -> 0
    NodeKind.BUILDING -> 1
    NodeKind.FACILITY -> 2
    NodeKind.LANDMARK -> 3
    else -> 4
}

private fun nodeIcon(kind: String): ImageVector = when (kind) {
    NodeKind.GATE -> Icons.Rounded.MeetingRoom
    NodeKind.FACILITY -> Icons.Rounded.SportsBasketball
    NodeKind.LANDMARK -> Icons.Rounded.Explore
    else -> Icons.Rounded.AccountBalance
}

private fun nodeAccent(kind: String): Color = when (kind) {
    NodeKind.GATE -> SafetyOrange
    NodeKind.FACILITY -> InfoBlue
    NodeKind.LANDMARK -> Warning
    else -> TealPrimary
}
