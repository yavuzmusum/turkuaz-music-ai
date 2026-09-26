package ai.turkuaz.music

import ai.turkuaz.music.data.api.RetrofitClient
import ai.turkuaz.music.data.api.SessionManager
import ai.turkuaz.music.navigation.RootNavGraph
import ai.turkuaz.music.ui.theme.TurkuazMusicAITheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager(applicationContext)
        RetrofitClient.init(sessionManager)

        setContent {
            TurkuazMusicAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootNavGraph(sessionManager = sessionManager)
                }
            }
        }
    }
}
