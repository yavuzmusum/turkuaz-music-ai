package ai.turkuaz.music.navigation

import ai.turkuaz.music.ui.screens.*
import ai.turkuaz.music.viewmodel.MusicViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ai.turkuaz.music.ui.theme.Turquoise
import androidx.compose.foundation.layout.padding
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_HOME = "home"
private const val ROUTE_CREATE = "create/{prompt}"
private const val ROUTE_RESULT = "result/{trackId}"
private const val ROUTE_MY_MUSIC = "my_music"
private const val ROUTE_EDIT = "edit/{trackId}"

@Composable
fun UserMainScreen(musicViewModel: MusicViewModel, onLogout: () -> Unit) {
    val innerNavController = rememberNavController()
    var showBottomBar by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { innerNavController.navigate(ROUTE_HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Turquoise, selectedTextColor = Turquoise)
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { innerNavController.navigate(ROUTE_MY_MUSIC) },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "My Music") },
                        label = { Text("My Music") },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Turquoise, selectedTextColor = Turquoise)
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = innerNavController,
            startDestination = ROUTE_HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(ROUTE_HOME) {
                showBottomBar = true
                HomeScreen(
                    musicViewModel = musicViewModel,
                    onCreateClick = { prompt ->
                        innerNavController.navigate("create/${encode(prompt)}")
                    },
                    onTrackClick = { trackId -> innerNavController.navigate("result/$trackId") }
                )
            }
            composable(ROUTE_CREATE) { backStackEntry ->
                showBottomBar = false
                val prompt = decode(backStackEntry.arguments?.getString("prompt") ?: "")
                CreateScreen(
                    initialPrompt = prompt,
                    musicViewModel = musicViewModel,
                    onBack = { innerNavController.popBackStack() },
                    onGenerated = { trackId ->
                        innerNavController.navigate("result/$trackId") {
                            popUpTo(ROUTE_HOME)
                        }
                    }
                )
            }
            composable(ROUTE_RESULT) { backStackEntry ->
                showBottomBar = false
                val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
                ResultScreen(
                    trackId = trackId,
                    musicViewModel = musicViewModel,
                    onEditClick = { id -> innerNavController.navigate("edit/$id") },
                    onDone = {
                        innerNavController.navigate(ROUTE_MY_MUSIC) { popUpTo(ROUTE_HOME) }
                    }
                )
            }
            composable(ROUTE_MY_MUSIC) {
                showBottomBar = true
                MyMusicScreen(
                    musicViewModel = musicViewModel,
                    onTrackClick = { trackId -> innerNavController.navigate("result/$trackId") }
                )
            }
            composable(ROUTE_EDIT) { backStackEntry ->
                showBottomBar = false
                val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
                EditScreen(
                    trackId = trackId,
                    musicViewModel = musicViewModel,
                    onBack = { innerNavController.popBackStack() }
                )
            }
        }
    }
}

private fun encode(text: String) = java.net.URLEncoder.encode(text, "UTF-8")
private fun decode(text: String) = java.net.URLDecoder.decode(text, "UTF-8")
