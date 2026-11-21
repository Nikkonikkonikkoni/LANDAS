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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.hypot
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.EmptyState
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.map.CampusMap
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

private enum class RouteTab(val label: String) {
    LOCATIONS("Locations"),
    PATHS("Paths")
}

@Composable
fun AdminRoutesScreen(
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    onBack: () -> Unit,
    onSaveNode: (String?, String, String, Float, Float, String, String, Boolean, Int, Int, Float, Float, String) -> Unit,
    onDeleteNode: (String) -> Unit,
    onToggleNodeHazard: (String, Boolean) -> Unit,
    onSetAreaOccupancy: (String, Int) -> Unit,
    onAddEdge: (String, String) -> Unit,
    onDeleteEdge: (String) -> Unit,
    onToggleEdgeHazard: (String, Boolean) -> Unit
) {
    var tab by remember { mutableStateOf(RouteTab.LOCATIONS) }
    var query by remember { mutableStateOf("") }
    var editingNode by remember { mutableStateOf<CampusNodeEntity?>(null) }
    var creatingAt by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var deletingNode by remember { mutableStateOf<CampusNodeEntity?>(null) }
    var showAddPath by remember { mutableStateOf(false) }
    var deletingEdge by remember { mutableStateOf<CampusEdgeEntity?>(null) }

    val nodeMap = remember(nodes) { nodes.associateBy { it.id } }
    val blockedNodes = nodes.count { it.hazardBlocked }
    val blockedEdges = edges.count { it.hazardBlocked }

    val filteredNodes = nodes
        .filter { it.kind != NodeKind.JUNCTION }
        .filter {
            query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.shortName.contains(query, ignoreCase = true) ||
                it.buildingCode.contains(query, ignoreCase = true)
        }

    LandasScaffold(
        title = "Routes & hazards",
        subtitle = "Campus graph, capacities, and safe paths",
        onBack = onBack,
        actions = {
            StatusPill(
                text = "${blockedNodes + blockedEdges} blocked",
                color = if (blockedNodes + blockedEdges == 0) Success else Danger,
                background = if (blockedNodes + blockedEdges == 0) SuccessSoft else DangerSoft,
                icon = Icons.Rounded.Block
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
                CampusMap(
                    nodes = nodes,
                    edges = edges,
                    debugMode = true,
                    onNodeTap = { node ->
                        if (node.kind != NodeKind.JUNCTION) editingNode = node
                    },
                    onMapTap = { x, y -> creatingAt = x to y },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
            }

            item {
                Text(
                    "Tap a building to edit it, or tap empty ground to add a new location. Blocked elements appear with a red X and are avoided by evacuation routing.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RouteTab.entries.forEach { option ->
                        FilterChip(
                            selected = tab == option,
                            onClick = { tab = option },
                            label = { Text(option.label) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Surface(
                        onClick = {
                            if (tab == RouteTab.LOCATIONS) {
                                creatingAt = 500f to 380f
                            } else {
                                showAddPath = true
                            }
                        },
                        shape = RoundedCornerShape(50),
                        color = TealPrimary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(
                                if (tab == RouteTab.LOCATIONS) "Add location" else "Add path",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (tab == RouteTab.LOCATIONS) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search campus locations") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (filteredNodes.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.LocationOn,
                            title = "No locations found",
                            message = "Adjust your search or tap the map to add a new campus location."
                        )
                    }
                }

                items(filteredNodes, key = { it.id }) { node ->
                    NodeAdminCard(
                        node = node,
                        onEdit = { editingNode = node },
                        onDelete = { deletingNode = node },
                        onToggleHazard = { blocked -> onToggleNodeHazard(node.id, blocked) },
                        onOccupancyChange = { onSetAreaOccupancy(node.id, it) }
                    )
                }

                item {
                    Surface(color = WarningSoft, shape = RoundedCornerShape(14.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(17.dp))
                            Text(
                                "Blocking a node removes it from every computed route. Blocking a path removes only that connection. Changes apply immediately to student maps.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PathStat("${edges.size}", "Total paths", TealPrimary, Modifier.weight(1f))
                        PathStat("${edges.count { it.hazardBlocked }}", "Blocked", Danger, Modifier.weight(1f))
                        PathStat("${edges.sumOf { it.distanceMeters.toInt() } / edges.size.coerceAtLeast(1)} m", "Avg length", InfoBlue, Modifier.weight(1f))
                    }
                }
                items(edges, key = { it.id }) { edge ->
                    val from = nodeMap[edge.fromNodeId]
                    val to = nodeMap[edge.toNodeId]
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (edge.hazardBlocked) Danger.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (edge.hazardBlocked) DangerSoft else MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(9.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.Link, contentDescription = null, tint = if (edge.hazardBlocked) Danger else TealPrimary, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(9.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${from?.shortName ?: edge.fromNodeId} ↔ ${to?.shortName ?: edge.toNodeId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${edge.distanceMeters.toInt()} m walking distance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (edge.hazardBlocked) {
                                    StatusPill("Blocked", Danger, DangerSoft)
                                }
                                Switch(
                                    checked = edge.hazardBlocked,
                                    onCheckedChange = { onToggleEdgeHazard(edge.id, it) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Danger)
                                )
                                IconButton(onClick = { deletingEdge = edge }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete path", tint = MutedInk, modifier = Modifier.size(19.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingNode != null || creatingAt != null) {
        NodeEditorDialog(
            node = editingNode,
            initialX = creatingAt?.first ?: 500f,
            initialY = creatingAt?.second ?: 380f,
            onDismiss = {
                editingNode = null
                creatingAt = null
            },
            onSave = { id, name, short, x, y, kind, code, evacuation, capacity, occupancy, width, height, description ->
                onSaveNode(id, name, short, x, y, kind, code, evacuation, capacity, occupancy, width, height, description)
                editingNode = null
                creatingAt = null
            }
        )
    }

    deletingNode?.let { node ->
        AlertDialog(
            onDismissRequest = { deletingNode = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Danger) },
            title = { Text("Delete ${node.name}?") },
            text = { Text("This also deletes every walking path connected to this location. Routes will be recalculated immediately.") },
            confirmButton = {
                TextButton(onClick = {
                    deletingNode = null
                    onDeleteNode(node.id)
                }) {
                    Text("Delete location", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNode = null }) { Text("Cancel") }
            }
        )
    }

    deletingEdge?.let { edge ->
        val from = nodeMap[edge.fromNodeId]
        val to = nodeMap[edge.toNodeId]
        AlertDialog(
            onDismissRequest = { deletingEdge = null },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = Danger) },
            title = { Text("Delete this path?") },
            text = { Text("The connection between ${from?.name ?: edge.fromNodeId} and ${to?.name ?: edge.toNodeId} will be removed from the routing graph.") },
            confirmButton = {
                TextButton(onClick = {
                    deletingEdge = null
                    onDeleteEdge(edge.id)
                }) {
                    Text("Delete path", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEdge = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddPath) {
        AddPathDialog(
            nodes = nodes.filter { it.kind != NodeKind.JUNCTION },
            onDismiss = { showAddPath = false },
            onAdd = { fromId, toId ->
                onAddEdge(fromId, toId)
                showAddPath = false
            }
        )
    }
}

@Composable
private fun NodeAdminCard(
    node: CampusNodeEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleHazard: (Boolean) -> Unit,
    onOccupancyChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (node.hazardBlocked) Danger.copy(alpha = 0.45f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (node.isEvacuationArea) SuccessSoft else MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (node.isEvacuationArea) Icons.Rounded.Flag else Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = if (node.isEvacuationArea) Success else TealPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        node.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${node.kind.lowercase().replaceFirstChar { it.uppercase() }}${if (node.buildingCode.isNotBlank()) " · ${node.buildingCode}" else ""} · (${node.x.toInt()}, ${node.y.toInt()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit ${node.name}", tint = TealPrimary, modifier = Modifier.size(19.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete ${node.name}", tint = MutedInk, modifier = Modifier.size(19.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (node.hazardBlocked) "Blocked from routing" else "Available for routing",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (node.hazardBlocked) Danger else Success,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = node.hazardBlocked,
                    onCheckedChange = onToggleHazard,
                    colors = SwitchDefaults.colors(checkedTrackColor = Danger)
                )
            }

            if (node.isEvacuationArea) {
                HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Assembly area occupancy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${node.occupancy} of ${node.capacity} persons · ${(node.capacity - node.occupancy).coerceAtLeast(0)} spaces open",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { onOccupancyChange((node.occupancy - 1).coerceAtLeast(0)) },
                        modifier = Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease occupancy", modifier = Modifier.size(15.dp))
                    }
                    Text(
                        "${node.occupancy}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    IconButton(
                        onClick = { onOccupancyChange((node.occupancy + 1).coerceAtMost(node.capacity)) },
                        modifier = Modifier
                            .size(30.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Increase occupancy", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PathStat(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = accent.copy(alpha = 0.1f),
        shape = RoundedCornerShape(13.dp)
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NodeEditorDialog(
    node: CampusNodeEntity?,
    initialX: Float,
    initialY: Float,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, Float, Float, String, String, Boolean, Int, Int, Float, Float, String) -> Unit
) {
    var name by remember(node) { mutableStateOf(node?.name.orEmpty()) }
    var shortName by remember(node) { mutableStateOf(node?.shortName.orEmpty()) }
    var kind by remember(node) { mutableStateOf(node?.kind ?: NodeKind.BUILDING) }
    var buildingCode by remember(node) { mutableStateOf(node?.buildingCode.orEmpty()) }
    var description by remember(node) { mutableStateOf(node?.description.orEmpty()) }
    var isEvacuation by remember(node) { mutableStateOf(node?.isEvacuationArea ?: false) }
    var capacity by remember(node) { mutableStateOf((node?.capacity ?: 0).toString()) }
    var occupancy by remember(node) { mutableStateOf((node?.occupancy ?: 0).toString()) }
    var width by remember(node) { mutableStateOf((node?.width ?: 90f).toString()) }
    var height by remember(node) { mutableStateOf((node?.height ?: 60f).toString()) }
    var x by remember(node) { mutableStateOf((node?.x ?: initialX).toString()) }
    var y by remember(node) { mutableStateOf((node?.y ?: initialY).toString()) }

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
                        Text(
                            if (node == null) "Add campus location" else "Edit location",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Coordinates are in map-world units (1000 × 760).",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Location name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = shortName,
                    onValueChange = { shortName = it },
                    label = { Text("Map label (short)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        NodeKind.BUILDING to "Building",
                        NodeKind.FACILITY to "Facility",
                        NodeKind.LANDMARK to "Landmark",
                        NodeKind.GATE to "Gate",
                        NodeKind.EVACUATION to "Assembly area"
                    ).chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { (value, label) ->
                                FilterChip(
                                    selected = kind == value,
                                    onClick = { kind = value },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = buildingCode,
                    onValueChange = { buildingCode = it },
                    label = { Text("Building code (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = x,
                        onValueChange = { x = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Map X") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = y,
                        onValueChange = { y = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Map Y") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Width") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Height") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Surface(
                    color = if (isEvacuation) SuccessSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = null, tint = if (isEvacuation) Success else MutedInk, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Evacuation assembly area", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Included as a destination in safe-route searches.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isEvacuation,
                            onCheckedChange = {
                                isEvacuation = it
                                if (it && kind == NodeKind.BUILDING) kind = NodeKind.EVACUATION
                            }
                        )
                    }
                }

                if (isEvacuation) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it.filter(Char::isDigit) },
                            label = { Text("Capacity") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = occupancy,
                            onValueChange = { occupancy = it.filter(Char::isDigit) },
                            label = { Text("Occupancy") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                PrimaryButton(
                    text = if (node == null) "Add location" else "Save changes",
                    icon = if (node == null) Icons.Rounded.AddLocationAlt else Icons.Rounded.Edit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank(),
                    onClick = {
                        onSave(
                            node?.id,
                            name.trim(),
                            shortName.trim(),
                            x.toFloatOrNull()?.coerceIn(20f, 980f) ?: initialX,
                            y.toFloatOrNull()?.coerceIn(20f, 740f) ?: initialY,
                            kind,
                            buildingCode.trim(),
                            isEvacuation,
                            capacity.toIntOrNull() ?: 0,
                            (occupancy.toIntOrNull() ?: 0).coerceAtMost(capacity.toIntOrNull() ?: Int.MAX_VALUE),
                            width.toFloatOrNull() ?: 90f,
                            height.toFloatOrNull() ?: 60f,
                            description.trim()
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun AddPathDialog(
    nodes: List<CampusNodeEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var fromId by remember { mutableStateOf<String?>(null) }
    var toId by remember { mutableStateOf<String?>(null) }
    var choosing by remember { mutableStateOf<Int?>(null) }
    val from = nodes.firstOrNull { it.id == fromId }
    val to = nodes.firstOrNull { it.id == toId }
    val distance = if (from != null && to != null) {
        (hypot(from.x - to.x, from.y - to.y) * 0.35f).coerceAtLeast(4f)
    } else null

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("Add walking path", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Connect two locations. Distance is estimated from their map coordinates and can be adjusted in the graph seed if needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SelectorField(
                    label = "From",
                    value = from?.name,
                    onClick = { choosing = 0 }
                )
                SelectorField(
                    label = "To",
                    value = to?.name,
                    onClick = { choosing = 1 }
                )
                if (distance != null) {
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                        Text(
                            "Estimated walking distance: ${distance.toInt()} m",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                PrimaryButton(
                    text = "Add path",
                    icon = Icons.Rounded.Link,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fromId != null && toId != null && fromId != toId,
                    onClick = { onAdd(checkNotNull(fromId), checkNotNull(toId)) }
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }

    val choosingIndex = choosing
    if (choosingIndex != null) {
        val title = if (choosingIndex == 0) "Choose starting location" else "Choose destination"
        AlertDialog(
            onDismissRequest = { choosing = null },
            title = { Text(title) },
            text = {
                LazyColumn(modifier = Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(nodes, key = { it.id }) { node ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (choosingIndex == 0) fromId = node.id else toId = node.id
                                    choosing = null
                                }
                                .padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(node.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { choosing = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SelectorField(label: String, value: String?, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = MutedInk)
                Text(
                    value ?: "Select a location",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (value == null) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (value == null) MutedInk else MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(Icons.Rounded.Map, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
        }
    }
}
