package ai.turkuaz.music.ui.screens.admin

import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.AdminViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminDashboardScreen(adminViewModel: AdminViewModel) {
    val stats by adminViewModel.stats.collectAsState()

    LaunchedEffect(Unit) { adminViewModel.loadDashboard() }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Admin Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Sistem durumu: ${stats?.system_status ?: "yukleniyor..."}",
            color = if (stats?.system_status == "operational") Turquoise else MaterialTheme.colorScheme.error,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Toplam kullanici", stats?.total_users?.toString() ?: "-", Modifier.weight(1f))
            StatCard("Aktif kullanici", stats?.active_users?.toString() ?: "-", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Toplam uretim", stats?.total_generations?.toString() ?: "-", Modifier.weight(1f))
            StatCard("Gunluk uretim", stats?.generations_today?.toString() ?: "-", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
