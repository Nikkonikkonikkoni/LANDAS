package ph.edu.wvsu.landas.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.StoredSession
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
import ph.edu.wvsu.landas.ui.components.DetailRow
import ph.edu.wvsu.landas.ui.components.LandasScaffold
import ph.edu.wvsu.landas.ui.components.PrimaryButton
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
import ph.edu.wvsu.landas.util.formatDateTime

@Composable
fun ProfileScreen(
    session: StoredSession,
    user: UserEntity?,
    isOnline: Boolean,
    pendingSync: Int,
    lastSyncedAt: Long?,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onSync: () -> Unit,
    onSignOut: () -> Unit,
    onExportBackup: (android.net.Uri) -> Unit,
    onImportBackup: (android.net.Uri) -> Unit,
    onOpenDocumentation: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenHotline: () -> Unit,
    onOpenAdmin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackup(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirm = true
        }
    }

    LandasScaffold(
        title = "Profile & settings",
        subtitle = "Your identity, data, and app preferences",
        onBack = onBack,
        actions = {
            StatusPill(
                text = if (isOnline) "Online" else "Offline",
                color = if (isOnline) Success else Warning,
                background = if (isOnline) SuccessSoft else WarningSoft,
                icon = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = TealDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(Color.White.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Avatar(session.displayName, size = 64.dp)
                        }
                        Text(
                            session.displayName,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "${session.schoolId} · ${roleLabel(session.role)}",
                            color = Color.White.copy(alpha = 0.68f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Surface(color = Gold.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (session.isVisitor) Icons.Rounded.Person else Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        if (session.isVisitor) "Visitor session" else "Verified campus account",
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            if (user?.isActive == true) {
                                StatusPill("Active", Success, SuccessSoft, Icons.Rounded.CheckCircle)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = onOpenQr,
                                shape = RoundedCornerShape(11.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.QrCode2, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text("My QR", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Surface(
                                onClick = {
                                    clipboard.setText(AnnotatedString(session.schoolId))
                                },
                                shape = RoundedCornerShape(11.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Text("Copy ID", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            if (onOpenAdmin != null && !session.isVisitor) {
                                Surface(
                                    onClick = onOpenAdmin,
                                    shape = RoundedCornerShape(11.dp),
                                    color = Color.White.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                        Text("Admin", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Account details")
                AppCard {
                    Column(Modifier.padding(15.dp)) {
                        DetailRow("Full name", session.displayName)
                        if (user != null) {
                            DetailRow("School ID", user.schoolId)
                            DetailRow("Role", roleLabel(user.role))
                            if (user.program.isNotBlank()) DetailRow("Program", user.program)
                            if (user.yearLevel.isNotBlank()) DetailRow("Year level", user.yearLevel)
                            DetailRow(
                                "Account status",
                                if (user.isActive) "Active" else "Deactivated",
                                valueColor = if (user.isActive) Success else Danger
                            )
                        } else {
                            DetailRow("Access", "Visitor · no account linked")
                            DetailRow("Available tools", "Map, guide, hotlines, QR")
                        }
                        HorizontalDivider(Modifier.padding(vertical = 7.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MutedInk, modifier = Modifier.size(15.dp))
                            Text(
                                "Credentials are validated against the local encrypted cache. No password is sent anywhere.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Offline data")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(39.dp)
                                    .background(if (pendingSync > 0) WarningSoft else SuccessSoft, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (pendingSync > 0) Icons.Rounded.CloudSync else Icons.Rounded.CloudDone,
                                    contentDescription = null,
                                    tint = if (pendingSync > 0) Warning else Success,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (pendingSync > 0) "$pendingSync record${if (pendingSync == 1) "" else "s"} queued" else "Everything is in sync",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    lastSyncedAt?.let { "Last sync ${it.formatDateTime()}" } ?: "No sync has run in this session.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onSync, enabled = !isSyncing) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isSyncing) "Syncing…" else "Sync")
                            }
                        }
                        Text(
                            "The sync routine runs entirely on the device. It marks pending attendance and distress records as delivered to demonstrate queue handling for a future backend.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                SectionHeader("Backup & recovery")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Export a JSON snapshot of campus data, users, schedules, attendance, and distress records. Keep it in a safe location; it contains identity and location data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { exportLauncher.launch("landas-backup-${System.currentTimeMillis()}.json") },
                                shape = RoundedCornerShape(13.dp),
                                color = TealPrimary,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 11.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Export", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Surface(
                                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                                shape = RoundedCornerShape(13.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 11.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.FileUpload, contentDescription = null, tint = Navy, modifier = Modifier.size(17.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Restore", color = Navy, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Surface(color = WarningSoft, shape = RoundedCornerShape(10.dp)) {
                            Text(
                                "Restoring replaces the current campus dataset with the selected backup. This includes user accounts and attendance history.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Quick links")
                ProfileLink(
                    icon = Icons.Rounded.Book,
                    title = "Preparedness & documentation",
                    subtitle = "Emergency guides and system notes",
                    tint = TealPrimary,
                    background = TealSoft,
                    onClick = onOpenDocumentation
                )
                Spacer(Modifier.height(8.dp))
                ProfileLink(
                    icon = Icons.Rounded.Explore,
                    title = "Campus guide",
                    subtitle = "Buildings, assembly areas, and visitor tips",
                    tint = InfoBlue,
                    background = InfoSoft,
                    onClick = onOpenGuide
                )
                Spacer(Modifier.height(8.dp))
                ProfileLink(
                    icon = Icons.Rounded.Phone,
                    title = "Emergency hotline",
                    subtitle = "Campus and municipal responders",
                    tint = SafetyOrange,
                    background = Color(0xFFFFF0E5),
                    onClick = onOpenHotline
                )
            }

            item {
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = MutedInk, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("About this build", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        DetailRow("Application", "LANDAS")
                        DetailRow("Version", "1.0.0 · prototype")
                        DetailRow("Campus", "WVSU – Pototan, Iloilo")
                        DetailRow("Mode", "Offline-first standalone build")
                        Text(
                            "In a real emergency, follow official responders and campus protocols.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                }
            }

            item {
                PrimaryButton(
                    text = if (session.isVisitor) "Exit visitor mode" else "Sign out",
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (session.isVisitor) TealPrimary else Danger,
                    onClick = { showSignOutDialog = true }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "LANDAS · WVSU – Pototan Campus · ph.edu.wvsu.landas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = Danger) },
            title = { Text(if (session.isVisitor) "Exit visitor mode?" else "Sign out of LANDAS?") },
            text = {
                Text(
                    if (session.isVisitor) {
                        "You will return to the sign-in screen. Cached visitor QR access will still be available after signing in again."
                    } else {
                        "Your local data remains on this device. You will need your school ID or administrator credentials to sign back in."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onSignOut()
                }) {
                    Text(if (session.isVisitor) "Exit" else "Sign out", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showImportConfirm && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = {
                showImportConfirm = false
                pendingImportUri = null
            },
            icon = { Icon(Icons.Rounded.Storage, contentDescription = null, tint = Warning) },
            title = { Text("Restore this backup?") },
            text = {
                Text("Current campus data will be replaced by the selected backup file. Export a fresh backup first if you need to keep the current state.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val uri = pendingImportUri
                    showImportConfirm = false
                    pendingImportUri = null
                    if (uri != null) onImportBackup(uri)
                }) {
                    Text("Restore backup", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    pendingImportUri = null
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileLink(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    background: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(background, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = MutedInk, modifier = Modifier.size(17.dp))
        }
    }
}

private fun roleLabel(role: String): String = when (role) {
    Roles.STUDENT -> "Student"
    Roles.FACULTY -> "Faculty"
    Roles.ADMIN -> "Administrator"
    Roles.VISITOR -> "Visitor"
    else -> "Campus member"
}
