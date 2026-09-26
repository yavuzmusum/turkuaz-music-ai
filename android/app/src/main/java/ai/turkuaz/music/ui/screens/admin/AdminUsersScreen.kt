package ai.turkuaz.music.ui.screens.admin

import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.AdminViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminUsersScreen(adminViewModel: AdminViewModel) {
    val users by adminViewModel.users.collectAsState()
    LaunchedEffect(Unit) { adminViewModel.loadUsers() }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Kullanicilar", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(users) { user ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.username, fontWeight = FontWeight.SemiBold)
                            Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${user.track_count} parca", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        AssistChip(
                            onClick = {
                                if (!user.is_admin) {
                                    adminViewModel.setUserStatus(user.id, if (user.status == "active") "blocked" else "active")
                                }
                            },
                            label = { Text(if (user.status == "active") "Aktif" else "Engelli") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (user.status == "active") Turquoise.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                                labelColor = if (user.status == "active") Turquoise else Color.Red
                            )
                        )
                    }
                }
            }
        }
    }
}
