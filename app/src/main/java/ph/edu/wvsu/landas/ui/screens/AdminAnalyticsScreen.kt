package ph.edu.wvsu.landas.ui.screens

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
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AnnouncementEntity
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusEdgeEntity
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.DistressStatus
import ph.edu.wvsu.landas.data.Roles
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

@Composable
fun AdminAnalyticsScreen(
    users: List<UserEntity>,
    attendanceToday: List<AttendanceRecord>,
    allAttendance: List<AttendanceRecord>,
    distressSignals: List<DistressSignal>,
    alerts: List<AlertEntity>,
    announcements: List<AnnouncementEntity>,
    nodes: List<CampusNodeEntity>,
    edges: List<CampusEdgeEntity>,
    classrooms: List<ClassroomEntity>,
    schedules: List<ScheduleSlotEntity>,
    onBack: () -> Unit
) {
    val students = users.filter { it.role == Roles.STUDENT && it.isActive }
    val faculty = users.filter { it.role == Roles.FACULTY && it.isActive }
    val activeSos = distressSignals.filter { it.status != DistressStatus.RESOLVED }
    val resolvedSos = distressSignals.filter { it.status == DistressStatus.RESOLVED }
    val acknowledgedSos = distressSignals.filter { it.status == DistressStatus.ACKNOWLEDGED }
    val blockedNodes = nodes.count { it.hazardBlocked }
    val blockedEdges = edges.count { it.hazardBlocked }
    val totalElements = (nodes.size + edges.size).coerceAtLeast(1)
    val routeHealth = (1f - (blockedNodes + blockedEdges).toFloat() / totalElements).coerceIn(0f, 1f)

    val safeStudents = attendanceToday.filter { it.role == Roles.STUDENT }.map { it.userId }.distinct().size
    val safeFaculty = attendanceToday.filter { it.role == Roles.FACULTY }.map { it.userId }.distinct().size
    val denominator = (students.size + faculty.size).coerceAtLeast(1)
    val accountability = (safeStudents + safeFaculty).toFloat() / denominator

    val evacuationAreas = nodes.filter { it.isEvacuationArea }
    val totalCapacity = evacuationAreas.sumOf { it.capacity }
    val totalOccupancy = evacuationAreas.sumOf { it.occupancy }
    val capacityHealth = if (totalCapacity == 0) 1f else ((totalCapacity - totalOccupancy).toFloat() / totalCapacity).coerceIn(0f, 1f)
    val syncHealth = if (attendanceToday.isEmpty() && distressSignals.isEmpty()) 1f else {
        val total = attendanceToday.size + distressSignals.size
        val synced = attendanceToday.count { it.synced } + distressSignals.count { it.synced }
        synced.toFloat() / total
    }
    val sosPenalty = (activeSos.size * 0.06f).coerceAtMost(0.2f)
    val readiness = (routeHealth * 0.34f + accountability * 0.34f + capacityHealth * 0.18f + syncHealth * 0.14f - sosPenalty)
        .coerceIn(0f, 1f)

    val today = LocalDate.now()
    val hourly = remember(attendanceToday) {
        (6..20).map { hour ->
            val count = attendanceToday.count {
                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).hour == hour
            }
            "${hour}" to count
        }
    }
    val weekly = remember(allAttendance, today) {
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val count = allAttendance.count {
                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
            }
            date.format(DateTimeFormatter.ofPattern("EEE", Locale.US)) to count
        }
    }
    val hourlyPeak = hourly.maxOfOrNull { it.second } ?: 0
    val weeklyPeak = weekly.maxOfOrNull { it.second } ?: 0
    val historicalDays = allAttendance
        .map { Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
        .distinct()
        .size

    LandasScaffold(
        title = "Analytics",
        subtitle = "Readiness, trends, and response metrics",
        onBack = onBack,
        actions = {
            StatusPill(
                "${(readiness * 100).toInt()}% ready",
                if (readiness >= 0.8f) Success else if (readiness >= 0.6f) Warning else Danger,
                if (readiness >= 0.8f) SuccessSoft else if (readiness >= 0.6f) WarningSoft else DangerSoft,
                Icons.Rounded.Analytics
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
                Surface(shape = RoundedCornerShape(24.dp), color = TealDark) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(13.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Insights, contentDescription = null, tint = Gold, modifier = Modifier.size(23.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Campus readiness score",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    "${(readiness * 100).toInt()}%",
                                    color = Color.White,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            StatusPill(
                                text = when {
                                    readiness >= 0.8f -> "Ready"
                                    readiness >= 0.6f -> "Needs attention"
                                    else -> "At risk"
                                },
                                color = if (readiness >= 0.8f) Success else if (readiness >= 0.6f) Warning else Danger,
                                background = Color.White
                            )
                        }
                        LinearProgressIndicator(
                            progress = { readiness },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp),
                            color = when {
                                readiness >= 0.8f -> Color(0xFF8CE99A)
                                readiness >= 0.6f -> Gold
                                else -> Color(0xFFFFA8A8)
                            },
                            trackColor = Color.White.copy(alpha = 0.15f)
                        )
                        Text(
                            "Weighted from route availability, today's accountability, assembly-area headroom, local sync health, and open SOS load. This is a prototype indicator, not an official readiness certification.",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    AnalyticsTile(
                        modifier = Modifier.weight(1f),
                        value = "${(accountability * 100).toInt()}%",
                        label = "Accountability",
                        icon = Icons.Rounded.Groups,
                        accent = if (accountability >= 0.8f) Success else Warning,
                        background = if (accountability >= 0.8f) SuccessSoft else WarningSoft
                    )
                    AnalyticsTile(
                        modifier = Modifier.weight(1f),
                        value = "${activeSos.size}",
                        label = "Open SOS",
                        icon = Icons.Rounded.Sos,
                        accent = if (activeSos.isEmpty()) Success else Danger,
                        background = if (activeSos.isEmpty()) SuccessSoft else DangerSoft
                    )
                    AnalyticsTile(
                        modifier = Modifier.weight(1f),
                        value = "${blockedNodes + blockedEdges}",
                        label = "Blocked routes",
                        icon = Icons.Rounded.WarningAmber,
                        accent = if (blockedNodes + blockedEdges == 0) Success else Warning,
                        background = if (blockedNodes + blockedEdges == 0) SuccessSoft else WarningSoft
                    )
                }
            }

            item {
                SectionHeader("Today's check-ins by hour")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${attendanceToday.size} record${if (attendanceToday.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(
                                "Peak ${hourly.maxByOrNull { it.second }?.first ?: "–"}:00",
                                TealPrimary,
                                TealSoft,
                                Icons.Rounded.TrendingUp
                            )
                        }
                        BarChart(
                            data = hourly.map { (label, value) ->
                                (if (label.toInt() % 2 == 0) label else "") to value
                            },
                            peak = hourlyPeak,
                            accent = TealPrimary,
                            emptyLabel = "No check-ins recorded today"
                        )
                        Text(
                            "Hours are local device time. Bars show one-tap and manual attendance combined.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Seven-day attendance trend")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${allAttendance.size} total record${if (allAttendance.size == 1) "" else "s"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(
                                "$historicalDays active day${if (historicalDays == 1) "" else "s"}",
                                InfoBlue,
                                InfoSoft
                            )
                        }
                        BarChart(
                            data = weekly,
                            peak = weeklyPeak,
                            accent = InfoBlue,
                            emptyLabel = "No attendance history yet"
                        )
                        Text(
                            "Historical rows are preserved when an administrator clears today's attendance; only the current day is removed.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Accountability by role")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                        RoleProgress(
                            label = "Students",
                            safe = safeStudents,
                            total = students.size,
                            accent = InfoBlue
                        )
                        RoleProgress(
                            label = "Faculty",
                            safe = safeFaculty,
                            total = faculty.size,
                            accent = Color(0xFF6D5FA8)
                        )
                        RoleProgress(
                            label = "Administrators",
                            safe = attendanceToday.count { it.role == Roles.ADMIN }.coerceAtMost(
                                users.count { it.role == Roles.ADMIN && it.isActive }
                            ),
                            total = users.count { it.role == Roles.ADMIN && it.isActive },
                            accent = Success
                        )
                        HorizontalDivider()
                        Text(
                            "Visitor accounts are excluded from accountability totals. Each person is counted once even if multiple check-ins exist.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Assembly area utilization")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (evacuationAreas.isEmpty()) {
                            Text(
                                "No assembly areas are defined. Add one in Routes & hazards.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        evacuationAreas.forEach { area ->
                            val ratio = if (area.capacity > 0) area.occupancy.toFloat() / area.capacity else 0f
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Flag,
                                        contentDescription = null,
                                        tint = if (area.hazardBlocked) Danger else Success,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        area.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        if (area.hazardBlocked) "CLOSED" else "${area.occupancy}/${area.capacity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            area.hazardBlocked -> Danger
                                            ratio >= 0.9f -> Danger
                                            ratio >= 0.7f -> Warning
                                            else -> Success
                                        }
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { ratio.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = when {
                                        area.hazardBlocked -> Danger
                                        ratio >= 0.9f -> Danger
                                        ratio >= 0.7f -> Warning
                                        else -> Success
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                        Text(
                            "${(totalCapacity - totalOccupancy).coerceAtLeast(0)} of $totalCapacity spaces remain across all areas.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("SOS response")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ResponseCount("${activeSos.count { it.status == DistressStatus.ACTIVE }}", "Active", Danger, DangerSoft, Modifier.weight(1f))
                            ResponseCount("${acknowledgedSos.size}", "Acknowledged", Warning, WarningSoft, Modifier.weight(1f))
                            ResponseCount("${resolvedSos.size}", "Resolved", Success, SuccessSoft, Modifier.weight(1f))
                        }
                        val resolutionRate = if (distressSignals.isEmpty()) 1f else resolvedSos.size.toFloat() / distressSignals.size
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Resolution rate",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${(resolutionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (resolutionRate >= 0.8f) Success else Warning
                                )
                            }
                            LinearProgressIndicator(
                                progress = { resolutionRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp),
                                color = if (resolutionRate >= 0.8f) Success else Warning,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Text(
                            "Resolution rate counts every signal recorded on this device. Because timestamps track creation only in this prototype, time-to-acknowledge is not computed.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Data coverage")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CoverageRow("Campus locations", "${nodes.size}", "Buildings, facilities, gates, landmarks")
                        CoverageRow("Walking paths", "${edges.size}", "Weighted graph edges used by Dijkstra")
                        CoverageRow("Classrooms", "${classrooms.size}", "Rooms with availability status")
                        CoverageRow("Class slots", "${schedules.size}", "Weekly timetable entries")
                        CoverageRow("Alerts issued", "${alerts.size}", "${alerts.count { it.isActive }} currently active")
                        CoverageRow("Announcements", "${announcements.size}", "Campus-wide notices")
                        CoverageRow("Users", "${users.size}", "${users.count { it.isActive }} active accounts")
                        CoverageRow("Distress history", "${distressSignals.size}", "All locally recorded signals")
                    }
                }
            }

            item {
                SectionHeader("Readiness checks")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ReadinessCheck("Safe routes available", blockedNodes == 0 && blockedEdges == 0, "$blockedNodes blocked locations · $blockedEdges blocked paths")
                        ReadinessCheck("Assembly areas open", evacuationAreas.isNotEmpty() && evacuationAreas.none { it.hazardBlocked }, "${evacuationAreas.count { !it.hazardBlocked }} of ${evacuationAreas.size} open")
                        ReadinessCheck("Capacity within limits", evacuationAreas.none { it.capacity > 0 && it.occupancy > it.capacity }, "Occupancy is capped at capacity")
                        ReadinessCheck("Classroom data complete", classrooms.isNotEmpty() && schedules.isNotEmpty(), "${schedules.size} class slots across ${classrooms.size} rooms")
                        ReadinessCheck("Sync queue clear", syncHealth >= 0.999f, "Queue health ${(syncHealth * 100).toInt()}%")
                        ReadinessCheck("No open distress signals", activeSos.isEmpty(), "${activeSos.size} open signal${if (activeSos.size == 1) "" else "s"}")
                    }
                }
            }

            item {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(13.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.Analytics, contentDescription = null, tint = MutedInk, modifier = Modifier.size(20.dp))
                        Text(
                            "Computed from the local LANDAS database on this device. No analytics leave the device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsTile(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, color = background, shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<Pair<String, Int>>,
    peak: Int,
    accent: Color,
    emptyLabel: String
) {
    if (peak == 0) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                emptyLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp),
                textAlign = TextAlign.Center
            )
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, value) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (value > 0) "$value" else "",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MutedInk,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((72 * (value.toFloat() / peak)).coerceAtLeast(if (value > 0) 4f else 1f).dp)
                            .background(
                                if (value > 0) accent else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                            )
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            data.forEach { (label, _) ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = MutedInk,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RoleProgress(label: String, safe: Int, total: Int, accent: Color) {
    val ratio = if (total == 0) 0f else safe.toFloat() / total
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$safe of $total · ${(ratio * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { ratio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp),
            color = accent,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ResponseCount(value: String, label: String, accent: Color, background: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = background, shape = RoundedCornerShape(11.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = accent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun CoverageRow(label: String, value: String, detail: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun ReadinessCheck(label: String, ok: Boolean, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Icon(
            if (ok) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = if (ok) Success else Warning,
            modifier = Modifier.size(17.dp)
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (ok) Success else Warning
            )
            Text(
                detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
