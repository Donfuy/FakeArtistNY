package com.example.fakeartistny.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.example.fakeartistny.data.PlayerDao
import com.example.fakeartistny.model.Player
import com.example.fakeartistny.ui.lucky
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
import kotlin.random.Random

enum class Phase {
    PLAYER, WORDS, REVEAL, ORDER
}

enum class Mode {
    ALL_FAKE_ARTISTS, RANDOM_FAKE_ARTISTS, NO_FAKE_ARTISTS, NORMAL
}

private const val TAG = "GameViewModel"


class GameViewModel(
    private val playerDao: PlayerDao
) : ViewModel() {

    val allPlayers: LiveData<List<Player>> = playerDao.getPlayers().asLiveData()

    // Player currently playing, regardless of phase
    private var _currentPlayer = MutableLiveData<Player?>(null)
    val currentPlayer: LiveData<Player?> = _currentPlayer

    // Current phase of the game
    var currentPhase = Phase.PLAYER
    private var currentMode: Mode? = null

    var playerOrder: MutableList<Player> = mutableListOf()

    // List of fakes
    private var fakes: MutableList<Player> = mutableListOf()

    private var _word = MutableLiveData("")
    val word: LiveData<String> = _word

    // Word: Category map
    private var words = mutableMapOf<String, String>()

    private var _category = MutableLiveData("")

    // Settings
    var isAllFaEnabled = false
    var isNoFaEnabled = false
    var isRandomFaEnabled = false
    var isStartFaEnabled = false

    /**
     * GAME FLOW CONTROL
     */

    fun resetGame() {
        _word.value = ""
        fakes = mutableListOf()
        words = mutableMapOf()
        currentPhase = Phase.PLAYER
    }

    /**
     * Advances the game, either by phase or by player
     */
    fun next() {

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

    /**
     * Returns the next player in the order
     */
    private fun nextPlayer(): Player {
        return playerOrder[playerOrder.indexOf(_currentPlayer.value) + 1]
    }

    /**
     *  A game is valid if there's at least 3 players
     */
    fun gameIsValid(): Boolean {
        val players: List<Player> = allPlayers.value ?: listOf()
        return players.size >= 3
    }

    /**
     * Checks if the current player is a Fake Artist
     */
    fun currentPlayerIsFake(): Boolean {
        return currentPlayer.value?.let { isFake(it) } ?: false
    }

    private fun isFake(player: Player): Boolean {
        return fakes.find { it == player } != null
    }

    /**
     * Selects which Fake Artist game mode and applies it
     */
    private fun selectFAMode() {
        when {
            allFaConditions() -> setGameWithAllFa()
            randomFaConditions() -> setGameWithRandomFa()
            noFaConditions() -> setGameWithNoFa()
            else -> setNormalGame()
        }
    }

    /**
     * Sets game with everyone as fake artists
     */
    private fun setGameWithAllFa() {
        Log.d(TAG, "All FA mode")
        fakes.addAll(allPlayers.value!!)
        currentMode = Mode.ALL_FAKE_ARTISTS
    }

    /**
     * Sets game with a random amount of fake artists
     */
    private fun setGameWithRandomFa() {
        Log.d(TAG, "Random FA mode")
        for (i in 1..Random.nextInt(1, allPlayers.value!!.size / 2 - 1)) {
            val newFake = allPlayers.value!!.random()
            if (fakes.find { it == newFake } == null) {
                fakes.add(newFake)
            }
        }
        currentMode = Mode.RANDOM_FAKE_ARTISTS
    }

    /**
     * Sets game with no fake artists
     */
    private fun setGameWithNoFa() {
        Log.d(TAG, "No FA mode")
        fakes = mutableListOf()
        currentMode = Mode.NO_FAKE_ARTISTS
    }

    /**
     * Sets game with one fake artist (normal game mode)
     */
    private fun setNormalGame() {
        Log.d(TAG, "Normal mode")
        fakes.add(allPlayers.value!!.random())
        currentMode = Mode.NORMAL
    }

    /**
     * Checks if the Random FA game mode conditions are met
     */
    private fun randomFaConditions(): Boolean {
        return isRandomFaEnabled && allPlayers.value!!.size > 4 && lucky(10)
    }

    /**
     * Checks if the All FA game mode conditions are met
     */
    private fun allFaConditions(): Boolean {
        return isAllFaEnabled && lucky(10)
    }

    /**
     *  Checks if the No FA game mode conditions are met
     */
    private fun noFaConditions(): Boolean {
        return isNoFaEnabled && lucky(10)
    }


    /**
     * Adds a word to the list of player submitted words
     */
    fun addWord(word: String, category: String) {
        words[word] = category
    }

    /**
     * Select a random word from the list of words
     */
    private fun selectRandomWord() {
        _word.value = words.keys.random()
        _category.value = words[word.value]!!
    }

    /**
     * PHASES SETUP
     */

    /**
     * Sets up WORDS phase
     */
    private fun setWordsPhase() {
        // Randomly select the fake artist
        selectFAMode()

        // Randomize order
        playerOrder = allPlayers.value!!.shuffled().toMutableList()

        // Initialize currentPlayer with the first player
        _currentPlayer.value = playerOrder.first()

        // Set WORDS phase
        currentPhase = Phase.WORDS
    }

    /**
     * Sets up REVEAL phase
     */
    private fun setRevealPhase() {
        selectRandomWord()
        // Set REVEAL phase
        currentPhase = Phase.REVEAL
        // Set first player in order as currentPlayer
        _currentPlayer.value = playerOrder.first()
    }

    /**
     * Sets up ORDER phase (shuffles playerOrder once again)
     */
    private fun setOrderPhase() {
        // Set ORDER phase
        currentPhase = Phase.ORDER

        // Shuffle order again
        playerOrder = playerOrder.shuffled().toMutableList()

        // Check for startFa Modifier
        if (startFaConditions() && lucky(90)) {
            // Swap fake artist with the next artist in order
            var artistIndex = -1
            for (player in playerOrder) {
                if (!isFake(player) && artistIndex == -1) {
                    artistIndex = playerOrder.indexOf(player)
                }
            }
            val artist = playerOrder[artistIndex]
            playerOrder[artistIndex] = playerOrder[0]
            playerOrder[0] = artist
        }
    }

    private fun startFaConditions(): Boolean {
        if (isStartFaEnabled && isFake(playerOrder[0])) {
            if (currentMode == Mode.NORMAL || currentMode == Mode.RANDOM_FAKE_ARTISTS) {
                return true
            }
        }
        return false
    }

    /**
     *  DB Ops
     */

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