package com.example.fakeartistny.ui.viewmodel

import androidx.lifecycle.*
import com.example.fakeartistny.data.PlayerDao
import com.example.fakeartistny.model.Player
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class GameViewModel(
    private val playerDao: PlayerDao
) : ViewModel() {
    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers().asLiveData()

    fun retrievePlayer(id: Long): LiveData<Player> {
        return playerDao.getPlayer(id).asLiveData()
    }

    fun addPlayer(
        name: String,
        color: Int
    ) {
        val player = Player(
            name = name,
            color = color
        )

        viewModelScope.launch {
            playerDao.insert(player)
        }
    }

    fun updatePlayer(
        id: Long,
        name: String,
        isFake: Boolean,
        color: Int
    ) {
        val player = Player(
            id = id,
            name = name,
            isFake = isFake,
            color = color
        )

        viewModelScope.launch {
            playerDao.update(player)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            playerDao.delete(player)
        }
    }

    class GameViewModelFactory(private val playerDao: PlayerDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return GameViewModel(playerDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}