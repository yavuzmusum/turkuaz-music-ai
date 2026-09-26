package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.ui.components.TrackCard
import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.MusicViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val filters = listOf("all" to "All", "recent" to "Recent", "favorites" to "Favorites")

@Composable
fun MyMusicScreen(
    musicViewModel: MusicViewModel,
    onTrackClick: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("all") }
    var trackForMenu by remember { mutableStateOf<String?>(null) }
    var renameDialogFor by remember { mutableStateOf<Pair<String, String>?>(null) }
    val tracks by musicViewModel.tracks.collectAsState()
    val isLoading by musicViewModel.isLoading.collectAsState()

    LaunchedEffect(selectedFilter) { musicViewModel.loadTracks(selectedFilter) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Text("My Music", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Turquoise, selectedLabelColor = androidx.compose.ui.graphics.Color.Black)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Turquoise)
        }

        if (tracks.isEmpty() && !isLoading) {
            Text("Bu filtrede henuz muzik yok.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(tracks) { track ->
                TrackCard(
                    track = track,
                    onClick = { onTrackClick(track.id) },
                    onFavoriteClick = { musicViewModel.toggleFavorite(track.id) },
                    onMenuClick = { trackForMenu = track.id }
                )
            }
        }
    }

    // Menu: Rename, Favorite, Download, Edit, Delete (spec 7)
    trackForMenu?.let { trackId ->
        val track = tracks.find { it.id == trackId }
        ModalBottomSheet(onDismissRequest = { trackForMenu = null }) {
            Column(modifier = Modifier.padding(16.dp)) {
                ListItem(
                    headlineContent = { Text("Rename") },
                    modifier = Modifier.clickableRow {
                        renameDialogFor = trackId to (track?.title ?: "")
                        trackForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text(if (track?.is_favorite == true) "Favorilerden cikar" else "Favorile") },
                    modifier = Modifier.clickableRow {
                        musicViewModel.toggleFavorite(trackId); trackForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Download") },
                    modifier = Modifier.clickableRow { trackForMenu = null }
                )
                ListItem(
                    headlineContent = { Text("Edit") },
                    modifier = Modifier.clickableRow {
                        onTrackClick(trackId); trackForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickableRow {
                        musicViewModel.deleteTrack(trackId) {}
                        trackForMenu = null
                    }
                )
            }
        }
    }

    renameDialogFor?.let { (trackId, currentTitle) ->
        var newTitle by remember { mutableStateOf(currentTitle) }
        AlertDialog(
            onDismissRequest = { renameDialogFor = null },
            title = { Text("Rename") },
            text = {
                OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    musicViewModel.renameTrack(trackId, newTitle)
                    renameDialogFor = null
                }) { Text("Kaydet", color = Turquoise) }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogFor = null }) { Text("Vazgec") }
            }
        )
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
