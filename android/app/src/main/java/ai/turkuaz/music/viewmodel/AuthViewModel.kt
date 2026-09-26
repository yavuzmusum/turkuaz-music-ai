package ai.turkuaz.music.viewmodel

import ai.turkuaz.music.data.api.RetrofitClient
import ai.turkuaz.music.data.api.SessionManager
import ai.turkuaz.music.data.model.LoginRequest
import ai.turkuaz.music.data.model.RegisterRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val isAdmin: Boolean) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun login(email: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.saveSession(body.access_token, body.is_admin)
                    _state.value = AuthUiState.Success(body.is_admin)
                } else {
                    _state.value = AuthUiState.Error("E-posta veya sifre hatali")
                }
            } catch (e: Exception) {
                _state.value = AuthUiState.Error("Baglanti hatasi: ${e.message}")
            }
        }
    }

    fun register(email: String, username: String, password: String) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.register(RegisterRequest(email, username, password))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    sessionManager.saveSession(body.access_token, body.is_admin)
                    _state.value = AuthUiState.Success(body.is_admin)
                } else {
                    _state.value = AuthUiState.Error("Kayit basarisiz. Bilgileri kontrol edin.")
                }
            } catch (e: Exception) {
                _state.value = AuthUiState.Error("Baglanti hatasi: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clear()
            _state.value = AuthUiState.Idle
        }
    }
}
