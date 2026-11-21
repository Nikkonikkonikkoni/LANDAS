package ph.edu.wvsu.landas.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.AltRoute
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FireTruck
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.Storm
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tsunami
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import ph.edu.wvsu.landas.ui.theme.TealSoft
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft

private data class GuideTopic(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val summary: String,
    val steps: List<String>
)

private val preparednessTopics = listOf(
    GuideTopic(
        title = "Earthquake",
        icon = Icons.Rounded.Tsunami,
        accent = Danger,
        summary = "Drop, cover, and hold. Evacuate only after the shaking stops.",
        steps = listOf(
            "Drop to your hands and knees, cover your head and neck, and hold on to sturdy furniture.",
            "Stay away from windows, shelves, and hanging objects.",
            "Once the shaking stops, check yourself and nearby people for injuries.",
            "Use the LANDAS map to find the nearest open assembly area and follow the highlighted route.",
            "Mark yourself safe so responders can account for you.",
            "Expect aftershocks and avoid damaged structures until an all-clear is issued."
        )
    ),
    GuideTopic(
        title = "Fire",
        icon = Icons.Rounded.FireTruck,
        accent = SafetyOrange,
        summary = "Raise the alarm, leave immediately, and stay low under smoke.",
        steps = listOf(
            "Pull the nearest fire alarm and call for help.",
            "Evacuate through the nearest safe exit; never use elevators.",
            "Stay low and cover your nose and mouth if smoke is present.",
            "Close doors behind you only if it is safe and quick to do so.",
            "Proceed to the designated assembly area and keep clear of fire lanes.",
            "Do not re-enter the building until responders declare it safe."
        )
    ),
    GuideTopic(
        title = "Typhoon & flooding",
        icon = Icons.Rounded.Storm,
        accent = InfoBlue,
        summary = "Monitor official advisories and prepare to move to higher ground.",
        steps = listOf(
            "Store drinking water, food, medicines, a flashlight, and a charged power bank.",
            "Secure loose objects and move valuables away from windows.",
            "Follow MDRRMO evacuation orders; do not attempt to cross flooded roads.",
            "Avoid rivers, drainage canals, and electrical posts.",
            "Use the hotline directory if you need rescue or medical assistance.",
            "Return home only after authorities issue an all-clear."
        )
    )
)

private data class AppInfo(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val body: String
)

private val appInfoItems = listOf(
    AppInfo(
        title = "What is LANDAS?",
        icon = Icons.Rounded.AltRoute,
        accent = TealPrimary,
        body = "LANDAS stands for Location-Aware Navigation and Disaster Assistance System. It is a standalone Android prototype for WVSU – Pototan Campus that combines an offline campus map, Dijkstra-based evacuation routing, identity-verified attendance during emergencies, SOS signaling, classroom availability, QR tools, and an administrator response console."
    ),
    AppInfo(
        title = "How evacuation routing works",
        icon = Icons.Rounded.Map,
        accent = Success,
        body = "Campus buildings, walkways, gates, and assembly areas are stored as a weighted graph in the local Room database. The pure Kotlin Dijkstra router ignores hazard-blocked nodes and edges, finds the nearest open assembly area by walking distance, and falls back to another area when the closest one is at capacity. Turn-by-turn steps are generated from the resulting node sequence and computed bearings."
    ),
    AppInfo(
        title = "Offline-first design",
        icon = Icons.Rounded.CloudOff,
        accent = InfoBlue,
        body = "All maps, schedules, alerts, users, attendance records, and distress signals are stored on the device. No network connection is required to open the map or compute a route. New attendance check-ins and SOS signals are tagged as pending until the local sync routine runs; in this prototype the sync is simulated because no backend server is bundled."
    ),
    AppInfo(
        title = "Roles and access",
        icon = Icons.Rounded.Groups,
        accent = Color(0xFF6D5FA8),
        body = "Students and faculty can view the dashboard, navigate, check classrooms, mark themselves safe, and send distress signals. Visitors can use the map, guide, hotlines, and QR tools without an account. Administrators get additional modules for route editing, hazard and capacity control, alert broadcasting, attendance, distress response, classroom scheduling, user management, analytics, and backup."
    ),
    AppInfo(
        title = "Data privacy",
        icon = Icons.Rounded.Lock,
        accent = Warning,
        body = "LANDAS stores only the information needed by the prototype: school ID, name, role, program, and section. QR codes contain the same identity fields and are generated entirely on the device. The SOS map pin is approximate and intended for demonstration; a production deployment must add authenticated transport, encryption at rest, and a formal data-retention policy."
    ),
    AppInfo(
        title = "Prototype limitations",
        icon = Icons.Rounded.Info,
        accent = Danger,
        body = "Campus coordinates, building geometry, office descriptions, schedules, and contact numbers are demonstration data and must be validated before real use. There is no live backend, push messaging, or real dispatch integration. Automatic Android notifications are local. In an actual emergency, always follow official responders and campus protocols."
    )
)

@Composable
fun DocumentationScreen(
    onBack: () -> Unit
) {
    var expandedInfo by remember { mutableStateOf<String?>(appInfoItems.first().title) }
    var expandedTopic by remember { mutableStateOf<String?>(null) }

    LandasScaffold(
        title = "Preparedness & docs",
        subtitle = "Guides, system design, and prototype notes",
        onBack = onBack,
        actions = {
            StatusPill("v1.0", TealPrimary, TealSoft, Icons.Rounded.Book)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = TealDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = Gold, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Be ready before it matters",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Simple actions that save lives",
                                    color = Color.White.copy(alpha = 0.64f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Text(
                            "These guides are quick references for common campus emergencies. They do not replace official training, responder instructions, or local ordinances.",
                            color = Color.White.copy(alpha = 0.76f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            DocBadge(Icons.Rounded.Shield, "Stay calm")
                            DocBadge(Icons.Rounded.Sos, "Call for help")
                            DocBadge(Icons.Rounded.Person, "Account for others")
                        }
                    }
                }
            }

            item {
                SectionHeader("Emergency preparedness")
            }

            items(preparednessTopics.size) { index ->
                val topic = preparednessTopics[index]
                TopicCard(
                    title = topic.title,
                    icon = topic.icon,
                    accent = topic.accent,
                    summary = topic.summary,
                    steps = topic.steps,
                    expanded = expandedTopic == topic.title,
                    onToggle = {
                        expandedTopic = if (expandedTopic == topic.title) null else topic.title
                    }
                )
            }

            item {
                SectionHeader("About LANDAS")
            }

            items(appInfoItems.size) { index ->
                val item = appInfoItems[index]
                InfoCard(
                    title = item.title,
                    icon = item.icon,
                    accent = item.accent,
                    body = item.body,
                    expanded = expandedInfo == item.title,
                    onToggle = {
                        expandedInfo = if (expandedInfo == item.title) null else item.title
                    }
                )
            }

            item {
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Code, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text("Technical snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        DetailLine("Platform", "Native Android · Jetpack Compose · Material 3")
                        DetailLine("Storage", "Room · DataStore session cache")
                        DetailLine("Routing", "Embedded Dijkstra with hazard and capacity rules")
                        DetailLine("Location", "Google Play Services Fused Location")
                        DetailLine("QR", "ZXing encoder and embedded scanner")
                        DetailLine("Sync", "Local queue (no backend in prototype)")
                        DetailLine("Minimum Android", "8.0 (API 26)")
                        Text(
                            "Package: ph.edu.wvsu.landas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedInk,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Public, contentDescription = null, tint = MutedInk, modifier = Modifier.size(24.dp))
                        Text(
                            "LANDAS · WVSU – Pototan Campus",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Location-Aware Navigation and Disaster Assistance System · prototype build 1.0.0",
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
private fun TopicCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    summary: String,
    steps: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) 3 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MutedInk
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    steps.forEachIndexed { index, step ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(19.dp)
                                    .background(accent.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent
                                )
                            }
                            Text(
                                step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    accent: Color,
    body: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        onClick = onToggle
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Rounded.ExpandMore else Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MutedInk,
                    modifier = Modifier.size(19.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Text(
                    body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun DocBadge(icon: ImageVector, label: String) {
    Surface(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.72f), modifier = Modifier.size(12.dp))
            Text(label, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MutedInk,
            modifier = Modifier.width(112.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = Navy,
            modifier = Modifier.weight(1f)
        )
    }
}
