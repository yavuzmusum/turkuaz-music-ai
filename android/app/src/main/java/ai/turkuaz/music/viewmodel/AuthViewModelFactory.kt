package ai.turkuaz.music.viewmodel

import ai.turkuaz.music.data.api.SessionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AuthViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sinifi: ${modelClass.name}")
    }
}
