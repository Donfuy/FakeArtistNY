package com.example.fakeartistny.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.*
import com.example.fakeartistny.databinding.FragmentAddPlayerBinding
import com.example.fakeartistny.ui.adapter.PlayerListAdapter
import com.example.fakeartistny.ui.viewmodel.GameViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.map

private const val TAG = "AddPlayersFragment"

private const val DEFAULT_COLOR = R.color.fg_orange

class AddPlayersFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentAddPlayerBinding? = null
    private val binding get() = _binding!!

    private var _currentColor = MutableLiveData(DEFAULT_COLOR)
    private val currentColor: LiveData<Int> = _currentColor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlayerListAdapter({ player ->
            // Change player color
            viewModel.updatePlayer(
                player.id,
                player.name,
                getNextColor(requireContext(), player.color)
            )
        }, { player ->
            // Delete player
            viewModel.deletePlayer(player)
        })

        // Show keyboard immediately
        showSoftKeyboard(requireActivity(), binding.editTextName)

        // DataStore values to GameViewModel. Dirty, but working.
        requireContext().dataStore.data.apply {
            map { prefs -> prefs[ALL_FA] }.asLiveData().observe(viewLifecycleOwner) {
                viewModel.isAllFaEnabled = it ?: false
            }
            map { prefs -> prefs[NO_FA] }.asLiveData().observe(viewLifecycleOwner) {
                viewModel.isNoFaEnabled = it ?: false
            }
            map { prefs -> prefs[RANDOM_FA] }.asLiveData().observe(viewLifecycleOwner) {
                viewModel.isRandomFaEnabled = it ?: false
            }
            map { prefs -> prefs[START_FA] }.asLiveData().observe(viewLifecycleOwner) {
                viewModel.isStartFaEnabled = it ?: false
            }
        }

        viewModel.allPlayers.observe(this.viewLifecycleOwner) { players ->
            players.let {
                adapter.submitList(it)
            }
        }

        binding.apply {
            playersRecyclerView.adapter = adapter

            // Initialize colors
            if (buttonChangeColor.colorFilter == null) {
                updateColors()
            }

            darkenStatusBar(
                requireActivity(),
                ContextCompat.getColor(requireContext(), R.color.pink_medium)
            )

            /**
             *  Listeners
             */

            // Sets the keyboard action key (Enter) to addPlayer
            editTextName.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        addPlayer()
                        true
                    }
                    else -> false
                }
            }

            buttonAddPlayer.setOnClickListener { addPlayer() }

            buttonChangeColor.setOnClickListener { updateColors() }

            buttonSettings.setOnClickListener { navigateToSettings() }

            buttonStartGame.setOnClickListener { startGame() }
        }
    }

    /**
     * Adds a new player
     */
    private fun addPlayer() {
        if (binding.editTextName.text.toString() != "") {
            viewModel.addPlayer(
                binding.editTextName.text.toString(),
                currentColor.value ?: DEFAULT_COLOR
            )
            binding.editTextName.setText("")
            updateColors()
        }
    }

    /**
     * Updates paintbrush and editTextName's text color to match the View Model
     */
    private fun updateColors() {
        _currentColor.value = getNextColor(requireContext(), currentColor.value ?: DEFAULT_COLOR)
        binding.apply {
            // Update button's color
            buttonChangeColor.setColorFilter(
                ContextCompat.getColor(requireContext(), currentColor.value!!)
            )

            // Update text color
            editTextName.setTextColor(
                ContextCompat.getColor(requireContext(), currentColor.value!!)
            )
        }
    }

    /**
     * Starts the game with the current list of players
     */
    private fun startGame() {
        if (viewModel.gameIsValid()) {
            // Start game
            viewModel.next()
            findNavController().navigate(
                AddPlayersFragmentDirections.actionAddPlayersFragmentToAddWordFragment()
            )
        } else {
            // Tell the player a game needs at least 3 players
            MaterialAlertDialogBuilder(requireContext())
                .setMessage("You need at least 3 players to start!")
                .setPositiveButton("Okay") { _, _ -> }
                .show()
        }
    }

    /**
     * Navigate to the Settings screen
     */
    private fun navigateToSettings() {
        findNavController().navigate(
            AddPlayersFragmentDirections.actionAddPlayersFragmentToSettingsFragment()
        )
        hideSoftKeyboard(requireActivity(), binding.editTextName)
    }

}
