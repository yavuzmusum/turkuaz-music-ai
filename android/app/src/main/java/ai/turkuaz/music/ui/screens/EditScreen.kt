package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.ui.components.TurkuazPrimaryButton
import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.MusicViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val quickInstructions = listOf(
    "Daha enerjik yap",
    "Bass'i guclendir",
    "Daha karanlik bir atmosfer olustur",
    "Tempoyu artir",
    "Daha sakin yap"
)

@Composable
fun EditScreen(
    trackId: String,
    musicViewModel: MusicViewModel,
    onBack: () -> Unit
) {
    var instruction by remember { mutableStateOf("") }
    val trackDetail by musicViewModel.selectedTrack.collectAsState()
    val isLoading by musicViewModel.isLoading.collectAsState()

    LaunchedEffect(trackId) { musicViewModel.loadTrackDetail(trackId) }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri") }
            Text(trackDetail?.title ?: "", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("AI ile tekrar duzenle", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = instruction,
            onValueChange = { instruction = it },
            placeholder = { Text("Orn: Bass'i guclendir") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(quickInstructions) { text ->
                AssistChip(onClick = { instruction = text }, label = { Text(text) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Turquoise)
        } else {
            TurkuazPrimaryButton(
                text = "Yeni versiyon olustur",
                enabled = instruction.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                musicViewModel.editTrack(trackId, instruction) { instruction = "" }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.History, contentDescription = null, tint = Turquoise, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Versiyon gecmisi", fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Spec 8: Midnight Drive -> Version 1, Version 2, Version 3 ... hicbiri silinmez.
        trackDetail?.versions?.reversed()?.forEach { v ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Turquoise, shape = androidx.compose.foundation.shape.CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Version ${v.version_number}", fontWeight = FontWeight.Medium)
                    Text(
                        v.prompt_used,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
