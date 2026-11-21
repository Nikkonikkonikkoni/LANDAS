package ph.edu.wvsu.landas.routing

import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity

enum class TurnDirection {
    START, STRAIGHT, LEFT, RIGHT, SLIGHT_LEFT, SLIGHT_RIGHT, U_TURN, ARRIVE
}

data class NavigationStep(
    val index: Int,
    val instruction: String,
    val detail: String,
    val distanceMeters: Int,
    val direction: TurnDirection,
    val nodeId: String
)

data class RouteResult(
    val startNodeId: String,
    val destinationNodeId: String,
    val pathNodeIds: List<String>,
    val distanceMeters: Float,
    val estimatedMinutes: Int,
    val computationMs: Double,
    val steps: List<NavigationStep>,
    val notes: List<String> = emptyList()
)

sealed interface RouteOutcome {
    data class Success(
        val result: RouteResult,
        val destination: CampusNodeEntity,
        val capacityFallback: Boolean = false
    ) : RouteOutcome

    data class Failure(val reason: String) : RouteOutcome
}

/**
 * An offline, on-device implementation of Dijkstra's shortest-path algorithm.
 *
 * The graph is undirected. Edges are weighted by their walking distance in metres.
 * Hazard-blocked edges and blocked intermediate nodes are removed before traversal.
 * Hazard blocking is dynamic: the UI recalculates immediately after an administrator
 * changes a route, without a network round trip.
 */
object DijkstraRouter {

    private data class QueueEntry(val nodeId: String, val distance: Float) : Comparable<QueueEntry> {
        override fun compareTo(other: QueueEntry): Int = distance.compareTo(other.distance)
    }

    private class Graph(
        val nodes: Map<String, CampusNodeEntity>,
        val adjacency: Map<String, List<Pair<String, Float>>>
    )

    private fun buildGraph(
        nodes: List<CampusNodeEntity>,
        edges: List<CampusEdgeEntity>,
        avoidHazards: Boolean
    ): Graph {
        val nodeMap = nodes.associateBy { it.id }
        val adjacency = mutableMapOf<String, MutableList<Pair<String, Float>>>()
        val blocked = nodes.filter { it.hazardBlocked }.map { it.id }.toSet()
        for (edge in edges) {
            if (edge.fromNodeId !in nodeMap || edge.toNodeId !in nodeMap) continue
            if (avoidHazards && (edge.hazardBlocked || edge.fromNodeId in blocked || edge.toNodeId in blocked)) continue
            if (!edge.accessible) continue
            adjacency.getOrPut(edge.fromNodeId) { mutableListOf() }
                .add(edge.toNodeId to edge.distanceMeters.coerceAtLeast(0.1f))
            adjacency.getOrPut(edge.toNodeId) { mutableListOf() }
                .add(edge.fromNodeId to edge.distanceMeters.coerceAtLeast(0.1f))
        }
        return Graph(nodeMap, adjacency)
    }

    fun routeToNearestEvacuation(
        nodes: List<CampusNodeEntity>,
        edges: List<CampusEdgeEntity>,
        startNodeId: String,
        avoidHazards: Boolean = true,
        respectCapacity: Boolean = true
    ): RouteOutcome {
        val start = nodes.firstOrNull { it.id == startNodeId }
            ?: return RouteOutcome.Failure("Starting location not found in the campus map.")
        val graph = buildGraph(nodes, edges, avoidHazards)
        if (start.id !in graph.adjacency) {
            return RouteOutcome.Failure("This location is not connected to a safe walking route.")
        }

        val availableAreas = graph.nodes.values.filter {
            it.isEvacuationArea && !it.hazardBlocked
        }
        if (availableAreas.isEmpty()) {
            return RouteOutcome.Failure("No open evacuation area is available. Contact the CDRRMO coordinator.")
        }

        val shortest = dijkstra(graph, startNodeId)
        val openAreas = availableAreas
            .mapNotNull { area -> shortest.distances[area.id]?.let { area to it } }
            .sortedBy { it.second }
        if (openAreas.isEmpty()) {
            return RouteOutcome.Failure("All evacuation routes are currently blocked. Admin assistance is required.")
        }

        var capacityFallback = false
        var selected = openAreas.firstOrNull { (area, _) ->
            !respectCapacity || area.capacity <= 0 || area.occupancy < area.capacity
        }
        if (selected == null) {
            selected = openAreas.first()
            capacityFallback = true
        }
        val notes = mutableListOf<String>()
        if (capacityFallback) {
            notes += "All open areas are near capacity. The nearest area is still recommended."
        }
        return buildOutcome(graph, shortest, start, selected.first, notes)
    }

    fun routeToDestination(
        nodes: List<CampusNodeEntity>,
        edges: List<CampusEdgeEntity>,
        startNodeId: String,
        destinationNodeId: String,
        avoidHazards: Boolean = true
    ): RouteOutcome {
        val start = nodes.firstOrNull { it.id == startNodeId }
            ?: return RouteOutcome.Failure("Starting location not found in the campus map.")
        val destination = nodes.firstOrNull { it.id == destinationNodeId }
            ?: return RouteOutcome.Failure("Destination not found in the campus map.")
        if (destination.hazardBlocked) {
            return RouteOutcome.Failure("${destination.name} is temporarily blocked by a hazard.")
        }
        if (start.id == destination.id) {
            return RouteOutcome.Failure("Choose a different destination.")
        }
        val graph = buildGraph(nodes, edges, avoidHazards)
        if (start.id !in graph.adjacency) {
            return RouteOutcome.Failure("This location is not connected to a safe walking route.")
        }
        val shortest = dijkstra(graph, startNodeId)
        if (shortest.distances[destination.id] == null) {
            return RouteOutcome.Failure("No safe path is available from ${start.name} to ${destination.name}.")
        }
        return buildOutcome(graph, shortest, start, destination, emptyList())
    }

    private class DijkstraResult(
        val distances: Map<String, Float>,
        val previous: Map<String, String>
    )

    private fun dijkstra(graph: Graph, startNodeId: String): DijkstraResult {
        val distances = mutableMapOf<String, Float>()
        val previous = mutableMapOf<String, String>()
        val visited = mutableSetOf<String>()
        val queue = PriorityQueue<QueueEntry>()
        distances[startNodeId] = 0f
        queue.add(QueueEntry(startNodeId, 0f))

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: continue
            if (!visited.add(current.nodeId)) continue
            val neighbors = graph.adjacency[current.nodeId] ?: continue
            for ((neighborId, weight) in neighbors) {
                if (neighborId in visited) continue
                if (graph.nodes[neighborId]?.hazardBlocked == true) continue
                val candidate = current.distance + weight
                if (candidate < (distances[neighborId] ?: Float.MAX_VALUE)) {
                    distances[neighborId] = candidate
                    previous[neighborId] = current.nodeId
                    queue.add(QueueEntry(neighborId, candidate))
                }
            }
        }
        return DijkstraResult(distances, previous)
    }

    private fun buildOutcome(
        graph: Graph,
        shortest: DijkstraResult,
        start: CampusNodeEntity,
        destination: CampusNodeEntity,
        notes: List<String>
    ): RouteOutcome {
        val startedAt = System.nanoTime()
        val path = mutableListOf<String>()
        var cursor: String? = destination.id
        while (cursor != null) {
            path.add(cursor)
            if (cursor == start.id) break
            cursor = shortest.previous[cursor]
        }
        if (path.lastOrNull() != start.id) {
            return RouteOutcome.Failure("No safe route could be reconstructed.")
        }
        val ordered = path.reversed()
        val distance = shortest.distances[destination.id] ?: 0f
        val computationMs = (System.nanoTime() - startedAt) / 1_000_000.0
        val steps = buildSteps(ordered, graph.nodes)
        val result = RouteResult(
            startNodeId = start.id,
            destinationNodeId = destination.id,
            pathNodeIds = ordered,
            distanceMeters = distance,
            estimatedMinutes = (distance / 1.25f / 60f).coerceAtLeast(1f).roundToInt(),
            computationMs = computationMs,
            steps = steps,
            notes = notes
        )
        return RouteOutcome.Success(result, destination)
    }

    private fun buildSteps(path: List<String>, nodes: Map<String, CampusNodeEntity>): List<NavigationStep> {
        if (path.size < 2) return emptyList()
        val points = path.mapNotNull { nodes[it] }
        val steps = mutableListOf<NavigationStep>()
        var cumulativeDistance = 0f

        for (index in points.indices) {
            val current = points[index]
            if (index == 0) {
                val next = points[1]
                val distance = segmentDistance(current, next)
                cumulativeDistance += distance
                val direction = compassDirection(current, next)
                steps += NavigationStep(
                    index = 0,
                    instruction = "Head $direction toward ${next.name}",
                    detail = "Start from ${current.name}. Follow the highlighted route.",
                    distanceMeters = distance.roundToInt(),
                    direction = TurnDirection.START,
                    nodeId = current.id
                )
            } else if (index == points.lastIndex) {
                steps += NavigationStep(
                    index = steps.size,
                    instruction = "Arrive at ${current.name}",
                    detail = if (current.isEvacuationArea) {
                        "You have reached the evacuation area. Check in or mark yourself safe."
                    } else {
                        "You have reached your destination."
                    },
                    distanceMeters = 0,
                    direction = TurnDirection.ARRIVE,
                    nodeId = current.id
                )
            } else {
                val previous = points[index - 1]
                val next = points[index + 1]
                val segment = segmentDistance(current, next)
                cumulativeDistance += segment
                val delta = turnDelta(bearing(previous, current), bearing(current, next))
                val turn = when {
                    abs(delta) >= 150f -> TurnDirection.U_TURN
                    delta > 55f -> TurnDirection.RIGHT
                    delta > 20f -> TurnDirection.SLIGHT_RIGHT
                    delta < -55f -> TurnDirection.LEFT
                    delta < -20f -> TurnDirection.SLIGHT_LEFT
                    else -> TurnDirection.STRAIGHT
                }
                val verb = when (turn) {
                    TurnDirection.U_TURN -> "Make a U-turn"
                    TurnDirection.RIGHT -> "Turn right"
                    TurnDirection.LEFT -> "Turn left"
                    TurnDirection.SLIGHT_RIGHT -> "Bear right"
                    TurnDirection.SLIGHT_LEFT -> "Bear left"
                    else -> "Continue straight"
                }
                steps += NavigationStep(
                    index = steps.size,
                    instruction = "$verb past ${current.name}",
                    detail = "Then continue toward ${next.name}.",
                    distanceMeters = segment.roundToInt(),
                    direction = turn,
                    nodeId = current.id
                )
            }
        }
        return steps
    }

    private fun segmentDistance(a: CampusNodeEntity, b: CampusNodeEntity): Float =
        kotlin.math.hypot(a.x - b.x, a.y - b.y) * 0.35f

    private fun bearing(from: CampusNodeEntity, to: CampusNodeEntity): Float {
        val degrees = Math.toDegrees(atan2((to.x - from.x).toDouble(), (from.y - to.y).toDouble())).toFloat()
        return (degrees + 360f) % 360f
    }

    private fun turnDelta(inbound: Float, outbound: Float): Float {
        var delta = outbound - inbound
        while (delta > 180f) delta -= 360f
        while (delta < -180f) delta += 360f
        return delta
    }

    private fun compassDirection(from: CampusNodeEntity, to: CampusNodeEntity): String {
        val angle = bearing(from, to)
        return when {
            angle < 22.5f || angle >= 337.5f -> "north"
            angle < 67.5f -> "northeast"
            angle < 112.5f -> "east"
            angle < 157.5f -> "southeast"
            angle < 202.5f -> "south"
            angle < 247.5f -> "southwest"
            angle < 292.5f -> "west"
            else -> "northwest"
        }
    }
}
