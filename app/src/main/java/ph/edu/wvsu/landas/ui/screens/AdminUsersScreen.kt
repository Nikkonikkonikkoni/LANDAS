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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ph.edu.wvsu.landas.data.AttendanceRecord
import ph.edu.wvsu.landas.data.Roles
import ph.edu.wvsu.landas.data.UserEntity
import ph.edu.wvsu.landas.ui.components.AppCard
import ph.edu.wvsu.landas.ui.components.Avatar
import ph.edu.wvsu.landas.ui.components.EmptyState
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
import ph.edu.wvsu.landas.ui.theme.Success
import ph.edu.wvsu.landas.ui.theme.SuccessSoft
import ph.edu.wvsu.landas.ui.theme.TealDark
import ph.edu.wvsu.landas.ui.theme.TealPrimary
import ph.edu.wvsu.landas.ui.theme.TealSoft
import ph.edu.wvsu.landas.ui.theme.Warning
import ph.edu.wvsu.landas.ui.theme.WarningSoft

private enum class UserFilter(val label: String) {
    ALL("All"),
    STUDENTS("Students"),
    FACULTY("Faculty"),
    ADMINS("Admins"),
    INACTIVE("Inactive")
}

@Composable
fun AdminUsersScreen(
    users: List<UserEntity>,
    attendanceToday: List<AttendanceRecord>,
    currentAdminId: String?,
    onBack: () -> Unit,
    onSetActive: (UserEntity, Boolean) -> Unit,
    onResetPassword: (UserEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(UserFilter.ALL) }
    var resettingUser by remember { mutableStateOf<UserEntity?>(null) }
    var deactivatingUser by remember { mutableStateOf<UserEntity?>(null) }

    val checkedInIds = attendanceToday.map { it.userId }.toSet()
    val students = users.filter { it.role == Roles.STUDENT }
    val faculty = users.filter { it.role == Roles.FACULTY }
    val admins = users.filter { it.role == Roles.ADMIN }
    val inactive = users.filter { !it.isActive }

    val filtered = users
        .filter {
            query.isBlank() ||
                it.fullName.contains(query, ignoreCase = true) ||
                it.schoolId.contains(query, ignoreCase = true) ||
                it.program.contains(query, ignoreCase = true)
        }
        .filter {
            when (filter) {
                UserFilter.ALL -> true
                UserFilter.STUDENTS -> it.role == Roles.STUDENT
                UserFilter.FACULTY -> it.role == Roles.FACULTY
                UserFilter.ADMINS -> it.role == Roles.ADMIN
                UserFilter.INACTIVE -> !it.isActive
            }
        }
        .sortedWith(compareBy({ it.role }, { it.fullName }))

    LandasScaffold(
        title = "Users & access",
        subtitle = "Accounts, roles, and credentials",
        onBack = onBack,
        actions = {
            StatusPill(
                "${users.count { it.isActive }} active",
                Success,
                SuccessSoft,
                Icons.Rounded.CheckCircle
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = TealDark) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(11.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.ManageAccounts, contentDescription = null, tint = Gold, modifier = Modifier.size(21.dp))
                            }
                            Spacer(Modifier.width(9.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Campus accounts",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Deactivating an account blocks sign-in but keeps its records.",
                                    color = Color.White.copy(alpha = 0.64f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            UserStat("${students.size}", "Students", Modifier.weight(1f))
                            UserStat("${faculty.size}", "Faculty", Modifier.weight(1f))
                            UserStat("${admins.size}", "Admins", Modifier.weight(1f))
                            UserStat("${inactive.size}", "Inactive", Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search name, school ID, or program") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    UserFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.label, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${filtered.size} account${if (filtered.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${checkedInIds.size} marked safe today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Rounded.Groups,
                        title = "No accounts match",
                        message = "Adjust the search text or role filter to find a campus account."
                    )
                }
            }

            items(filtered, key = { it.id }) { user ->
                val isSelf = user.id == currentAdminId
                UserAdminCard(
                    user = user,
                    checkedIn = user.id in checkedInIds,
                    isSelf = isSelf,
                    onToggleActive = { active ->
                        if (active) onSetActive(user, true) else deactivatingUser = user
                    },
                    onResetPassword = { resettingUser = user }
                )
            }

            item {
                SectionHeader("Access policy")
                AppCard {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PolicyRow(
                            Icons.Rounded.School,
                            "Students & faculty",
                            "Sign in with school ID and local password. Access map, SOS, attendance, classrooms, QR, and documentation."
                        )
                        HorizontalDivider()
                        PolicyRow(
                            Icons.Rounded.AdminPanelSettings,
                            "Administrators",
                            "Unlock the CDRRMO console for routes, alerts, attendance, distress response, schedules, analytics, and backup."
                        )
                        HorizontalDivider()
                        PolicyRow(
                            Icons.Rounded.Lock,
                            "Storage",
                            "Passwords are salted and hashed locally. Accounts and sessions never leave this prototype device."
                        )
                        Surface(color = WarningSoft, shape = RoundedCornerShape(10.dp)) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Rounded.Key, contentDescription = null, tint = Warning, modifier = Modifier.size(15.dp))
                                Text(
                                    "Resetting a password restores the demo default: landas123 for students and faculty, admin123 for administrators. Share it privately and change it in a production deployment.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    resettingUser?.let { user ->
        val default = if (user.role == Roles.ADMIN) "admin123" else "landas123"
        AlertDialog(
            onDismissRequest = { resettingUser = null },
            icon = { Icon(Icons.Rounded.Key, contentDescription = null, tint = Warning) },
            title = { Text("Reset password for ${user.fullName}?") },
            text = {
                Text("The account password will be reset to the demo default \"$default\". Any previously shared password will stop working immediately.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = user
                    resettingUser = null
                    onResetPassword(target)
                }) {
                    Text("Reset password", color = Warning, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { resettingUser = null }) { Text("Cancel") }
            }
        )
    }

    deactivatingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { deactivatingUser = null },
            icon = { Icon(Icons.Rounded.PersonOff, contentDescription = null, tint = Danger) },
            title = { Text("Deactivate ${user.fullName}?") },
            text = {
                Text("This account will not be able to sign in. Existing attendance, distress, and alert history remain in the database, and the account can be reactivated at any time.")
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = user
                    deactivatingUser = null
                    onSetActive(target, false)
                }) {
                    Text("Deactivate", color = Danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deactivatingUser = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun UserAdminCard(
    user: UserEntity,
    checkedIn: Boolean,
    isSelf: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onResetPassword: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(17.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (user.isActive) MaterialTheme.colorScheme.outlineVariant else Danger.copy(alpha = 0.35f)
        )
    ) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user.fullName, size = 40.dp)
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            user.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isSelf) {
                            StatusPill("You", InfoBlue, InfoSoft)
                        }
                    }
                    Text(
                        "${user.schoolId} · ${roleLabel(user.role)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.program.isNotBlank()) {
                        Text(
                            "${user.program}${if (user.yearLevel.isNotBlank()) " · ${user.yearLevel}" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                StatusPill(
                    if (user.isActive) "Active" else "Inactive",
                    if (user.isActive) Success else Danger,
                    if (user.isActive) SuccessSoft else DangerSoft
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (checkedIn) {
                    StatusPill("Marked safe today", Success, SuccessSoft, Icons.Rounded.CheckCircle)
                } else {
                    StatusPill("No check-in today", MutedInk, MaterialTheme.colorScheme.surfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                if (isSelf) {
                    Text(
                        "Cannot deactivate yourself",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedInk
                    )
                } else {
                    Switch(
                        checked = user.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(checkedTrackColor = Success)
                    )
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (user.isActive) "Account can sign in" else "Sign-in blocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.isActive) Success else Danger,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onResetPassword) {
                    Icon(Icons.Rounded.Key, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset password", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun UserStat(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White.copy(alpha = 0.09f), shape = RoundedCornerShape(10.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                label,
                color = Color.White.copy(alpha = 0.58f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PolicyRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(17.dp))
        Column {
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                body,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun roleLabel(role: String): String = when (role) {
    Roles.STUDENT -> "Student"
    Roles.FACULTY -> "Faculty"
    Roles.ADMIN -> "Administrator"
    else -> "Campus member"
}
