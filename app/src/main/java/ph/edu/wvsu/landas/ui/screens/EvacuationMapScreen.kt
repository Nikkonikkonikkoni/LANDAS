package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.EditLocationAlt
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TurnLeft
import androidx.compose.material.icons.rounded.TurnRight
import androidx.compose.material.icons.rounded.TurnSlightLeft
import androidx.compose.material.icons.rounded.TurnSlightRight
import androidx.compose.material.icons.rounded.UTurnLeft
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.routing.DijkstraRouter
import ph.edu.wvsu.landas.routing.NavigationStep
import ph.edu.wvsu.landas.routing.RouteOutcome
import ph.edu.wvsu.landas.routing.RouteResult
import ph.edu.wvsu.landas.routing.TurnDirection
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.DetailRow
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.OnlineStatusPill
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.map.CampusMap
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.DangerSoft
import ph.edu.wvsu.landas.ui.theme.Gold
import ph.edu.wvsu.landas.ui.theme.InfoBlue
import ph.edu.wvsu.landas.ui.theme.Navy
import ph.edu.wvsu.landas.ui.theme.SafetyOrange
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.TealDark
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft

@Composable
fun EvacuationMapScreen(
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    classrooms: List<ClassroomEntity>,
    isOnline: Boolean,
    onBack: () -> Unit,
    onMarkSafe: (() -> Unit)? = null,
    debugMode: Boolean = false,
    initialStartId: String = "n_admin"
) {
    var startNodeId by remember { mutableStateOf(initialStartId) }
    var startLabel by remember {
        mutableStateOf(nodes.firstOrNull { it.id == initialStartId }?.name ?: "Select a location")
    }
    var selectedDestinationId by remember { mutableStateOf<String?>(null) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var guiding by remember { mutableStateOf(false) }

    val evacuationAreas = remember(nodes) {
        nodes.filter { it.isEvacuationArea && !it.hazardBlocked }
    }
    val blockedNodes = nodes.count { it.hazardBlocked }
    val blockedEdges = edges.count { it.hazardBlocked }

    val nearestOutcome = remember(nodes, edges, startNodeId) {
        DijkstraRouter.routeToNearestEvacuation(nodes, edges, startNodeId)
    }
    val outcome = remember(nodes, edges, startNodeId, selectedDestinationId) {
        if (selectedDestinationId == null) {
            nearestOutcome
        } else {
            DijkstraRouter.routeToDestination(nodes, edges, startNodeId, selectedDestinationId!!)
        }
    }
    val route = (outcome as? RouteOutcome.Success)?.result
    val destination = (outcome as? RouteOutcome.Success)?.destination

    if (guiding && route != null && destination != null) {
        GuidanceView(
            route = route,
            destination = destination,
            nodes = nodes,
            edges = edges,
            isOnline = isOnline,
            onExit = { guiding = false },
            onMarkSafe = onMarkSafe
        )
        return
    }

    LandasScaffold(
        title = "Evacuation map",
        subtitle = "On-device Dijkstra routing · Offline ready",
        onBack = onBack,
        actions = {
            OnlineStatusPill(isOnline = isOnline)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StartLocationCard(
                startLabel = startLabel,
                onChange = { showLocationPicker = true }
            )

            CampusMap(
                nodes = nodes,
                edges = edges,
                routeNodeIds = route?.pathNodeIds.orEmpty(),
                userMapX = nodes.firstOrNull { it.id == startNodeId }?.x,
                userMapY = nodes.firstOrNull { it.id == startNodeId }?.y,
                debugMode = debugMode,
                onNodeTap = { node ->
                    startNodeId = node.id
                    startLabel = node.name
                    selectedDestinationId = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(372.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tap a building to move your start pin",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.DirectionsWalk, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(13.dp))
                    Text(
                        "${nodes.size} campus nodes",
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary
                    )
                }
            }

            if (blockedNodes > 0 || blockedEdges > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DangerSoft
                ) {
                    Row(
                        Modifier.padding(11.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Danger, modifier = Modifier.size(18.dp))
                        Text(
                            "Dynamic hazard blocking is active. ${blockedEdges + blockedNodes} blocked element${if (blockedEdges + blockedNodes == 1) "" else "s"} avoided.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusPill("Rerouted", Danger, Color.White.copy(alpha = 0.4f))
                    }
                }
            }

            when (outcome) {
                is RouteOutcome.Success -> {
                    RouteSummaryCard(
                        route = outcome.result,
                        destination = outcome.destination,
                        capacityFallback = outcome.capacityFallback,
                        onStartGuidance = { guiding = true },
                        onMarkSafe = onMarkSafe
                    )
                    DestinationAlternatives(
                        areas = evacuationAreas,
                        selectedDestinationId = selectedDestinationId,
                        nearestDestinationId = (nearestOutcome as? RouteOutcome.Success)?.destination?.id,
                        onSelect = { area ->
                            selectedDestinationId = if (selectedDestinationId == area.id) null else area.id
                        }
                    )
                    TurnByTurnCard(steps = outcome.result.steps)
                    if (outcome.result.notes.isNotEmpty()) {
                        Surface(
                            color = WarningSoft,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                outcome.result.notes.joinToString(" "),
                                style = MaterialTheme.typography.bodySmall,
                                color = Navy,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                is RouteOutcome.Failure -> {
                    Surface(
                        color = DangerSoft,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Danger)
                                Spacer(Modifier.width(8.dp))
                                Text("No safe route available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Danger)
                            }
                            Text(outcome.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { showLocationPicker = true }) {
                                Text("Choose another starting location")
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Rounded.EditLocationAlt, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                    Text(
                        "Map demonstrator: campus geometry and walking distances are a prototype dataset for WVSU-PC. In an actual emergency, always follow official responders and posted evacuation signs.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            nodes = nodes,
            classrooms = classrooms,
            onDismiss = { showLocationPicker = false },
            onSelectNode = { node ->
                startNodeId = node.id
                startLabel = node.name
                selectedDestinationId = null
                showLocationPicker = false
            },
            onSelectRoom = { room ->
                val node = nodes.firstOrNull { it.buildingCode == room.buildingCode }
                if (node != null) {
                    startNodeId = node.id
                    startLabel = "${room.name} · ${room.buildingName}"
                    selectedDestinationId = null
                }
                showLocationPicker = false
            }
        )
    }
}

@Composable
private fun StartLocationCard(startLabel: String, onChange: () -> Unit) {
    AppCard {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.MyLocation, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("YOUR STARTING LOCATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    startLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onChange) {
                Text("Change")
            }
        }
    }
}

@Composable
private fun RouteSummaryCard(
    route: RouteResult,
    destination: CampusNodeEntity,
    capacityFallback: Boolean,
    onStartGuidance: () -> Unit,
    onMarkSafe: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = TealDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(
                    text = if (capacityFallback) "NEAREST · AT CAPACITY" else "RECOMMENDED ROUTE",
                    color = Navy,
                    background = Gold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "Dijkstra · ${String.format("%.1f", route.computationMs)} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Success),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("Nearest safe assembly", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        destination.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RouteMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.DirectionsWalk,
                    value = "${route.distanceMeters.toInt()} m",
                    label = "walking distance"
                )
                RouteMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.AccessTime,
                    value = "${route.estimatedMinutes} min",
                    label = "estimated time"
                )
                RouteMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.AltRoute,
                    value = "${(route.pathNodeIds.size - 2).coerceAtLeast(0)}",
                    label = "turns"
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.13f))
                ) {
                    val occupancyRatio = if (destination.capacity > 0) {
                        destination.occupancy.toFloat() / destination.capacity
                    } else 0f
                    Box(
                        Modifier
                            .fillMaxWidth(occupancyRatio.coerceIn(0f, 1f))
                            .height(5.dp)
                            .background(if (occupancyRatio > .85f) SafetyOrange else Color(0xFF8CE99A), CircleShape)
                    )
                }
                Spacer(Modifier.width(9.dp))
                Text(
                    "${destination.occupancy}/${destination.capacity} capacity",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton(
                    text = "Start guidance",
                    icon = Icons.Rounded.Navigation,
                    modifier = Modifier.weight(1f),
                    containerColor = Gold,
                    contentColor = Navy,
                    onClick = onStartGuidance
                )
                if (onMarkSafe != null) {
                    OutlinedButton(
                        onClick = onMarkSafe,
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(15.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("I'm safe", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteMetric(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(15.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}

@Composable
private fun DestinationAlternatives(
    areas: List<CampusNodeEntity>,
    selectedDestinationId: String?,
    nearestDestinationId: String?,
    onSelect: (CampusNodeEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Assembly areas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (selectedDestinationId != null) {
                TextButton(onClick = { areas.firstOrNull { it.id == nearestDestinationId }?.let(onSelect) }) {
                    Text("Use nearest", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        areas.forEach { area ->
            val selected = selectedDestinationId == area.id
            val nearest = area.id == nearestDestinationId && selectedDestinationId == null
            Surface(
                onClick = { onSelect(area) },
                shape = RoundedCornerShape(16.dp),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selected) TealPrimary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (area.occupancy >= area.capacity) DangerSoft else SuccessSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = null, tint = Success, modifier = Modifier.size(19.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(area.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (nearest) {
                                StatusPill("Nearest", Success, SuccessSoft)
                            }
                        }
                        Text(
                            "${area.occupancy}/${area.capacity} people · ${if (area.occupancy >= area.capacity) "at capacity" else "${area.capacity - area.occupancy} spaces open"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        if (selected) Icons.Rounded.Flag else Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = if (selected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnByTurnCard(steps: List<NavigationStep>) {
    var expanded by remember { mutableStateOf(false) }
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.AltRoute, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text("Turn-by-turn directions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("${steps.size} steps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Hide" else "View")
                }
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    steps.forEachIndexed { index, step ->
                        NavigationStepRow(step = step, isLast = index == steps.lastIndex)
                    }
                }
            }
            if (!expanded && steps.isNotEmpty()) {
                NavigationStepRow(step = steps.first(), isLast = steps.size == 1)
            }
        }
    }
}

@Composable
private fun NavigationStepRow(step: NavigationStep, isLast: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (step.direction == TurnDirection.ARRIVE) SuccessSoft else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    stepIcon(step.direction),
                    contentDescription = null,
                    tint = if (step.direction == TurnDirection.ARRIVE) Success else TealPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        Column(
            Modifier
                .weight(1f)
                .padding(top = 4.dp, bottom = if (isLast) 0.dp else 5.dp)
        ) {
            Text(step.instruction, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(
                if (step.distanceMeters > 0) "${step.distanceMeters} m · ${step.detail}" else step.detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun stepIcon(direction: TurnDirection): ImageVector = when (direction) {
    TurnDirection.START -> Icons.Rounded.Navigation
    TurnDirection.STRAIGHT -> Icons.Rounded.ArrowUpward
    TurnDirection.LEFT -> Icons.Rounded.TurnLeft
    TurnDirection.RIGHT -> Icons.Rounded.TurnRight
    TurnDirection.SLIGHT_LEFT -> Icons.Rounded.TurnSlightLeft
    TurnDirection.SLIGHT_RIGHT -> Icons.Rounded.TurnSlightRight
    TurnDirection.U_TURN -> Icons.Rounded.UTurnLeft
    TurnDirection.ARRIVE -> Icons.Rounded.Flag
}

@Composable
private fun GuidanceView(
    route: RouteResult,
    destination: CampusNodeEntity,
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    isOnline: Boolean,
    onExit: () -> Unit,
    onMarkSafe: (() -> Unit)?
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = route.steps
    val currentStep = steps.getOrNull(stepIndex)
    val arrival = stepIndex >= steps.lastIndex

    LaunchedEffect(stepIndex) {
        if (stepIndex < steps.lastIndex) {
            delay(5000)
            stepIndex += 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TealDark)
    ) {
        CampusMap(
            nodes = nodes,
            edges = edges,
            routeNodeIds = route.pathNodeIds,
            modifier = Modifier.fillMaxSize(),
            showControls = false,
            showLegend = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 5.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(SuccessSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Flag, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("EVACUATION GUIDANCE", style = MaterialTheme.typography.labelSmall, color = TealPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            destination.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onExit) {
                        Icon(Icons.Rounded.Close, contentDescription = "Exit guidance")
                    }
                }
            }
            Spacer(Modifier.weight(1f))

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 12.dp
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (arrival) SuccessSoft else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (currentStep != null) stepIcon(currentStep.direction) else Icons.Rounded.Flag,
                                contentDescription = null,
                                tint = if (arrival) Success else TealPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (arrival) "You have arrived" else currentStep?.instruction ?: "Follow the highlighted route",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Text(
                                if (arrival) {
                                    "Check in at ${destination.shortName} and mark yourself safe."
                                } else {
                                    currentStep?.detail ?: "Continue along the safe route."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Step ${(stepIndex + 1).coerceAtMost(steps.size.coerceAtLeast(1))} of ${steps.size.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Remaining ≈ ${((route.distanceMeters / steps.size) * (steps.size - stepIndex)).toInt()} m",
                                style = MaterialTheme.typography.labelSmall,
                                color = TealPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            OnlineStatusPill(isOnline = isOnline)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { (stepIndex + 1f) / steps.size.coerceAtLeast(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = TealPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    if (arrival) {
                        PrimaryButton(
                            text = "I have arrived · Mark safe",
                            icon = Icons.Rounded.Flag,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Success,
                            onClick = {
                                if (onMarkSafe != null) {
                                    onMarkSafe()
                                } else {
                                    onExit()
                                }
                            }
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onExit,
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(15.dp)
                            ) {
                                Text("Exit")
                            }
                            PrimaryButton(
                                text = "Next instruction",
                                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                                modifier = Modifier.weight(1.3f),
                                onClick = { if (stepIndex < steps.lastIndex) stepIndex += 1 }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationPickerDialog(
    nodes: List<CampusNodeEntity>,
    classrooms: List<ClassroomEntity>,
    onDismiss: () -> Unit,
    onSelectNode: (CampusNodeEntity) -> Unit,
    onSelectRoom: (ClassroomEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val selectableNodes = remember(nodes) {
        nodes.filter {
            it.kind != NodeKind.JUNCTION && !it.isEvacuationArea
        }.sortedBy { it.name }
    }
    val filteredNodes = selectableNodes.filter {
        query.isBlank() ||
            it.name.contains(query, ignoreCase = true) ||
            it.shortName.contains(query, ignoreCase = true) ||
            it.buildingCode.contains(query, ignoreCase = true)
    }
    val filteredRooms = classrooms.filter {
        query.isBlank() ||
            it.name.contains(query, ignoreCase = true) ||
            it.buildingName.contains(query, ignoreCase = true)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 40.dp)
                .imePadding(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Choose starting location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Buildings, landmarks, or a specific room", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search buildings or rooms") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(430.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredNodes.isNotEmpty()) {
                        item {
                            Text(
                                "CAMPUS LOCATIONS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                            )
                        }
                        items(filteredNodes, key = { it.id }) { node ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectNode(node) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (node.kind == NodeKind.GATE) Icons.Rounded.MyLocation else Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = TealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(node.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        node.description.ifBlank { node.kind.lowercase().replaceFirstChar { it.uppercase() } },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (node.buildingCode.isNotBlank()) {
                                    Text(node.buildingCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    if (filteredRooms.isNotEmpty()) {
                        item {
                            Text(
                                "ROOMS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                            )
                        }
                        items(filteredRooms, key = { it.id }) { room ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onSelectRoom(room) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE4F1EF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Rounded.AltRoute, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(room.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${room.buildingName} · ${room.floor}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    if (filteredNodes.isEmpty() && filteredRooms.isEmpty()) {
                        item {
                            Text(
                                "No matching location found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
