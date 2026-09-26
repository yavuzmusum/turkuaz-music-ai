package ai.turkuaz.music.viewmodel

import ai.turkuaz.music.data.api.RetrofitClient
import ai.turkuaz.music.data.model.AdminUser
import ai.turkuaz.music.data.model.DashboardStats
import ai.turkuaz.music.data.model.SystemSetting
import ai.turkuaz.music.data.model.UpdateUserStatusRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val _stats = MutableStateFlow<DashboardStats?>(null)
    val stats: StateFlow<DashboardStats?> = _stats

    private val _users = MutableStateFlow<List<AdminUser>>(emptyList())
    val users: StateFlow<List<AdminUser>> = _users

    private val _settings = MutableStateFlow<List<SystemSetting>>(emptyList())
    val settings: StateFlow<List<SystemSetting>> = _settings

    fun loadDashboard() {
        viewModelScope.launch {
            val response = RetrofitClient.api.adminDashboard()
            if (response.isSuccessful) _stats.value = response.body()
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val response = RetrofitClient.api.adminListUsers()
            if (response.isSuccessful) _users.value = response.body() ?: emptyList()
        }
    }

    fun setUserStatus(userId: String, status: String) {
        viewModelScope.launch {
            RetrofitClient.api.adminUpdateUserStatus(userId, UpdateUserStatusRequest(status))
            loadUsers()
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            val response = RetrofitClient.api.adminGetSettings()
            if (response.isSuccessful) _settings.value = response.body() ?: emptyList()
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            RetrofitClient.api.adminUpdateSetting(SystemSetting(key, value))
            loadSettings()
        }
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch {
            RetrofitClient.api.adminDeleteTrack(trackId)
        }
    }
}
