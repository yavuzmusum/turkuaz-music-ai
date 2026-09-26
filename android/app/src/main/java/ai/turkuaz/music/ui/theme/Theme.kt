package ai.turkuaz.music.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp

// Spec: premium, modern, minimal, klasik DAW karmasikligindan kacinan bir
// gorunum icin koyu (siyah/koyu gri) zemin uzerinde turkuaz vurgu tercih edildi.
private val TurkuazColorScheme = darkColorScheme(
    primary = Turquoise,
    onPrimary = PureBlack,
    secondary = TurquoiseLight,
    background = PureBlack,
    onBackground = PureWhite,
    surface = DarkGray,
    onSurface = PureWhite,
    surfaceVariant = MidGray,
    onSurfaceVariant = Silver,
    error = Color(0xFFFF6B6B)
)

val TurkuazTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp)
)

@Composable
fun TurkuazMusicAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TurkuazColorScheme,
        typography = TurkuazTypography,
        content = content
    )
}
