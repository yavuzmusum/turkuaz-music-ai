package ai.turkuaz.music.ui.screens.admin

import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.AdminViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private enum class AdminTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Dashboard("Dashboard", Icons.Default.Dashboard),
    Users("Users", Icons.Default.People),
    System("System", Icons.Default.Settings)
}

/**
 * Spec 9: Admin Panel - Dashboard, Users, Music, AI, System.
 * V1'de Music ve AI sekmeleri Dashboard/System icine sadelestirildi,
 * genisleme noktalari backend'de (SystemSetting, admin/music/reported) hazir.
 */
@Composable
fun AdminHomeScreen(adminViewModel: AdminViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(AdminTab.Dashboard) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cikis yap")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Turquoise,
                            selectedTextColor = Turquoise,
                            indicatorColor = Turquoise.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                AdminTab.Dashboard -> AdminDashboardScreen(adminViewModel)
                AdminTab.Users -> AdminUsersScreen(adminViewModel)
                AdminTab.System -> AdminSystemScreen(adminViewModel)
            }
        }
    }
}
