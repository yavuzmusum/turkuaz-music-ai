package ai.turkuaz.music.ui.screens.admin

import ai.turkuaz.music.ui.components.TurkuazPrimaryButton
import ai.turkuaz.music.viewmodel.AdminViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminSystemScreen(adminViewModel: AdminViewModel) {
    val settings by adminViewModel.settings.collectAsState()
    var maintenanceOn by remember { mutableStateOf(false) }
    var dailyLimit by remember { mutableStateOf("10") }
    var announcement by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { adminViewModel.loadSettings() }
    LaunchedEffect(settings) {
        settings.find { it.key == "maintenance_mode" }?.let { maintenanceOn = it.value == "true" }
        settings.find { it.key == "daily_generation_limit" }?.let { dailyLimit = it.value }
        settings.find { it.key == "announcement" }?.let { announcement = it.value }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Sistem Ayarlari", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Bakim modu", fontWeight = FontWeight.Medium)
                Text("Acikken kullanicilar muzik uretemez", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = maintenanceOn,
                onCheckedChange = {
                    maintenanceOn = it
                    adminViewModel.updateSetting("maintenance_mode", it.toString())
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Gunluk uretim limiti (kullanici basina)", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dailyLimit,
            onValueChange = { dailyLimit = it.filter { c -> c.isDigit() } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TurkuazPrimaryButton(text = "Limiti kaydet", modifier = Modifier.fillMaxWidth()) {
            adminViewModel.updateSetting("daily_generation_limit", dailyLimit)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Duyuru", fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = announcement,
            onValueChange = { announcement = it },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TurkuazPrimaryButton(text = "Duyuruyu yayinla", modifier = Modifier.fillMaxWidth()) {
            adminViewModel.updateSetting("announcement", announcement)
        }
    }
}
