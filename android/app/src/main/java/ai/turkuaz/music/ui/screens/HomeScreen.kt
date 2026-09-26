package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.ui.components.QuickGenreChip
import ai.turkuaz.music.ui.components.TrackCard
import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.MusicViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val quickGenres = listOf("Song", "Beat", "Melody", "Ambient", "Lo-fi", "Hip-hop", "Electronic")

@Composable
fun HomeScreen(
    musicViewModel: MusicViewModel,
    onCreateClick: (initialPrompt: String) -> Unit,
    onTrackClick: (trackId: String) -> Unit
) {
    var prompt by remember { mutableStateOf("") }
    val tracks by musicViewModel.tracks.collectAsState()

    LaunchedEffect(Unit) { musicViewModel.loadTracks("recent") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
    ) {
        item {
            Text("Hos geldin", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Bugun ne olusturmak istiyorsun?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Karanlik, enerjik bir trap beat olustur...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onCreateClick(prompt) },
                enabled = prompt.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise, contentColor = androidx.compose.ui.graphics.Color.Black),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("CREATE", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickGenres) { genre ->
                    QuickGenreChip(label = genre) { onCreateClick("$genre uret") }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Son calismalar", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (tracks.isEmpty()) {
            item {
                Text(
                    "Henuz bir muzik olusturmadin. Yukaridan basla!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            items(tracks) { track ->
                TrackCard(
                    track = track,
                    onClick = { onTrackClick(track.id) },
                    onFavoriteClick = { musicViewModel.toggleFavorite(track.id) },
                    onMenuClick = { }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
