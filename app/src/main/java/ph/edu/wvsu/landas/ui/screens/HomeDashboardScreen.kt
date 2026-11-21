package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalPhone
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import ph.edu.wvsu.landas.data.AlertEntity
import ph.edu.wvsu.landas.data.AlertSeverity
import ph.edu.wvsu.landas.data.AnnouncementEntity
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.ClassroomEntity
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.ScheduleSlotEntity
import ph.edu.wvsu.landas.data.StoredSession
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
import ph.edu.wvsu.landas.ui.components.BrandMark
import ph.edu.wvsu.landas.ui.components.OnlineStatusPill
import ph.edu.wvsu.landas.ui.components.QuickActionCard
import ph.edu.wvsu.landas.ui.components.SectionHeader
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.util.ClassroomAvailability
import ph.edu.wvsu.landas.util.RoomAvailability
import ph.edu.wvsu.landas.util.formatDateTime
import ph.edu.wvsu.landas.util.formatPercent
import ph.edu.wvsu.landas.util.formatRelative
import ph.edu.wvsu.landas.util.formatTime
import ph.edu.wvsu.landas.util.todayDateLabel
import ph.edu.wvsu.landas.util.todayGreeting
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

@Composable
fun HomeDashboardScreen(
    session: StoredSession,
    user: UserEntity?,
    activeAlert: AlertEntity?,
    announcements: List<AnnouncementEntity>,
    nodes: List<CampusNodeEntity>,
    classrooms: List<ClassroomEntity>,
    schedules: List<ScheduleSlotEntity>,
    attendanceToday: List<AttendanceRecord>,
    safeCounts: Triple<Int, Int, Pair<Int, Int>>,
    unreadAlerts: Int,
    isOnline: Boolean,
    pendingSync: Int,
    isSyncing: Boolean,
    onOpenMap: () -> Unit,
    onMarkSafe: () -> Unit,
    onSos: () -> Unit,
    onClassrooms: () -> Unit,
    onHotline: () -> Unit,
    onQr: () -> Unit,
    onCampusInfo: () -> Unit,
    onDocumentation: () -> Unit,
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    onSync: () -> Unit
) {
    val userAttendance = attendanceToday.firstOrNull { it.userId == session.userId }
    val evacuationAreas = nodes.filter { it.isEvacuationArea && !it.hazardBlocked }
    val now = LocalDateTime.now()
    val availableRooms = classrooms.count { room ->
        val slots = schedules.filter { it.roomId == room.id && it.dayOfWeek == now.dayOfWeek.value }
        ClassroomAvailability.evaluate(room, slots, now).availability == RoomAvailability.AVAILABLE
    }
    var expandedAnnouncements by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 26.dp)
    ) {
        item {
            DashboardHeader(
                session = session,
                activeAlert = activeAlert,
                unreadAlerts = unreadAlerts,
                isOnline = isOnline,
                onNotifications = onNotifications,
                onProfile = onProfile
            )
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (activeAlert != null) {
                    Spacer(Modifier.height(12.dp))
                    ActiveAlertBanner(alert = activeAlert, onClick = onNotifications)
                } else {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        color = SuccessSoft,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(13.dp),
                            horizontalArrangement = Arrangement.spacedBy(9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(20.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Campus status: Normal operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Success)
                                Text("No active emergency alerts. Stay prepared.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            OnlineStatusPill(isOnline = isOnline)
                        }
                    }
                }

                Spacer(Modifier.height(2.dp))
                SectionHeader("Quick actions", actionText = null)
                QuickActionsGrid(
                    userAttendance = userAttendance,
                    onOpenMap = onOpenMap,
                    onMarkSafe = onMarkSafe,
                    onSos = onSos,
                    onClassrooms = onClassrooms,
                    onHotline = onHotline,
                    onQr = onQr,
                    onCampusInfo = onCampusInfo,
                    onDocumentation = onDocumentation
                )

                SectionHeader("Campus at a glance")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CampusGlanceTile(
                        modifier = Modifier.weight(1f),
                        value = "${evacuationAreas.size}",
                        label = "Open evacuation areas",
                        icon = Icons.Rounded.AltRoute,
                        accent = Success
                    )
                    CampusGlanceTile(
                        modifier = Modifier.weight(1f),
                        value = "$availableRooms",
                        label = "Rooms available now",
                        icon = Icons.Rounded.Class,
                        accent = InfoBlue
                    )
                    CampusGlanceTile(
                        modifier = Modifier.weight(1f),
                        value = "${evacuationAreas.sumOf { it.occupancy }}",
                        label = "People marked safe",
                        icon = Icons.Rounded.Groups,
                        accent = SafetyOrange
                    )
                }

                PersonalSafetyCard(
                    session = session,
                    user = user,
                    attendance = userAttendance,
                    onMarkSafe = onMarkSafe
                )

                EvacuationOccupancyCard(
                    areas = nodes.filter { it.isEvacuationArea },
                    onOpenMap = onOpenMap
                )

                DrillAndSyncCard(
                    activeAlert = activeAlert,
                    isOnline = isOnline,
                    pendingSync = pendingSync,
                    isSyncing = isSyncing,
                    onSync = onSync,
                    onOpenAlerts = onNotifications
                )

                if (pendingSync > 0) {
                    SyncQueueCard(pendingSync = pendingSync, isOnline = isOnline, isSyncing = isSyncing, onSync = onSync)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Announcements",
                        modifier = Modifier.weight(1f),
                        actionText = if (announcements.size > 2) {
                            if (expandedAnnouncements) "Show less" else "See all"
                        } else null,
                        onAction = if (announcements.size > 2) {
                            { expandedAnnouncements = !expandedAnnouncements }
                        } else null
                    )
                }

                val visibleAnnouncements = if (expandedAnnouncements) announcements else announcements.take(2)
                if (visibleAnnouncements.isEmpty()) {
                    AppCard {
                        Text(
                            "No announcements yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    visibleAnnouncements.forEach { announcement ->
                        AnnouncementCard(announcement)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "LANDAS · WVSU – Pototan Campus",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    session: StoredSession,
    activeAlert: AlertEntity?,
    unreadAlerts: Int,
    isOnline: Boolean,
    onNotifications: () -> Unit,
    onProfile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(TealDark, TealPrimary)
                )
            )
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.045f),
                radius = 130.dp.toPx(),
                center = Offset(size.width * 0.95f, 40.dp.toPx())
            )
            drawCircle(
                color = Gold.copy(alpha = 0.06f),
                radius = 60.dp.toPx(),
                center = Offset(size.width * 0.02f, 190.dp.toPx())
            )
            val route = listOf(
                Offset(size.width * 0.58f, 40.dp.toPx()),
                Offset(size.width * 0.73f, 76.dp.toPx()),
                Offset(size.width * 0.83f, 58.dp.toPx()),
                Offset(size.width * 1.0f, 108.dp.toPx())
            )
            for (i in 0 until route.lastIndex) {
                drawLine(
                    color = Color.White.copy(alpha = 0.10f),
                    start = route[i],
                    end = route[i + 1],
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }
            route.forEach { drawCircle(Color.White.copy(alpha = 0.18f), radius = 3.dp.toPx(), center = it) }
        }

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrandMark(size = 36.dp)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "LANDAS",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "WVSU – Pototan Campus",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                OnlineStatusPill(
                    isOnline = isOnline,
                    modifier = Modifier.padding(end = 4.dp)
                )
                IconButton(
                    onClick = onNotifications,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.11f), CircleShape)
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadAlerts > 0) {
                                Badge(containerColor = SafetyOrange, contentColor = Navy) {
                                    Text("$unreadAlerts")
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
                Spacer(Modifier.width(9.dp))
                Box(modifier = Modifier) {
                    Avatar(
                        name = session.displayName,
                        size = 40.dp,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .padding(2.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Transparent, CircleShape)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "${todayGreeting()},",
                color = Color.White.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = displayFirstName(session.displayName),
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RolePill(role = session.role)
                Text(
                    todayDateLabel(),
                    color = Color.White.copy(alpha = 0.58f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.height(22.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.10f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (activeAlert == null) Color(0xFF8CE99A) else Gold,
                                CircleShape
                            )
                    )
                    Text(
                        if (activeAlert == null) {
                            "Campus situation is normal"
                        } else {
                            "Emergency drill in progress · ${activeAlert.title}"
                        },
                        color = Color.White.copy(alpha = 0.88f),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        if (activeAlert == null) Icons.Rounded.CloudDone else Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = if (activeAlert == null) Color.White.copy(alpha = 0.65f) else Gold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RolePill(role: String) {
    val label = when (role) {
        Roles.STUDENT -> "Student"
        Roles.FACULTY -> "Faculty"
        Roles.ADMIN -> "Administrator"
        else -> "Visitor"
    }
    Surface(
        color = Gold.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(5.dp).background(Gold, CircleShape))
            Text(label, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ActiveAlertBanner(alert: AlertEntity, onClick: () -> Unit) {
    val isCritical = alert.severity == AlertSeverity.CRITICAL
    val color = if (isCritical) Danger else Warning
    val background = if (isCritical) DangerSoft else WarningSoft
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Rounded.Emergency,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(21.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        when (alert.type) {
                            "EARTHQUAKE" -> "EARTHQUAKE ALERT"
                            "FIRE" -> "FIRE ALERT"
                            "ALL_CLEAR" -> "ALL CLEAR"
                            else -> alert.type
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text("· ${alert.issuedAt.formatRelative()}", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
                }
                Text(
                    alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Tap for details and safety instructions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }
            Icon(Icons.Rounded.AltRoute, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuickActionsGrid(
    userAttendance: AttendanceRecord?,
    onOpenMap: () -> Unit,
    onMarkSafe: () -> Unit,
    onSos: () -> Unit,
    onClassrooms: () -> Unit,
    onHotline: () -> Unit,
    onQr: () -> Unit,
    onCampusInfo: () -> Unit,
    onDocumentation: () -> Unit
) {
    val actions = listOf(
        QuickAction(Icons.Rounded.Map, "Evacuation Map", "Find your safest route", TealPrimary, MaterialTheme.colorScheme.primaryContainer, onOpenMap),
        QuickAction(
            Icons.Rounded.CheckCircle,
            "Marked Safe",
            userAttendance?.let { "Safe at ${it.timestamp.formatTime()}" } ?: "Confirm you are safe",
            Success,
            SuccessSoft,
            onMarkSafe
        ),
        QuickAction(Icons.Rounded.Sos, "Distress Alarm", "Send SOS with location", Danger, DangerSoft, onSos),
        QuickAction(Icons.Rounded.School, "Classrooms", "Check room availability", InfoBlue, InfoSoft, onClassrooms),
        QuickAction(Icons.Rounded.LocalPhone, "Emergency Hotline", "One-tap responder calls", SafetyOrange, Color(0xFFFFF0E5), onHotline),
        QuickAction(Icons.Rounded.QrCode2, "WVSU-PC QR", "Official links & scanner", Color(0xFF6D5FA8), Color(0xFFEEEAF8), onQr),
        QuickAction(Icons.Outlined.Info, "Campus Guide", "Visitor and safety info", Color(0xFFB7791F), Color(0xFFFFF7E0), onCampusInfo),
        QuickAction(Icons.Rounded.Book, "Preparedness", "Guides and documentation", TealPrimary, Color(0xFFE4F1EF), onDocumentation)
    )
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                rowActions.forEach { action ->
                    QuickActionCard(
                        title = action.title,
                        subtitle = action.subtitle,
                        icon = action.icon,
                        iconTint = action.iconTint,
                        iconBackground = action.iconBackground,
                        modifier = Modifier
                            .weight(1f)
                            .height(132.dp),
                        onClick = action.onClick
                    )
                }
                if (rowActions.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private data class QuickAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val iconTint: Color,
    val iconBackground: Color,
    val onClick: () -> Unit
)

@Composable
private fun CampusGlanceTile(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 13.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun PersonalSafetyCard(
    session: StoredSession,
    user: UserEntity?,
    attendance: AttendanceRecord?,
    onMarkSafe: () -> Unit
) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            if (attendance != null) SuccessSoft else MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (attendance != null) Icons.Rounded.CheckCircle else Icons.Rounded.Groups,
                        contentDescription = null,
                        tint = if (attendance != null) Success else TealPrimary,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (attendance != null) "You are marked safe" else "Accountability check",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (attendance != null) {
                            "${attendance.timestamp.formatTime()} · ${attendance.evacuationArea}"
                        } else {
                            "Confirm your safety after reaching an assembly area."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (attendance == null) {
                    TextButton(onClick = onMarkSafe) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Mark safe")
                    }
                } else {
                    StatusPill(
                        if (attendance.synced) "Synced" else "Queued offline",
                        if (attendance.synced) Success else Warning,
                        if (attendance.synced) SuccessSoft else WarningSoft,
                        if (attendance.synced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
                    )
                }
            }
            if (attendance == null && session.role == Roles.FACULTY) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Faculty monitoring is available in the Marked Safe module.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EvacuationOccupancyCard(
    areas: List<CampusNodeEntity>,
    onOpenMap: () -> Unit
) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Evacuation area occupancy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusPill(
                    text = "Live capacity",
                    color = Success,
                    background = SuccessSoft,
                    icon = Icons.Rounded.Groups
                )
            }
            areas.forEach { area ->
                val ratio = if (area.capacity > 0) area.occupancy.toFloat() / area.capacity else 0f
                val barColor = when {
                    area.hazardBlocked -> Danger
                    ratio >= 0.9f -> Danger
                    ratio >= 0.7f -> Warning
                    else -> Success
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(area.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (area.hazardBlocked) "Closed due to hazard" else "${area.occupancy} of ${area.capacity} persons",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (area.hazardBlocked) Danger else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            if (area.hazardBlocked) "CLOSED" else "${(ratio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp),
                        color = barColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            TextButton(
                onClick = onOpenMap,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Open evacuation map")
                Icon(Icons.Rounded.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DrillAndSyncCard(
    activeAlert: AlertEntity?,
    isOnline: Boolean,
    pendingSync: Int,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onOpenAlerts: () -> Unit
) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Latest drill & data status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = onSync, enabled = !isSyncing) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Sync now", modifier = Modifier.size(20.dp))
                    }
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                onClick = onOpenAlerts
            ) {
                Row(
                    Modifier.padding(11.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = Warning, modifier = Modifier.size(19.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            activeAlert?.title ?: "No drill scheduled",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            activeAlert?.let { "Issued ${it.issuedAt.formatDateTime()}" } ?: "The next campus drill will be announced here.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = if (isOnline) Success else Warning,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    if (pendingSync > 0) {
                        "$pendingSync record${if (pendingSync == 1) "" else "s"} queued for synchronization"
                    } else if (isOnline) {
                        "Offline data is synchronized and up to date"
                    } else {
                        "You are offline · local records are protected"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (isOnline && pendingSync > 0) {
                    TextButton(onClick = onSync, enabled = !isSyncing) {
                        Icon(Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sync", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncQueueCard(pendingSync: Int, isOnline: Boolean, isSyncing: Boolean, onSync: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = WarningSoft
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(Icons.Rounded.Sync, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
            Text(
                if (isOnline) "Queued records are ready to sync." else "Offline mode: $pendingSync queued record${if (pendingSync == 1) "" else "s"}. They will sync automatically when you reconnect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (isOnline) {
                TextButton(onClick = onSync, enabled = !isSyncing) {
                    Text("Sync now", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: AnnouncementEntity) {
    AppCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(
                    text = announcement.category,
                    color = TealPrimary,
                    background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    announcement.timestamp.formatRelative(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                announcement.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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

private fun displayFirstName(name: String): String {
    return name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString(" ")
}
