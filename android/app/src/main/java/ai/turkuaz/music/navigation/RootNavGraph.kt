package ai.turkuaz.music.navigation

import ai.turkuaz.music.data.api.SessionManager
import ai.turkuaz.music.ui.screens.AuthScreen
import ai.turkuaz.music.ui.screens.SplashScreen
import ai.turkuaz.music.ui.screens.admin.AdminHomeScreen
import ai.turkuaz.music.viewmodel.AdminViewModel
import ai.turkuaz.music.viewmodel.AuthViewModel
import ai.turkuaz.music.viewmodel.AuthViewModelFactory
import ai.turkuaz.music.viewmodel.MusicViewModel
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_AUTH = "auth"
private const val ROUTE_USER_MAIN = "user_main"
private const val ROUTE_ADMIN_MAIN = "admin_main"

@Composable
fun RootNavGraph(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(sessionManager))
    val musicViewModel: MusicViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()

    NavHost(navController = navController, startDestination = ROUTE_SPLASH) {
        composable(ROUTE_SPLASH) {
            SplashScreen(onFinished = {
                // Oturum var mi kontrol et; varsa dogrudan ilgili ana ekrana git (spec 3).
                val token = runBlocking { sessionManager.tokenFlow.first() }
                val isAdmin = runBlocking { sessionManager.isAdminFlow.first() }
                val destination = when {
                    token == null -> ROUTE_AUTH
                    isAdmin -> ROUTE_ADMIN_MAIN
                    else -> ROUTE_USER_MAIN
                }
                navController.navigate(destination) { popUpTo(ROUTE_SPLASH) { inclusive = true } }
            })
        }
        composable(ROUTE_AUTH) {
            AuthScreen(authViewModel = authViewModel, onAuthenticated = { isAdmin ->
                val destination = if (isAdmin) ROUTE_ADMIN_MAIN else ROUTE_USER_MAIN
                navController.navigate(destination) { popUpTo(ROUTE_AUTH) { inclusive = true } }
            })
        }
        composable(ROUTE_USER_MAIN) {
            UserMainScreen(musicViewModel = musicViewModel, onLogout = {
                authViewModel.logout()
                navController.navigate(ROUTE_AUTH) { popUpTo(0) }
            })
        }
        composable(ROUTE_ADMIN_MAIN) {
            AdminHomeScreen(adminViewModel = adminViewModel, onLogout = {
                authViewModel.logout()
                navController.navigate(ROUTE_AUTH) { popUpTo(0) }
            })
        }
    }
}
