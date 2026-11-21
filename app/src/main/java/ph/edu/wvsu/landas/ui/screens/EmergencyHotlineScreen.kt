package ph.edu.wvsu.landas.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalPolice
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft
import ph.edu.wvsu.landas.util.EmergencyContact
import ph.edu.wvsu.landas.util.EmergencyContacts

@Composable
fun EmergencyHotlineScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val grouped = EmergencyContacts.contacts.groupBy { it.category }

    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TealDark)
                    .padding(start = 18.dp, end = 18.dp, top = 52.dp, bottom = 18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, tint = Gold)
                        Spacer(Modifier.width(9.dp))
                        Column {
                            Text(
                                "Emergency hotline",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Direct calls to responders and campus offices",
                                color = Color.White.copy(alpha = 0.68f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Surface(
                        onClick = { dialNumber(context, "911") },
                        shape = RoundedCornerShape(18.dp),
                        color = Danger
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Emergency, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "National emergency line",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    "911",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "Police · Fire · Medical · Rescue",
                                    color = Color.White.copy(alpha = 0.75f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Surface(
                                color = Color.White,
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Rounded.Call,
                                    contentDescription = "Call 911",
                                    tint = Danger,
                                    modifier = Modifier
                                        .padding(11.dp)
                                        .size(22.dp)
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        HotlineChip("Stay calm", Icons.Rounded.Info)
                        HotlineChip("State your location", Icons.Rounded.Shield)
                        HotlineChip("Follow instructions", Icons.Rounded.Security)
                    }
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Surface(color = WarningSoft, shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                        Text(
                            "Numbers were verified against public listings for this prototype. Confirm them with WVSU-PC and the LGU before field deployment.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        grouped.forEach { (category, contacts) ->
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        when (category) {
                            "NATIONAL" -> "National responders"
                            "CAMPUS" -> "Campus offices"
                            "MUNICIPAL" -> "Pototan responders"
                            else -> "Provincial coordination"
                        }
                    )
                }
            }
            items(contacts, key = { it.id }) { contact ->
                HotlineContactCard(
                    contact = contact,
                    onCall = { dialNumber(context, contact.phone) },
                    onCopy = {
                        clipboard.setText(AnnotatedString(contact.displayPhone))
                        context.showToast("${contact.displayPhone} copied")
                    }
                )
            }
        }

        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = Success, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("When you call", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        listOf(
                            "Give your exact location and nearest landmark.",
                            "Describe the emergency and how many people need help.",
                            "Mention injuries, fire, or trapped persons.",
                            "Stay on the line until the dispatcher ends the call."
                        ).forEachIndexed { index, tip ->
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Text(
                                    "${index + 1}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                                Text(
                                    tip,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Text(
                    "If the line is busy, call 911 or proceed to the nearest evacuation area and notify campus security in person.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    Surface(
        onClick = onBack,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.16f),
        modifier = Modifier
            .align(Alignment.TopStart)
            .statusBarsPadding()
            .padding(12.dp)
    ) {
        Icon(
            androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .padding(9.dp)
                .size(20.dp)
        )
    }
    }
}

@Composable
private fun HotlineChip(label: String, icon: ImageVector) {
    Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
            Text(label, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HotlineContactCard(
    contact: EmergencyContact,
    onCall: () -> Unit,
    onCopy: () -> Unit
) {
    val accent = when {
        contact.category == "CAMPUS" -> TealPrimary
        contact.category == "MUNICIPAL" && contact.name.contains("Fire", ignoreCase = true) -> SafetyOrange
        contact.category == "MUNICIPAL" -> InfoBlue
        contact.category == "PROVINCIAL" -> Color(0xFF6D5FA8)
        else -> Danger
    }
    val icon = when {
        contact.name.contains("Fire", ignoreCase = true) -> Icons.Rounded.LocalFireDepartment
        contact.name.contains("Police", ignoreCase = true) -> Icons.Rounded.LocalPolice
        contact.name.contains("Health", ignoreCase = true) -> Icons.Rounded.LocalHospital
        contact.category == "CAMPUS" -> Icons.Rounded.Security
        else -> Icons.Rounded.Emergency
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.12f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        contact.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (contact.primary) {
                        StatusPill("Priority", Danger, DangerSoft)
                    }
                }
                Text(
                    contact.displayPhone,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                Text(
                    contact.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    contact.available,
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedInk
                )
            }
            IconButton(
                onClick = onCopy,
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy number", modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(2.dp))
            Surface(onClick = onCall, shape = CircleShape, color = accent) {
                Icon(
                    Icons.Rounded.Call,
                    contentDescription = "Call ${contact.name}",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(19.dp)
                )
            }
        }
    }
}

private fun dialNumber(context: Context, rawNumber: String) {
    val number = rawNumber.filter { it.isDigit() || it == '+' }
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

private fun Context.showToast(message: String) {
    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
}
