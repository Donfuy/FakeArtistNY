package com.example.fakeartistny.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.fakeartistny.data.PlayerDao
import com.example.fakeartistny.model.Player
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

enum class Phase {
    PLAYER, WORDS, REVEAL, ORDER
}

class GameViewModel(private val playerDao: PlayerDao) : ViewModel() {
    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers().asLiveData()

    // Player currently playing, regardless of phase
    private var _currentPlayer = MutableLiveData<Player?>(null)
    val currentPlayer: LiveData<Player?> = _currentPlayer

    // Current phase of the game
    var currentPhase = Phase.PLAYER
    var playerOrder: List<Player> = listOf()

    // List of fakes
    private var fakes: MutableList<Player> = mutableListOf()


    private var _word = MutableLiveData<String>("")
    val word: LiveData<String> = _word

    var words: MutableList<String> = mutableListOf()

    /**
     * GAME FLOW CONTROL
     */

    fun resetGame() {
        _word.value = ""
        fakes = mutableListOf()
        words = mutableListOf()
        currentPhase = Phase.PLAYER
    }

    fun next() {
        // TODO: Check if there's enough players
        when (currentPhase) {
            Phase.PLAYER -> setWordsPhase()
            Phase.WORDS -> {
                if (_currentPlayer.value == playerOrder.last()) {
                    setRevealPhase()
                } else {
                    nextPlayer()
                }
            }
            Phase.REVEAL -> {
                if (_currentPlayer.value == playerOrder.last()) {
                    setOrderPhase()
                } else {
                    nextPlayer()
                }
            }
            else -> Phase.PLAYER
        }
    }

    // Change current player to the next player in the order
    // There must be another way to do this
    private fun nextPlayer() {
        val currentPlayerIndex = playerOrder.indexOf(_currentPlayer.value)
        _currentPlayer.value = playerOrder[currentPlayerIndex + 1]
    }

    /**
     * Checks if a player is a Fake Artist
     */
    fun isFake(): Boolean {
        return fakes.find { it == currentPlayer.value } != null
    }

    /**
     *  WORDS PHASE
     */

    private fun setFakes() {
        // TODO: Variable number of fakes from DataStore
        fakes.add(allPlayers.value!!.random())
    }

    private fun setWordsPhase() {
        // Randomly select the fake artist
        setFakes()

        // Randomize order
        playerOrder = allPlayers.value!!.shuffled()

        // Initialize currentPlayer with the first player
        _currentPlayer.value = playerOrder.first()

        // Set WORDS phase
        currentPhase = Phase.WORDS
    }

    /**
     *  REVEAL PHASE
     */

    private fun setWord() {
        _word.value = words.random()
        Log.d("GameViewModel", _word.value!!)
    }

    private fun setRevealPhase() {

        setWord()

        currentPhase = Phase.REVEAL

        _currentPlayer.value = playerOrder.first()
    }

    /**
     * ORDER PHASE
     */

    private fun setOrderPhase() {
        // Set ORDER phase
        currentPhase = Phase.ORDER

        // Shuffle order again
        playerOrder = playerOrder.shuffled()
    }

    /**
     *  DB Ops
     */

    fun retrievePlayer(id: Long): LiveData<Player> {
        return playerDao.getPlayer(id).asLiveData()
    }

    fun addPlayer(name: String, color: Int) {
        val player = Player(
            name = name,
            color = color
        )

        viewModelScope.launch {
            playerDao.insert(player)
        }
    }

    fun updatePlayer(id: Long, name: String, color: Int) {
        val player = Player(
            id = id,
            name = name,
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