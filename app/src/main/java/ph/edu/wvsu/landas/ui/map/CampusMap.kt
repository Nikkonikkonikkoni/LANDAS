package ph.edu.wvsu.landas.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.min
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.Navy
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.TealDark
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.Warning

private const val WORLD_WIDTH = 1000f
private const val WORLD_HEIGHT = 760f

@Composable
fun CampusMap(
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    routeNodeIds: List<String> = emptyList(),
    userMapX: Float? = null,
    userMapY: Float? = null,
    debugMode: Boolean = false,
    onNodeTap: ((CampusNodeEntity) -> Unit)? = null,
    onMapTap: ((Float, Float) -> Unit)? = null,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
    showLegend: Boolean = true
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var fitScale by remember { mutableFloatStateOf(0.35f) }
    var scale by remember { mutableFloatStateOf(0.35f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var initialized by remember { mutableStateOf(false) }

    fun initialOffset(size: IntSize, newScale: Float) = Offset(
        x = (size.width - WORLD_WIDTH * newScale) / 2f,
        y = (size.height - WORLD_HEIGHT * newScale) / 2f
    )

    fun setInitialView(size: IntSize) {
        if (size.width == 0 || size.height == 0) return
        val fitted = min(size.width / WORLD_WIDTH, size.height / WORLD_HEIGHT)
        fitScale = fitted
        scale = fitted * 1.02f
        offset = initialOffset(size, scale)
        initialized = true
    }

    fun zoomAround(center: Offset, zoomFactor: Float) {
        if (!initialized) return
        val oldScale = scale
        val minScale = fitScale * 0.85f
        val maxScale = fitScale * 6f
        val newScale = (oldScale * zoomFactor).coerceIn(minScale, maxScale)
        offset = center - (center - offset) * (newScale / oldScale)
        scale = newScale
    }

    val nodeMap = remember(nodes) { nodes.associateBy { it.id } }
    val routeNodes = remember(routeNodeIds, nodeMap) { routeNodeIds.mapNotNull { nodeMap[it] } }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE2EBDE))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    canvasSize = size
                    if (!initialized) setInitialView(size)
                }
                .pointerInput(canvasSize, fitScale) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        if (!initialized) return@detectTransformGestures
                        val oldScale = scale
                        val minScale = fitScale * 0.85f
                        val maxScale = fitScale * 6f
                        val newScale = (oldScale * zoom).coerceIn(minScale, maxScale)
                        val adjustedOffset = centroid - (centroid - offset) * (newScale / oldScale)
                        scale = newScale
                        offset = adjustedOffset + pan
                    }
                }
                .pointerInput(nodes, scale, offset, onNodeTap, onMapTap) {
                    detectTapGestures { tap ->
                        if (!initialized) return@detectTapGestures
                        val world = (tap - offset) / scale
                        if (world.x !in 0f..WORLD_WIDTH || world.y !in 0f..WORLD_HEIGHT) return@detectTapGestures
                        val selected = nodes
                            .filter { it.kind != NodeKind.JUNCTION }
                            .filter { node ->
                                val dx = node.x - world.x
                                val dy = node.y - world.y
                                val radius = (maxOf(node.width, node.height) / 2f).coerceAtLeast(35f) + 30f
                                hypot(dx, dy) <= radius
                            }
                            .minByOrNull { node ->
                                hypot(node.x - world.x, node.y - world.y)
                            }
                        if (selected != null) {
                            onNodeTap?.invoke(selected)
                        } else {
                            onMapTap?.invoke(world.x, world.y)
                        }
                    }
                }
        ) {
            val treePositions = listOf(
                90f to 260f, 120f to 330f, 80f to 430f, 130f to 650f, 220f to 700f,
                300f to 680f, 400f to 690f, 470f to 660f, 580f to 730f, 700f to 700f,
                820f to 660f, 870f to 570f, 900f to 490f, 900f to 370f, 880f to 300f,
                890f to 170f, 700f to 110f, 600f to 140f, 500f to 120f, 190f to 100f,
                95f to 170f, 360f to 500f, 430f to 480f, 260f to 250f, 680f to 400f,
                720f to 380f, 560f to 430f, 180f to 500f, 840f to 360f
            )

            withTransform({
                translate(offset.x, offset.y)
                scale(scale, scale, pivot = Offset.Zero)
            }) {
                // Campus ground and boundary
                drawRoundRect(
                    color = Color(0xFFE9F1E3),
                    topLeft = Offset(15f, 15f),
                    size = Size(WORLD_WIDTH - 30f, WORLD_HEIGHT - 30f),
                    cornerRadius = CornerRadius(40f, 40f)
                )
                drawRoundRect(
                    color = Color(0xFFCBD9C4),
                    topLeft = Offset(15f, 15f),
                    size = Size(WORLD_WIDTH - 30f, WORLD_HEIGHT - 30f),
                    cornerRadius = CornerRadius(40f, 40f),
                    style = Stroke(width = 6f)
                )

                // Internal lawns
                drawRoundRect(
                    color = Color(0xFFDDEBD3),
                    topLeft = Offset(250f, 85f),
                    size = Size(290f, 130f),
                    cornerRadius = CornerRadius(40f, 40f)
                )
                drawRoundRect(
                    color = Color(0xFFDDEBD3),
                    topLeft = Offset(430f, 480f),
                    size = Size(160f, 100f),
                    cornerRadius = CornerRadius(24f, 24f)
                )

                // Walkway network
                edges.forEach { edge ->
                    val from = nodeMap[edge.fromNodeId] ?: return@forEach
                    val to = nodeMap[edge.toNodeId] ?: return@forEach
                    val edgeColor = when {
                        edge.hazardBlocked -> Color(0xFFDBA7A0)
                        debugMode -> Color(0xFF7A9E9F)
                        else -> Color(0xFFB9C8B4)
                    }
                    drawLine(
                        color = Color.White.copy(alpha = 0.65f),
                        start = Offset(from.x, from.y),
                        end = Offset(to.x, to.y),
                        strokeWidth = 18f
                    )
                    drawLine(
                        color = edgeColor,
                        start = Offset(from.x, from.y),
                        end = Offset(to.x, to.y),
                        strokeWidth = 9f
                    )
                    if (edge.hazardBlocked) {
                        val mid = Offset((from.x + to.x) / 2f, (from.y + to.y) / 2f)
                        drawCircle(Color(0xFFD62828), radius = 13f, center = mid)
                        drawLine(Color.White, mid - Offset(6f, 0f), mid + Offset(6f, 0f), strokeWidth = 4f)
                        drawLine(Color.White, mid - Offset(0f, 6f), mid + Offset(0f, 6f), strokeWidth = 4f)
                    }
                }

                // Trees
                treePositions.forEach { (x, y) ->
                    drawCircle(Color(0xFFB5D2A8), radius = 15f, center = Offset(x, y))
                    drawCircle(Color(0xFF8FB783), radius = 8f, center = Offset(x - 3f, y - 3f))
                }

                // Evacuation areas (under building layer so labels remain visible)
                nodes.filter { it.isEvacuationArea }.forEach { area ->
                    val left = area.x - area.width / 2f
                    val top = area.y - area.height / 2f
                    val shapeRadius = if (area.name.contains("Oval")) CornerRadius(70f, 70f) else CornerRadius(22f, 22f)
                    drawRoundRect(
                        color = if (area.hazardBlocked) Color(0xFFE7B8AE) else Color(0xFFA7D3AE),
                        topLeft = Offset(left, top),
                        size = Size(area.width, area.height),
                        cornerRadius = shapeRadius
                    )
                    drawRoundRect(
                        color = if (area.hazardBlocked) Danger else Success,
                        topLeft = Offset(left, top),
                        size = Size(area.width, area.height),
                        cornerRadius = shapeRadius,
                        style = Stroke(
                            width = 5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 9f))
                        )
                    )
                    val label = textMeasurer.measure(
                        text = area.shortName,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1
                    )
                    drawText(
                        textLayoutResult = label,
                        topLeft = Offset(
                            area.x - label.size.width / 2f,
                            area.y - label.size.height / 2f - 9f
                        )
                    )
                    val occupancy = textMeasurer.measure(
                        text = "${area.occupancy}/${area.capacity}",
                        style = TextStyle(
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = occupancy,
                        topLeft = Offset(
                            area.x - occupancy.size.width / 2f,
                            area.y + 10f
                        )
                    )
                }

                // Buildings and landmarks
                nodes.filter {
                    it.kind != NodeKind.JUNCTION && !it.isEvacuationArea
                }.forEach { node ->
                    val fill = if (node.hazardBlocked) Color(0xFFC77C75) else parseHexColor(node.colorHex)
                    val left = node.x - node.width / 2f
                    val top = node.y - node.height / 2f
                    val radius = CornerRadius(12f, 12f)
                    drawRoundRect(
                        color = Color(0xFF66816D).copy(alpha = 0.18f),
                        topLeft = Offset(left + 5f, top + 7f),
                        size = Size(node.width, node.height),
                        cornerRadius = radius
                    )
                    drawRoundRect(
                        color = fill,
                        topLeft = Offset(left, top),
                        size = Size(node.width, node.height),
                        cornerRadius = radius
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.85f),
                        topLeft = Offset(left, top),
                        size = Size(node.width, node.height),
                        cornerRadius = radius,
                        style = Stroke(width = 3f)
                    )
                    val labelColor = if (fill.luminance() > 0.62f) Navy else Color.White
                    val label = textMeasurer.measure(
                        text = node.shortName,
                        style = TextStyle(
                            color = labelColor,
                            fontSize = if (node.width < 120f) 19.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 2
                    )
                    drawText(
                        textLayoutResult = label,
                        topLeft = Offset(
                            node.x - label.size.width / 2f,
                            node.y - label.size.height / 2f
                        )
                    )
                    if (node.hazardBlocked) {
                        drawLine(
                            color = Color.White,
                            start = Offset(left + 16f, top + 16f),
                            end = Offset(left + node.width - 16f, top + node.height - 16f),
                            strokeWidth = 6f
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(left + node.width - 16f, top + 16f),
                            end = Offset(left + 16f, top + node.height - 16f),
                            strokeWidth = 6f
                        )
                    }
                    if (debugMode) {
                        drawCircle(
                            color = if (node.hazardBlocked) Danger else TealDark,
                            radius = 7f,
                            center = Offset(node.x, node.y)
                        )
                        val idLabel = textMeasurer.measure(
                            text = node.id,
                            style = TextStyle(
                                color = Navy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 1
                        )
                        drawText(idLabel, topLeft = Offset(node.x + 8f, node.y + 8f))
                    }
                }
            }

            // Route is drawn in screen space to keep a comfortable line width at every zoom level.
            if (routeNodes.size >= 2) {
                val points = routeNodes.map { Offset(it.x * scale + offset.x, it.y * scale + offset.y) }
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                }
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = 0.95f),
                    style = Stroke(width = 15.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                drawPath(
                    path = path,
                    color = TealPrimary,
                    style = Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                points.drop(1).dropLast(1).forEach { point ->
                    drawCircle(Color.White, radius = 4.5.dp.toPx(), center = point)
                    drawCircle(TealPrimary, radius = 2.5.dp.toPx(), center = point)
                }
                val start = points.first()
                val end = points.last()
                drawCircle(Color.White, radius = 15.dp.toPx(), center = start)
                drawCircle(TealDark, radius = 11.dp.toPx(), center = start)
                drawCircle(Color.White, radius = 4.dp.toPx(), center = start)
                drawCircle(Color.White, radius = 17.dp.toPx(), center = end)
                drawCircle(Success, radius = 13.dp.toPx(), center = end)

                val startLabel = textMeasurer.measure(
                    "START",
                    style = TextStyle(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    startLabel,
                    topLeft = Offset(start.x - startLabel.size.width / 2f, start.y + 18.dp.toPx())
                )
                val destNode = routeNodes.last()
                val destLabel = textMeasurer.measure(
                    destNode.shortName,
                    style = TextStyle(color = Success, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                )
                drawText(
                    destLabel,
                    topLeft = Offset(end.x - destLabel.size.width / 2f, end.y + 19.dp.toPx())
                )
            }

            if (userMapX != null && userMapY != null) {
                val point = Offset(userMapX * scale + offset.x, userMapY * scale + offset.y)
                drawCircle(Color(0x33276FBF), radius = 22.dp.toPx(), center = point)
                drawCircle(Color.White, radius = 10.dp.toPx(), center = point)
                drawCircle(Color(0xFF276FBF), radius = 7.dp.toPx(), center = point)
            }

            // North indicator
            val northOffset = Offset(28.dp.toPx(), size.height - 34.dp.toPx())
            drawCircle(Color.White.copy(alpha = 0.92f), radius = 16.dp.toPx(), center = northOffset)
            drawLine(TealDark, northOffset + Offset(0f, 7.dp.toPx()), northOffset - Offset(0f, 8.dp.toPx()), strokeWidth = 2.dp.toPx())
            drawLine(TealDark, northOffset - Offset(0f, 8.dp.toPx()), northOffset - Offset(4.dp.toPx(), 2.dp.toPx()), strokeWidth = 2.dp.toPx())
            drawLine(TealDark, northOffset - Offset(0f, 8.dp.toPx()), northOffset + Offset(4.dp.toPx(), 2.dp.toPx()), strokeWidth = 2.dp.toPx())
            val nLabel = textMeasurer.measure("N", style = TextStyle(color = Navy, fontSize = 8.sp, fontWeight = FontWeight.Bold))
            drawText(nLabel, topLeft = Offset(northOffset.x - nLabel.size.width / 2f, northOffset.y + 9.dp.toPx()))
        }

        if (showLegend) {
            MapLegend(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }

        if (showControls) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                MapControlButton(
                    icon = { Icon(Icons.Rounded.Add, contentDescription = "Zoom in", modifier = Modifier.size(19.dp)) },
                    onClick = { zoomAround(Offset(canvasSize.width / 2f, canvasSize.height / 2f), 1.35f) }
                )
                MapControlButton(
                    icon = { Icon(Icons.Rounded.Remove, contentDescription = "Zoom out", modifier = Modifier.size(19.dp)) },
                    onClick = { zoomAround(Offset(canvasSize.width / 2f, canvasSize.height / 2f), 0.74f) }
                )
                MapControlButton(
                    icon = { Icon(Icons.Rounded.MyLocation, contentDescription = "Reset map", modifier = Modifier.size(18.dp)) },
                    onClick = { setInitialView(canvasSize) }
                )
            }
        }
    }
}

@Composable
private fun MapControlButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shadowElevation = 3.dp
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Box(contentAlignment = Alignment.Center) { icon() }
        }
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LegendRow(Color(0xFFA7D3AE), Success, "Evacuation area")
            LegendRow(Color(0xFFD9A441), Color(0xFF8A6500), "Academic building")
            LegendRow(TealPrimary, TealPrimary, "Safe route")
        }
    }
}

@Composable
private fun LegendRow(fill: Color, border: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            Modifier
                .size(9.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(fill)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = Navy)
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        TealPrimary
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
