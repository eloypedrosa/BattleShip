package com.eloypedrosa.battleship.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eloypedrosa.battleship.data.model.User
import com.eloypedrosa.battleship.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LeaderboardViewModel : ViewModel() {
    private val repository = GameRepository()
    private val _leaderboard = MutableStateFlow<List<User>>(emptyList())
    val leaderboard: StateFlow<List<User>> = _leaderboard

    init {
        viewModelScope.launch {
            repository.getLeaderboard().collect { _leaderboard.value = it }
        }
    }
}