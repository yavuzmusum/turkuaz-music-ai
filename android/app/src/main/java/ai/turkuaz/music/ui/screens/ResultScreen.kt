package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.data.api.RetrofitClient
import ai.turkuaz.music.ui.theme.Turquoise
import ai.turkuaz.music.viewmodel.MusicViewModel
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun ResultScreen(
    trackId: String,
    musicViewModel: MusicViewModel,
    onEditClick: (trackId: String) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val trackDetail by musicViewModel.selectedTrack.collectAsState()

    LaunchedEffect(trackId) { musicViewModel.loadTrackDetail(trackId) }

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val latest = trackDetail?.versions?.lastOrNull()

    LaunchedEffect(latest?.file_url) {
        latest?.let {
            val fullUrl = RetrofitClient.mediaUrl(it.file_url)
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(fullUrl)))
            exoPlayer.prepare()
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            val duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
            progress = exoPlayer.currentPosition.toFloat() / duration.toFloat()
            kotlinx.coroutines.delay(300)
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Turquoise.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Turquoise, modifier = Modifier.size(72.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            trackDetail?.title ?: "Uretiliyor...",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "v${latest?.version_number ?: 1}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            progress = { progress },
            color = Turquoise,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${(exoPlayer.currentPosition / 1000)}sn", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${latest?.duration_seconds ?: 0}sn", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = {
                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                isPlaying = !isPlaying
            },
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(50))
                .background(Turquoise)
        ) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Oynat/Duraklat",
                tint = Color.Black,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResultActionButton(
                icon = if (trackDetail?.is_favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "Favori"
            ) { musicViewModel.toggleFavorite(trackId) }

            ResultActionButton(icon = Icons.Default.Download, label = "Indir") {
                latest?.let {
                    // Gercek indirme: DownloadManager ile mediaUrl(it.file_url) cagirilir.
                }
            }
            ResultActionButton(icon = Icons.Default.Edit, label = "Duzenle") { onEditClick(trackId) }
            ResultActionButton(icon = Icons.Default.MoreHoriz, label = "Diger") { }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("My Music'e git", color = Turquoise)
        }
    }
}

@Composable
private fun ResultActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = Turquoise)
        }
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
