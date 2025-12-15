package com.eloypedrosa.battleship.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eloypedrosa.battleship.data.repository.GameRepository
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = GameRepository()

    private val _navigateToGame = MutableLiveData<Pair<String, String>?>()
    val navigateToGame: LiveData<Pair<String, String>?> = _navigateToGame

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // --- NUEVO: LiveData para el nombre de usuario ---
    private val _currentUsername = MutableLiveData<String>("Cargando capitán...")
    val currentUsername: LiveData<String> = _currentUsername

    init {
        // Al abrir la app, intentamos obtener el nombre inmediatamente
        preloadUser()
    }

    private fun preloadUser() {
        viewModelScope.launch {
            val uid = repository.loginAnonymously()
            if (uid != null) {
                repository.createOrFetchUser(uid) // Asegura que existe
                val user = repository.getUser(uid)
                _currentUsername.value = user?.username ?: "Capitán Desconocido"
            }
        }
    }

    fun onFindGameClicked() {
        _isLoading.value = true
        viewModelScope.launch {
            val playerId = repository.loginAnonymously()
            if (playerId == null) {
                _errorMessage.value = "Error login Firebase"
                _isLoading.value = false
                return@launch
            }

            // Ya no hace falta llamar a createOrFetchUser aquí porque lo hacemos en el init,
            // pero mal no hace dejarlo por seguridad.
            repository.createOrFetchUser(playerId)

            try {
                val gameId = repository.findOrCreateGame(playerId)
                _navigateToGame.value = Pair(gameId, playerId)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}