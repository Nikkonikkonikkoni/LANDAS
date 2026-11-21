package ph.edu.wvsu.landas

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import ph.edu.wvsu.landas.data.StoredSession
import ph.edu.wvsu.landas.ui.screens.AdminAlertsScreen
import ph.edu.wvsu.landas.ui.screens.AdminAnalyticsScreen
import ph.edu.wvsu.landas.ui.screens.AdminAttendanceScreen
import ph.edu.wvsu.landas.ui.screens.AdminClassroomsScreen
import ph.edu.wvsu.landas.ui.screens.AdminDashboardScreen
import ph.edu.wvsu.landas.ui.screens.AdminDataScreen
import ph.edu.wvsu.landas.ui.screens.AdminDistressScreen
import ph.edu.wvsu.landas.ui.screens.AdminRoutesScreen
import ph.edu.wvsu.landas.ui.screens.AdminUsersScreen
import ph.edu.wvsu.landas.ui.screens.AlertsScreen
import ph.edu.wvsu.landas.ui.screens.CampusGuideScreen
import ph.edu.wvsu.landas.ui.screens.ClassroomScreen
import ph.edu.wvsu.landas.ui.screens.DocumentationScreen
import ph.edu.wvsu.landas.ui.screens.EmergencyHotlineScreen
import ph.edu.wvsu.landas.ui.screens.EvacuationMapScreen
import ph.edu.wvsu.landas.ui.screens.HomeDashboardScreen
import ph.edu.wvsu.landas.ui.screens.LoginScreen
import ph.edu.wvsu.landas.ui.screens.MarkedSafeScreen
import ph.edu.wvsu.landas.ui.screens.ProfileScreen
import ph.edu.wvsu.landas.ui.screens.QrScreen
import ph.edu.wvsu.landas.ui.screens.SosScreen
import ph.edu.wvsu.landas.ui.screens.SplashScreen
import ph.edu.wvsu.landas.ui.theme.LandasTheme
import ph.edu.wvsu.landas.ui.viewmodel.LandasViewModel

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            LandasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LandasRoot()
                }
            }
        }
    }
}

private enum class Screen {
    HOME,
    MAP,
    MARK_SAFE,
    SOS,
    CLASSROOMS,
    HOTLINE,
    QR,
    ALERTS,
    GUIDE,
    DOCUMENTATION,
    PROFILE,
    ADMIN,
    ADMIN_ROUTES,
    ADMIN_ALERTS,
    ADMIN_ATTENDANCE,
    ADMIN_DISTRESS,
    ADMIN_CLASSROOMS,
    ADMIN_USERS,
    ADMIN_ANALYTICS,
    ADMIN_DATA
}

@Composable
private fun LandasRoot(viewModel: LandasViewModel = viewModel()) {
    val bootstrapping by viewModel.bootstrapping.collectAsStateWithLifecycle()
    val bootstrapError by viewModel.bootstrapError.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var minimumSplashElapsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(900)
        minimumSplashElapsed = true
    }
    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message.text)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val activeSession = session
        when {
            bootstrapping || !minimumSplashElapsed -> SplashScreen(bootError = bootstrapError)
            activeSession == null -> LoginScreen(
                loginState = loginState,
                onLogin = viewModel::login,
                onContinueAsVisitor = viewModel::continueAsVisitor,
                onClearError = viewModel::clearLoginError
            )
            else -> LandasNavigation(
                viewModel = viewModel,
                session = activeSession,
                snackbarHostState = snackbarHostState
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun LandasNavigation(
    viewModel: LandasViewModel,
    session: StoredSession,
    snackbarHostState: SnackbarHostState
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncedAt by viewModel.lastSyncedAt.collectAsStateWithLifecycle()
    val scheduledDrillAt by viewModel.scheduledDrillAt.collectAsStateWithLifecycle()
    val pendingSync by viewModel.pendingSync.collectAsStateWithLifecycle()
    val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val unreadAlertCount by viewModel.unreadAlertCount.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val nodes by viewModel.campusNodes.collectAsStateWithLifecycle()
    val edges by viewModel.campusEdges.collectAsStateWithLifecycle()
    val classrooms by viewModel.classrooms.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val attendanceToday by viewModel.attendanceToday.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendance.collectAsStateWithLifecycle()
    val distressSignals by viewModel.distressSignals.collectAsStateWithLifecycle()
    val occupantsByRole by viewModel.occupantsByRole.collectAsStateWithLifecycle()
    val markSafeState by viewModel.markSafeState.collectAsStateWithLifecycle()
    val sosState by viewModel.sosState.collectAsStateWithLifecycle()

    val stack = remember(session.userId) { mutableStateListOf(Screen.HOME) }
    val stateHolder = rememberSaveableStateHolder()

    // Administrators land in the CDRRMO console; pressing back returns to the campus dashboard.
    LaunchedEffect(session.userId) {
        if (session.role == ph.edu.wvsu.landas.data.Roles.ADMIN && stack.size == 1) {
            stack.add(Screen.ADMIN)
        }
    }

    BackHandler(enabled = stack.size > 1) {
        stack.removeAt(stack.lastIndex)
    }

    fun open(screen: Screen) {
        if (stack.lastOrNull() != screen) stack.add(screen)
    }

    fun goHome() {
        stack.clear()
        stack.add(Screen.HOME)
    }

    val guardVisitor = { message: String ->
        if (session.isVisitor) {
            viewModel.emitMessage(message, true)
            false
        } else {
            true
        }
    }

    stateHolder.SaveableStateProvider(stack.last().name) {
        when (stack.last()) {
            Screen.HOME -> HomeDashboardScreen(
                session = session,
                user = currentUser,
                activeAlert = activeAlert,
                announcements = announcements,
                nodes = nodes,
                classrooms = classrooms,
                schedules = schedules,
                attendanceToday = attendanceToday,
                safeCounts = occupantsByRole,
                unreadAlerts = unreadAlertCount,
                isOnline = isOnline,
                pendingSync = pendingSync,
                isSyncing = isSyncing,
                onOpenMap = { open(Screen.MAP) },
                onMarkSafe = {
                    if (guardVisitor("Sign in with a student or faculty account to mark yourself safe.")) {
                        open(Screen.MARK_SAFE)
                    }
                },
                onSos = {
                    if (guardVisitor("Sign in with a student or faculty account to send a distress signal.")) {
                        open(Screen.SOS)
                    }
                },
                onClassrooms = { open(Screen.CLASSROOMS) },
                onHotline = { open(Screen.HOTLINE) },
                onQr = { open(Screen.QR) },
                onCampusInfo = { open(Screen.GUIDE) },
                onDocumentation = { open(Screen.DOCUMENTATION) },
                onNotifications = {
                    open(Screen.ALERTS)
                    viewModel.markAlertsRead()
                },
                onProfile = { open(Screen.PROFILE) },
                onSync = viewModel::syncNow
            )

            Screen.MAP -> EvacuationMapScreen(
                nodes = nodes,
                edges = edges,
                classrooms = classrooms,
                isOnline = isOnline,
                onBack = { stack.removeAt(stack.lastIndex) },
                onMarkSafe = {
                    if (guardVisitor("Sign in to mark yourself safe after evacuating.")) {
                        open(Screen.MARK_SAFE)
                    }
                }
            )

            Screen.MARK_SAFE -> MarkedSafeScreen(
                user = currentUser,
                nodes = nodes,
                users = users,
                attendanceToday = attendanceToday,
                markSafeState = markSafeState,
                isOnline = isOnline,
                onBack = { stack.removeAt(stack.lastIndex) },
                onOpenMap = { open(Screen.MAP) },
                onMarkSafe = viewModel::markSafe,
                onDismissConfirmation = viewModel::clearMarkSafeState
            )

            Screen.SOS -> SosScreen(
                user = currentUser,
                nodes = nodes,
                sosState = sosState,
                existingSignal = viewModel.currentUserSignal(),
                isOnline = isOnline,
                locationHelper = viewModel.locationHelper,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSend = viewModel::sendSos,
                onCancelSignal = viewModel::resolveOwnDistressSignal,
                onOpenMap = { open(Screen.MAP) }
            )

            Screen.CLASSROOMS -> ClassroomScreen(
                classrooms = classrooms,
                schedules = schedules,
                isOnline = isOnline,
                onBack = { stack.removeAt(stack.lastIndex) }
            )

            Screen.HOTLINE -> EmergencyHotlineScreen(
                onBack = { stack.removeAt(stack.lastIndex) }
            )

            Screen.QR -> QrScreen(
                session = session,
                isOnline = isOnline,
                onBack = { stack.removeAt(stack.lastIndex) }
            )

            Screen.ALERTS -> AlertsScreen(
                alerts = alerts,
                activeAlert = activeAlert,
                unreadCount = unreadAlertCount,
                isOnline = isOnline,
                onBack = { stack.removeAt(stack.lastIndex) },
                onOpenMap = { open(Screen.MAP) },
                onMarkAllRead = viewModel::markAlertsRead
            )

            Screen.GUIDE -> CampusGuideScreen(
                nodes = nodes,
                edges = edges,
                visitorMode = session.isVisitor,
                onBack = { stack.removeAt(stack.lastIndex) },
                onOpenMap = { open(Screen.MAP) },
                onOpenHotline = { open(Screen.HOTLINE) },
                onOpenQr = { open(Screen.QR) }
            )

            Screen.DOCUMENTATION -> DocumentationScreen(
                onBack = { stack.removeAt(stack.lastIndex) }
            )

            Screen.PROFILE -> ProfileScreen(
                session = session,
                user = currentUser,
                isOnline = isOnline,
                pendingSync = pendingSync,
                lastSyncedAt = lastSyncedAt,
                isSyncing = isSyncing,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSync = viewModel::syncNow,
                onSignOut = viewModel::signOut,
                onExportBackup = viewModel::exportBackup,
                onImportBackup = viewModel::importBackup,
                onOpenDocumentation = { open(Screen.DOCUMENTATION) },
                onOpenGuide = { open(Screen.GUIDE) },
                onOpenQr = { open(Screen.QR) },
                onOpenHotline = { open(Screen.HOTLINE) },
                onOpenAdmin = if (session.role == ph.edu.wvsu.landas.data.Roles.ADMIN) {
                    { open(Screen.ADMIN) }
                } else {
                    null
                }
            )

            Screen.ADMIN -> AdminDashboardScreen(
                admin = currentUser,
                isOnline = isOnline,
                activeAlert = activeAlert,
                alerts = alerts,
                users = users,
                attendanceToday = attendanceToday,
                distressSignals = distressSignals,
                nodes = nodes,
                edges = edges,
                pendingSync = pendingSync,
                isSyncing = isSyncing,
                onBack = { stack.removeAt(stack.lastIndex) },
                onOpenRoutes = { open(Screen.ADMIN_ROUTES) },
                onOpenAlerts = { open(Screen.ADMIN_ALERTS) },
                onOpenAttendance = { open(Screen.ADMIN_ATTENDANCE) },
                onOpenDistress = { open(Screen.ADMIN_DISTRESS) },
                onOpenClassrooms = { open(Screen.ADMIN_CLASSROOMS) },
                onOpenUsers = { open(Screen.ADMIN_USERS) },
                onOpenAnalytics = { open(Screen.ADMIN_ANALYTICS) },
                onOpenData = { open(Screen.ADMIN_DATA) },
                onOpenMap = { open(Screen.MAP) },
                onSync = viewModel::syncNow,
                onSignOut = viewModel::signOut
            )

            Screen.ADMIN_ROUTES -> AdminRoutesScreen(
                nodes = nodes,
                edges = edges,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSaveNode = viewModel::saveNode,
                onDeleteNode = viewModel::deleteNode,
                onToggleNodeHazard = viewModel::toggleNodeHazard,
                onSetAreaOccupancy = viewModel::setAreaOccupancy,
                onAddEdge = viewModel::addEdge,
                onDeleteEdge = viewModel::deleteEdge,
                onToggleEdgeHazard = viewModel::toggleEdgeHazard
            )

            Screen.ADMIN_ALERTS -> AdminAlertsScreen(
                alerts = alerts,
                announcements = announcements,
                activeAlert = activeAlert,
                scheduledDrillAt = scheduledDrillAt,
                onBack = { stack.removeAt(stack.lastIndex) },
                onPublishAlert = viewModel::publishAlert,
                onSendAllClear = viewModel::sendAllClear,
                onAddAnnouncement = viewModel::addAnnouncement,
                onScheduleDrill = viewModel::scheduleDrill,
                onCancelDrill = viewModel::cancelScheduledDrill
            )

            Screen.ADMIN_ATTENDANCE -> AdminAttendanceScreen(
                attendanceToday = attendanceToday,
                allAttendance = allAttendance,
                users = users,
                nodes = nodes,
                onBack = { stack.removeAt(stack.lastIndex) },
                onAddManual = viewModel::addManualAttendance,
                onClearToday = viewModel::clearTodayAttendance
            )

            Screen.ADMIN_DISTRESS -> AdminDistressScreen(
                signals = distressSignals,
                nodes = nodes,
                onBack = { stack.removeAt(stack.lastIndex) },
                onAcknowledge = viewModel::acknowledgeDistress,
                onResolve = { viewModel.resolveDistress(it) },
                onOpenMap = { open(Screen.MAP) }
            )

            Screen.ADMIN_CLASSROOMS -> AdminClassroomsScreen(
                classrooms = classrooms,
                schedules = schedules,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSetOverride = viewModel::setClassroomOverride,
                onAddSlot = viewModel::addScheduleSlot,
                onDeleteSlot = viewModel::deleteScheduleSlot
            )

            Screen.ADMIN_USERS -> AdminUsersScreen(
                users = users,
                attendanceToday = attendanceToday,
                currentAdminId = currentUser?.id,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSetActive = viewModel::setUserActive,
                onResetPassword = viewModel::resetUserPassword
            )

            Screen.ADMIN_ANALYTICS -> AdminAnalyticsScreen(
                users = users,
                attendanceToday = attendanceToday,
                allAttendance = allAttendance,
                distressSignals = distressSignals,
                alerts = alerts,
                announcements = announcements,
                nodes = nodes,
                edges = edges,
                classrooms = classrooms,
                schedules = schedules,
                onBack = { stack.removeAt(stack.lastIndex) }
            )

            Screen.ADMIN_DATA -> AdminDataScreen(
                isOnline = isOnline,
                pendingSync = pendingSync,
                isSyncing = isSyncing,
                lastSyncedAt = lastSyncedAt,
                users = users,
                attendance = allAttendance,
                distressSignals = distressSignals,
                alerts = alerts,
                announcements = announcements,
                nodes = nodes,
                edges = edges,
                classrooms = classrooms,
                schedules = schedules,
                onBack = { stack.removeAt(stack.lastIndex) },
                onSync = viewModel::syncNow,
                onExport = viewModel::exportBackup,
                onImport = viewModel::importBackup
            )
        }
    }
}
