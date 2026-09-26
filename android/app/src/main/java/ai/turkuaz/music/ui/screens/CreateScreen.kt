package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.ui.components.TurkuazPrimaryButton
import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.GenerationState
import ai.turkuaz.music.viewmodel.MusicViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CreateScreen(
    initialPrompt: String,
    musicViewModel: MusicViewModel,
    onBack: () -> Unit,
    onGenerated: (trackId: String) -> Unit
) {
    var prompt by remember { mutableStateOf(initialPrompt) }
    val state by musicViewModel.generationState.collectAsState()

    LaunchedEffect(state) {
        val s = state
        if (s is GenerationState.Success) {
            onGenerated(s.track.id)
            musicViewModel.resetGenerationState()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
            }
            Text("Olustur", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            placeholder = { Text("Gece araba kullaniyormus hissi veren, karanlik ve atmosferik bir elektronik parca olustur.") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            enabled = state !is GenerationState.Generating
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (state is GenerationState.Generating) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Turquoise)
                Spacer(modifier = Modifier.height(12.dp))
                Text("AI muzigini olusturuyor...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            if (state is GenerationState.Error) {
                Text(
                    (state as GenerationState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            TurkuazPrimaryButton(
                text = "CREATE",
                enabled = prompt.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                musicViewModel.generate(prompt)
            }
        }
    }
}
