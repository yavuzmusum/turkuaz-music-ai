package ai.turkuaz.music.viewmodel

import ai.turkuaz.music.data.api.RetrofitClient
import ai.turkuaz.music.data.model.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class GenerationState {
    object Idle : GenerationState()
    object Generating : GenerationState()
    data class Success(val track: Track) : GenerationState()
    data class Error(val message: String) : GenerationState()
}

class MusicViewModel : ViewModel() {

    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _selectedTrack = MutableStateFlow<TrackDetail?>(null)
    val selectedTrack: StateFlow<TrackDetail?> = _selectedTrack

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Spec 5: Kullanici prompt yazar -> AI uretir -> sonuc gosterilir. */
    fun generate(prompt: String, title: String? = null) {
        _generationState.value = GenerationState.Generating
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.createTrack(GenerateRequest(prompt = prompt, title = title))
                if (response.isSuccessful && response.body() != null) {
                    _generationState.value = GenerationState.Success(response.body()!!)
                } else {
                    _generationState.value = GenerationState.Error(
                        response.errorBody()?.string() ?: "Muzik uretilemedi"
                    )
                }
            } catch (e: Exception) {
                _generationState.value = GenerationState.Error("Baglanti hatasi: ${e.message}")
            }
        }
    }

    fun resetGenerationState() {
        _generationState.value = GenerationState.Idle
    }

    fun loadTracks(filter: String = "all") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.listTracks(filter)
                if (response.isSuccessful) {
                    _tracks.value = response.body() ?: emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTrackDetail(trackId: String) {
        viewModelScope.launch {
            val response = RetrofitClient.api.getTrack(trackId)
            if (response.isSuccessful) {
                _selectedTrack.value = response.body()
            }
        }
    }

    /** Spec 8: AI ile tekrar duzenleme -> yeni versiyon eklenir. */
    fun editTrack(trackId: String, instruction: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.editTrack(trackId, EditRequest(instruction))
                if (response.isSuccessful) {
                    loadTrackDetail(trackId)
                    onDone()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(trackId: String) {
        viewModelScope.launch {
            RetrofitClient.api.toggleFavorite(trackId)
            loadTracks()
        }
    }

    fun renameTrack(trackId: String, newTitle: String) {
        viewModelScope.launch {
            RetrofitClient.api.renameTrack(trackId, RenameRequest(newTitle))
            loadTracks()
        }
    }

    fun deleteTrack(trackId: String, onDone: () -> Unit) {
        viewModelScope.launch {
            RetrofitClient.api.deleteTrack(trackId)
            loadTracks()
            onDone()
        }
    }
}
