package ai.turkuaz.music.ui.screens

import ai.turkuaz.music.ui.theme.Turquoise
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(targetValue = if (visible) 1f else 0f, animationSpec = tween(700), label = "splashAlpha")
    val scale by animateFloatAsState(targetValue = if (visible) 1f else 0.85f, animationSpec = tween(700), label = "splashScale")

    LaunchedEffect(Unit) {
        visible = true
        delay(1400)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { this.alpha = alpha; scaleX = scale; scaleY = scale }
        ) {
            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Turquoise, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "TURKUAZ",
                color = Turquoise,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Text(
                "MUSIC AI",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                letterSpacing = 6.sp
            )
        }
    }
}
