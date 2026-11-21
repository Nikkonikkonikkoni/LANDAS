package ph.edu.wvsu.landas.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EditLocationAlt
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.GpsOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ph.edu.wvsu.landas.data.CampusNodeEntity
import ph.edu.wvsu.landas.data.DistressSignal
import ph.edu.wvsu.landas.data.DistressStatus
import ph.edu.wvsu.landas.data.NodeKind
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
import ph.edu.wvsu.landas.ui.components.DetailRow
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
import ph.edu.wvsu.landas.ui.components.PulseCircle
import ph.edu.wvsu.landas.ui.components.StatusPill
import ph.edu.wvsu.landas.ui.map.CampusMap
import ph.edu.wvsu.landas.ui.theme.Danger
import ph.edu.wvsu.landas.ui.theme.DangerDark
import ph.edu.wvsu.landas.ui.theme.DangerSoft
import ph.edu.wvsu.landas.ui.theme.Gold
import ph.edu.wvsu.landas.ui.theme.Navy
import ph.edu.wvsu.landas.ui.theme.SafetyOrange
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.viewmodel.SosUiState
import ph.edu.wvsu.landas.util.CampusLocation
import ph.edu.wvsu.landas.util.EmergencyContacts
import ph.edu.wvsu.landas.util.LocationHelper
import ph.edu.wvsu.landas.util.formatDateTime
import ph.edu.wvsu.landas.util.formatTime
import kotlin.math.hypot

@Composable
fun SosScreen(
    user: UserEntity?,
    nodes: List<CampusNodeEntity>,
    sosState: SosUiState,
    existingSignal: DistressSignal?,
    isOnline: Boolean,
    locationHelper: LocationHelper,
    onBack: () -> Unit,
    onSend: (CampusLocation, String) -> Unit,
    onCancelSignal: (String) -> Unit,
    onOpenMap: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentLocation by remember { mutableStateOf(CampusLocation.DEMO_CENTER) }
    var usingGps by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var locationNotice by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    var showSendConfirmation by remember { mutableStateOf(false) }
    var showCancelSignal by remember { mutableStateOf(false) }

    fun updateFromGps() {
        scope.launch {
            isLocating = true
            locationNotice = null
            val located = locationHelper.currentLocation()
            isLocating = false
            if (located != null) {
                currentLocation = located
                usingGps = true
                locationNotice = "GPS location acquired with ±${located.accuracyMeters.toInt()} m accuracy."
            } else {
                usingGps = false
                locationNotice = "GPS is unavailable. Using the campus center pin — tap “Pin manually” to adjust."
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            updateFromGps()
        } else {
            locationNotice = "Location permission denied. You can still pin your location manually."
        }
    }

    LaunchedEffect(Unit) {
        if (locationHelper.hasLocationPermission()) updateFromGps()
    }

    val activeSignal = sosState.sent ?: existingSignal
    if (activeSignal != null) {
        ActiveDistressScreen(
            signal = activeSignal,
            nodes = nodes,
            user = user,
            isOnline = isOnline,
            onBack = onBack,
            onOpenMap = onOpenMap,
            onCancel = { showCancelSignal = true }
        )
        if (showCancelSignal) {
            AlertDialog(
                onDismissRequest = { showCancelSignal = false },
                icon = { Icon(Icons.Rounded.Cancel, contentDescription = null, tint = Danger) },
                title = { Text("Cancel this distress signal?") },
                text = {
                    Text("Only cancel if you are safe and no longer need assistance. Responders may already be on the way.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelSignal = false
                        onCancelSignal(activeSignal.id)
                    }) {
                        Text("Yes, I am safe", color = Danger, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelSignal = false }) { Text("Keep active") }
                }
            )
        }
        return
    }

    LandasScaffold(
        title = "Distress alarm",
        subtitle = "SOS signal and responder location pinning",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = DangerSoft,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Danger, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Use only in a real emergency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Danger)
                        Text(
                            "For immediate danger, call 911. A distress signal shares your location with campus responders.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(182.dp)
                        .clip(CircleShape)
                        .clickable { showSendConfirmation = true },
                    contentAlignment = Alignment.Center
                ) {
                    PulseCircle(color = Danger) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Sos, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                            Text(
                                "SOS",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                "TAP TO SEND",
                                color = Color.White.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.labelSmall,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
            Text(
                "Your identity, GPS position, nearest landmark, and timestamp will be included.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            SectionHeaderSmall("Your location")
            AppCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (usingGps) SuccessSoft else WarningSoft, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (usingGps) Icons.Rounded.GpsFixed else Icons.Rounded.GpsOff,
                                contentDescription = null,
                                tint = if (usingGps) Success else Warning,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isLocating) "Acquiring GPS position…" else if (usingGps) "GPS location acquired" else "Manual / demo campus pin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                nearestLocationName(nodes, currentLocation.mapX, currentLocation.mapY),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isLocating) {
                            CircularProgressIndicator(modifier = Modifier.size(19.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(
                                onClick = {
                                    if (locationHelper.hasLocationPermission()) {
                                        updateFromGps()
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            ) {
                                Icon(Icons.Rounded.MyLocation, contentDescription = "Use GPS", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    DetailRow(
                        "Coordinates",
                        "%.5f, %.5f".format(currentLocation.latitude, currentLocation.longitude)
                    )
                    DetailRow("Map pin", "${currentLocation.mapX.toInt()}, ${currentLocation.mapY.toInt()} · campus coordinates")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPinDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pin manually")
                        }
                        OutlinedButton(
                            onClick = onOpenMap,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.AltRoute, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("View map")
                        }
                    }
                    AnimatedVisibility(visible = locationNotice != null) {
                        Text(
                            locationNotice.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (usingGps) Success else Warning
                        )
                    }
                }
            }

            SectionHeaderSmall("Message for responders (optional)")
            OutlinedTextField(
                value = message,
                onValueChange = { if (it.length <= 240) message = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. Trapped on the 2nd floor, exit is blocked.") },
                leadingIcon = { Icon(Icons.Rounded.Message, contentDescription = null) },
                supportingText = { Text("${message.length}/240", style = MaterialTheme.typography.labelSmall) },
                minLines = 3,
                shape = RoundedCornerShape(14.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            ) {
                listOf("Trapped", "Medical help", "Fire nearby").forEach { quickMessage ->
                    Surface(
                        onClick = { message = quickMessage },
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            quickMessage,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            AppCard {
                Row(
                    modifier = Modifier.padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(user?.fullName ?: "LANDAS User", size = 39.dp)
                    Column(Modifier.weight(1f)) {
                        Text(user?.fullName ?: "Campus member", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${user?.schoolId.orEmpty()} · identity attached automatically",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusPill(
                        if (isOnline) "Immediate" else "Offline queue",
                        if (isOnline) Success else Warning,
                        if (isOnline) SuccessSoft else WarningSoft,
                        if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
                    )
                }
            }

            AnimatedVisibility(visible = sosState.error != null) {
                Surface(color = DangerSoft, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        sosState.error.orEmpty(),
                        color = Danger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            PrimaryButton(
                text = if (sosState.sending) "Sending distress signal…" else "Send distress signal",
                icon = Icons.Rounded.Sos,
                modifier = Modifier.fillMaxWidth(),
                enabled = !sosState.sending,
                containerColor = Danger,
                onClick = { showSendConfirmation = true }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { dial(context, "911") },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Danger.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Rounded.Phone, contentDescription = null, tint = Danger, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call 911", color = Danger)
                }
                OutlinedButton(
                    onClick = { dial(context, EmergencyContacts.contacts.first { it.id == "campus-security" }.phone) },
                    modifier = Modifier
                        .weight(1.4f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Campus Security")
                }
            }
            Text(
                "Prototype: signals are stored on this device and shared with the administrator module. Automatic server delivery can be connected through the documented sync layer.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }

    if (showPinDialog) {
        ManualPinDialog(
            nodes = nodes,
            initialX = currentLocation.mapX,
            initialY = currentLocation.mapY,
            onDismiss = { showPinDialog = false },
            onConfirm = { x, y ->
                currentLocation = CampusLocation(
                    latitude = CampusLocation.CAMPUS_CENTER_LAT - (y - 400f) / CampusLocation.MAP_Y_SCALE,
                    longitude = CampusLocation.CAMPUS_CENTER_LNG + (x - 500f) / CampusLocation.MAP_X_SCALE,
                    mapX = x,
                    mapY = y,
                    accuracyMeters = 0f,
                    fromGps = false
                )
                usingGps = false
                locationNotice = "Manual pin placed at ${nearestLocationName(nodes, x, y)}."
                showPinDialog = false
            }
        )
    }

    if (showSendConfirmation) {
        val nearest = nearestLocationName(nodes, currentLocation.mapX, currentLocation.mapY)
        AlertDialog(
            onDismissRequest = { showSendConfirmation = false },
            icon = { Icon(Icons.Rounded.Sos, contentDescription = null, tint = Danger) },
            title = { Text("Send this distress signal?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Responders will receive your identity, location, and timestamp.", style = MaterialTheme.typography.bodyMedium)
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                        Column(Modifier.padding(10.dp)) {
                            Text("Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(nearest, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(
                                "%.5f, %.5f".format(currentLocation.latitude, currentLocation.longitude),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!isOnline) {
                        Text(
                            "You are offline. The signal is stored locally and queued for automatic synchronization.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSendConfirmation = false
                        onSend(currentLocation, message)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    enabled = !sosState.sending
                ) {
                    Text("Send SOS now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSendConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ActiveDistressScreen(
    signal: DistressSignal,
    nodes: List<CampusNodeEntity>,
    user: UserEntity?,
    isOnline: Boolean,
    onBack: () -> Unit,
    onOpenMap: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    SosAlarmEffect(signal.id)
    LandasScaffold(
        title = "Distress signal active",
        subtitle = "Responder monitoring channel",
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Danger
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .background(Color.White.copy(alpha = 0.16f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Sos, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                    Text(
                        when (signal.status) {
                            DistressStatus.ACKNOWLEDGED -> "Responder acknowledged"
                            else -> "Distress signal is active"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        if (signal.status == DistressStatus.ACKNOWLEDGED) {
                            "A responder has seen your signal. Stay where you are if it is safe."
                        } else {
                            "Your signal has been recorded. Campus responders can see your name and pinned location."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusPill(
                            text = if (signal.status == DistressStatus.ACKNOWLEDGED) "ACKNOWLEDGED" else "AWAITING RESPONSE",
                            color = Color.White,
                            background = Color.White.copy(alpha = 0.18f),
                            icon = Icons.Rounded.AccessTime
                        )
                        StatusPill(
                            text = if (signal.synced) "SYNCED" else "QUEUED OFFLINE",
                            color = Color.White,
                            background = Color.White.copy(alpha = 0.18f),
                            icon = if (signal.synced) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
                        )
                    }
                }
            }

            AppCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Signal details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(5.dp))
                    DetailRow("Name", signal.fullName)
                    DetailRow("Nearest landmark", signal.nearestLocation)
                    DetailRow("Coordinates", "%.5f, %.5f".format(signal.latitude, signal.longitude))
                    DetailRow("Sent", signal.timestamp.formatDateTime())
                    DetailRow("Message", signal.message)
                    DetailRow(
                        "Sync state",
                        if (signal.synced) "Delivered to campus records" else "Local queue · pending sync",
                        valueColor = if (signal.synced) Success else Warning
                    )
                }
            }

            AppCard {
                Column(Modifier.padding(8.dp)) {
                    CampusMap(
                        nodes = nodes,
                        edges = emptyList(),
                        userMapX = signal.mapX,
                        userMapY = signal.mapY,
                        showControls = false,
                        showLegend = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    )
                    Row(
                        modifier = Modifier.padding(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Danger, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "Your pinned location near ${signal.nearestLocation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onOpenMap) { Text("Map") }
                    }
                }
            }

            Surface(color = WarningSoft, shape = RoundedCornerShape(14.dp)) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                    Text(
                        if (isOnline) {
                            "Keep your phone with you and stay alert for a call or message from responders."
                        } else {
                            "You are offline. The signal is protected in the local queue and will sync when a connection returns."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { dial(context, "911") },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Danger)
                ) {
                    Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Call 911")
                }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("I am safe now")
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ManualPinDialog(
    nodes: List<CampusNodeEntity>,
    initialX: Float,
    initialY: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float) -> Unit
) {
    var x by remember { mutableStateOf(initialX) }
    var y by remember { mutableStateOf(initialY) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 30.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Pin your location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Tap anywhere on the map to place your pin.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
                }
                CampusMap(
                    nodes = nodes,
                    edges = emptyList(),
                    userMapX = x,
                    userMapY = y,
                    showControls = false,
                    showLegend = false,
                    onNodeTap = { node ->
                        x = node.x
                        y = node.y
                    },
                    onMapTap = { tappedX, tappedY ->
                        x = tappedX.coerceIn(40f, 960f)
                        y = tappedY.coerceIn(40f, 740f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(11.dp)
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Nearest landmark",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            nearestLocationName(nodes, x, y),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Campus coordinates: ${x.toInt()}, ${y.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                PrimaryButton(
                    text = "Use this location",
                    icon = Icons.Rounded.LocationOn,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onConfirm(x, y) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeaderSmall(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(top = 2.dp)
    )
}

@Composable
private fun SosAlarmEffect(key: String) {
    val context = LocalContext.current
    val toneGenerator = remember {
        runCatching { ToneGenerator(AudioManager.STREAM_ALARM, 72) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { toneGenerator?.release() }
        }
    }
    LaunchedEffect(key) {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 300, 140, 300, 140, 600),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 300, 140, 300, 140, 600), -1)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 1_000)
            delay(1_100)
            toneGenerator?.stopTone()
        }
    }
}

private fun nearestLocationName(nodes: List<CampusNodeEntity>, x: Float, y: Float): String {
    return nodes
        .filter { it.kind != NodeKind.JUNCTION }
        .minByOrNull { hypot(it.x - x, it.y - y) }
        ?.let { "${it.name} area" }
        ?: "Campus grounds"
}

private fun dial(context: Context, rawNumber: String) {
    val number = rawNumber.filter { it.isDigit() || it == '+' }
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}
