package com.example.fakeartistny.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.fakeartistny.R
import com.example.fakeartistny.data.PlayerDao
import com.example.fakeartistny.model.Player
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

enum class Phase {
    PLAYER, WORDS, REVEAL, ORDER
}

private val penColors = arrayOf(
    R.color.fg_dark_green,
    R.color.fg_green,
    R.color.fg_orange,
    R.color.fg_red,
    R.color.fg_purple,
    R.color.fg_lilac,
    R.color.fg_blue,
    R.color.fg_light_blue,
    R.color.fg_pink
)

private const val TAG = "GameViewModel"

class GameViewModel(private val playerDao: PlayerDao) : ViewModel() {

    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers().asLiveData()

    // Player currently playing, regardless of phase
    private var _currentPlayer = MutableLiveData<Player?>(null)
    val currentPlayer: LiveData<Player?> = _currentPlayer

    // Current phase of the game
    var currentPhase = Phase.PLAYER
    var playerOrder: List<Player> = listOf()

    // Color for the next added player
    private var _currentColor = MutableLiveData<Int>(R.color.fg_red)
    val currentColor: LiveData<Int> = _currentColor

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

    /**
     * onBack behaviour
     */
    fun back() {
        if (isFirstPlayer()) {
            resetGame()
        } else {
            _currentPlayer.value = previousPlayer()
            if (currentPhase == Phase.WORDS) {
                words.removeLast()
            }
        }
    }

    /**
     * Advances the game, either by phase or by player
     */
    fun next() {
        // T
        when (currentPhase) {
            Phase.PLAYER -> setWordsPhase()
            Phase.WORDS -> {
                if (_currentPlayer.value == playerOrder.last()) {
                    setRevealPhase()
                } else {
                    _currentPlayer.value = nextPlayer()
                }
            }
            Phase.REVEAL -> {
                if (_currentPlayer.value == playerOrder.last()) {
                    setOrderPhase()
                } else {
                    _currentPlayer.value = nextPlayer()
                }
            }
            else -> Phase.PLAYER
        }
    }

    // Returns the next player in the order
    // There must be another way to do this
    private fun nextPlayer(): Player {
        val currentPlayerIndex = playerOrder.indexOf(_currentPlayer.value)
        return playerOrder[currentPlayerIndex + 1]
    }

    // Change current player to the previous player
    private fun previousPlayer(): Player {
        val currentPlayerIndex = playerOrder.indexOf(_currentPlayer.value)
        return playerOrder[currentPlayerIndex - 1]
    }

    fun isFirstPlayer(): Boolean {
        return (_currentPlayer.value == playerOrder.first())
    }

    /**
     *  A game is valid if there's at least 3 players
     */
    fun gameIsValid(): Boolean {
        val players: List<Player> = allPlayers.value ?: listOf()
        return players.size >= 3
    }

    /**
     * Checks if a player is a Fake Artist
     */
    fun isFake(): Boolean {
        return fakes.find { it == currentPlayer.value } != null
    }


    /**
     *  COLORS
     */
    fun cycleColors() {
        val currentColorIndex = penColors.indexOf(currentColor.value)
        Log.d(TAG, currentColorIndex.toString())
        if (currentColorIndex == penColors.size - 1) {
            _currentColor.value = penColors[0]
        } else {
            _currentColor.value = penColors[currentColorIndex + 1]
        }
    }

    fun nextColor() {

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
        cycleColors()
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