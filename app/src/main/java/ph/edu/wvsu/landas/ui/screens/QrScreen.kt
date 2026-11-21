package ph.edu.wvsu.landas.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.StoredSession
import ph.edu.wvsu.landas.ui.components.AppCard
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
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.TealDark
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.util.QrUtils

private data class OfficialLink(
    val label: String,
    val description: String,
    val url: String
)

private val officialLinks = listOf(
    OfficialLink(
        "WVSU official website",
        "University news, admissions, and campus directory",
        "https://wvsu.edu.ph"
    ),
    OfficialLink(
        "WVSU – Pototan Campus",
        "Campus profile and history",
        "https://wvsu.edu.ph/satellite-campus/pototan-campus"
    ),
    OfficialLink(
        "Pototan municipal government",
        "LGU services, disaster updates, and contacts",
        "https://pototan.gov.ph"
    ),
    OfficialLink(
        "Iloilo Provincial DRRMO",
        "Provincial disaster response information",
        "https://iloilo.gov.ph/en/node/56"
    )
)

@Composable
fun QrScreen(
    session: StoredSession,
    isOnline: Boolean,
    onBack: () -> Unit
) {
    var scanResult by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            scanResult = result.contents
        }
    }

    fun launchScanner() {
        scanLauncher.launch(
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Align the WVSU-PC QR code inside the frame")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
        )
    }

    val identityContent = remember(session) {
        buildString {
            append("LANDAS|WVSU-PC|")
            append(session.schoolId)
            append('|')
            append(session.displayName)
            append('|')
            append(session.role)
        }
    }
    val identityBitmap = remember(identityContent) { QrUtils.generate(identityContent, 720) }

    LandasScaffold(
        title = "WVSU-PC QR",
        subtitle = "Identity code, official links, and scanner",
        onBack = onBack,
        actions = {
            StatusPill(
                text = if (isOnline) "Online" else "Offline",
                color = if (isOnline) Success else Warning,
                background = if (isOnline) SuccessSoft else WarningSoft
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = TealDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusPill(
                            text = if (session.isVisitor) "VISITOR PASS" else "CAMPUS IDENTITY",
                            color = Navy,
                            background = Gold
                        )
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            modifier = Modifier.border(3.dp, Color.White, RoundedCornerShape(18.dp))
                        ) {
                            Image(
                                bitmap = identityBitmap.asImageBitmap(),
                                contentDescription = "Your LANDAS QR code",
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(206.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                session.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${session.schoolId} · ${roleLabel(session.role)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.68f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        val uri = QrUtils.shareQr(context, identityBitmap, "landas-qr")
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(share, "Share your LANDAS QR"))
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Share", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(identityContent))
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Copy code", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Text(
                            "Present this code at the campus gate or scan it yourself to test the reader. It contains only your school ID, name, and role.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Surface(
                    onClick = { launchScanner() },
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(TealPrimary, RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Scan a WVSU-PC QR code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Camera-based scanning works fully offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = TealPrimary)
                    }
                }
            }

            item {
                SectionHeader("Official campus links")
                Text(
                    "Each code opens a verified public page. Long-press the URL to copy it when you are offline.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            items(officialLinks.size) { index ->
                val link = officialLinks[index]
                OfficialLinkCard(
                    link = link,
                    onOpen = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                        }
                    },
                    onCopy = { clipboard.setText(AnnotatedString(link.url)) }
                )
            }

            item {
                AppCard {
                    Row(
                        modifier = Modifier.padding(13.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                        Text(
                            "QR generation is performed entirely on the device. LANDAS does not upload identity codes or scan history.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (scanResult != null) {
        ScanResultDialog(
            content = scanResult.orEmpty(),
            onDismiss = { scanResult = null },
            onOpen = { value ->
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value)))
                }
                scanResult = null
            },
            onCopy = {
                clipboard.setText(AnnotatedString(scanResult.orEmpty()))
            }
        )
    }
}

@Composable
private fun OfficialLinkCard(
    link: OfficialLink,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    val qrBitmap = remember(link.url) { QrUtils.generate(link.url, 420) }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(color = Color.White, shape = RoundedCornerShape(10.dp)) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR code for ${link.label}",
                    modifier = Modifier
                        .padding(5.dp)
                        .size(64.dp)
                )
            }
            Column(Modifier.weight(1f)) {
                Text(link.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    link.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.Link, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(12.dp))
                    Text(
                        link.url.removePrefix("https://"),
                        style = MaterialTheme.typography.labelSmall,
                        color = TealPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                Icons.Rounded.OpenInNew,
                contentDescription = "Open link",
                tint = TealPrimary,
                modifier = Modifier
                    .size(36.dp)
                    .clickable(onClick = onOpen)
                    .padding(6.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 13.dp, end = 13.dp, bottom = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            TextButton(onClick = onOpen) {
                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Open", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = onCopy) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Copy URL", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun ScanResultDialog(
    content: String,
    onDismiss: () -> Unit,
    onOpen: (String) -> Unit,
    onCopy: () -> Unit
) {
    val startsWithUrl = content.startsWith("http://", ignoreCase = true) ||
        content.startsWith("https://", ignoreCase = true)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SuccessSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Success, modifier = Modifier.size(26.dp))
                }
                Text("QR code scanned", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "The content below was read from the scanned code.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        content,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(11.dp)
                    )
                }
                if (startsWithUrl) {
                    PrimaryButton(
                        text = "Open in browser",
                        icon = Icons.Rounded.OpenInNew,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpen(content) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Copy")
                    }
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text("Close")
                    }
                }
            }
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
