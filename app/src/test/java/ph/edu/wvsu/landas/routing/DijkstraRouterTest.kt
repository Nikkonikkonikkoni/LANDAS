package ph.edu.wvsu.landas.routing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity

class DijkstraRouterTest {

    private fun node(
        id: String,
        x: Float,
        y: Float,
        evacuation: Boolean = false,
        hazard: Boolean = false,
        capacity: Int = 0,
        occupancy: Int = 0
    ) = CampusNodeEntity(
        id = id,
        name = id,
        shortName = id,
        x = x,
        y = y,
        kind = if (evacuation) "EVACUATION" else "BUILDING",
        isEvacuationArea = evacuation,
        hazardBlocked = hazard,
        capacity = capacity,
        occupancy = occupancy
    )

    private fun edge(from: String, to: String, distance: Float, blocked: Boolean = false) =
        CampusEdgeEntity(
            id = "$from-$to",
            fromNodeId = from,
            toNodeId = to,
            distanceMeters = distance,
            hazardBlocked = blocked
        )

    private val nodes = listOf(
        node("start", 0f, 0f),
        node("mid", 100f, 0f),
        node("west", 0f, 100f),
        node("evacNorth", 200f, 0f, evacuation = true, capacity = 100),
        node("evacSouth", 0f, 200f, evacuation = true, capacity = 100)
    )

    @Test
    fun `chooses shortest reachable evacuation area`() {
        val edges = listOf(
            edge("start", "mid", 50f),
            edge("mid", "evacNorth", 50f),
            edge("start", "west", 50f),
            edge("west", "evacSouth", 150f)
        )
        val outcome = DijkstraRouter.routeToNearestEvacuation(nodes, edges, "start")
        assertTrue(outcome is RouteOutcome.Success)
        val success = outcome as RouteOutcome.Success
        assertEquals("evacNorth", success.destination.id)
        assertEquals(listOf("start", "mid", "evacNorth"), success.result.pathNodeIds)
        assertEquals(100f, success.result.distanceMeters, 0.01f)
        assertTrue(success.result.steps.isNotEmpty())
    }

    @Test
    fun `recalculates around a hazard-blocked edge`() {
        val edges = listOf(
            edge("start", "mid", 50f),
            edge("mid", "evacNorth", 50f, blocked = true),
            edge("start", "west", 50f),
            edge("west", "evacSouth", 150f)
        )
        val outcome = DijkstraRouter.routeToNearestEvacuation(nodes, edges, "start")
        val success = outcome as RouteOutcome.Success
        assertEquals("evacSouth", success.destination.id)
        assertEquals(listOf("start", "west", "evacSouth"), success.result.pathNodeIds)
    }

    @Test
    fun `redirects to next area when nearest is over capacity`() {
        val constrained = nodes.map {
            if (it.id == "evacNorth") it.copy(occupancy = 100) else it
        }
        val edges = listOf(
            edge("start", "mid", 50f),
            edge("mid", "evacNorth", 50f),
            edge("start", "west", 50f),
            edge("west", "evacSouth", 150f)
        )
        val outcome = DijkstraRouter.routeToNearestEvacuation(constrained, edges, "start")
        val success = outcome as RouteOutcome.Success
        assertEquals("evacSouth", success.destination.id)
        assertTrue(!success.capacityFallback)
    }

    @Test
    fun `fails clearly when every route is blocked`() {
        val edges = listOf(
            edge("start", "mid", 50f, blocked = true),
            edge("start", "west", 50f, blocked = true)
        )
        val outcome = DijkstraRouter.routeToNearestEvacuation(nodes, edges, "start")
        assertTrue(outcome is RouteOutcome.Failure)
    }

    @Test
    fun `builds turn-by-turn instructions`() {
        val edges = listOf(
            edge("start", "mid", 50f),
            edge("mid", "evacNorth", 50f)
        )
        val outcome = DijkstraRouter.routeToNearestEvacuation(nodes, edges, "start")
        val success = outcome as RouteOutcome.Success
        assertTrue(success.result.steps.first().instruction.startsWith("Head"))
        assertTrue(success.result.steps.last().instruction.startsWith("Arrive at"))
    }
}
