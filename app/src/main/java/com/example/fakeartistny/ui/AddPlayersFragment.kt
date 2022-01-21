package com.example.fakeartistny.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.BaseApplication
import com.example.fakeartistny.databinding.FragmentAddPlayerBinding
import com.example.fakeartistny.ui.adapter.PlayerListAdapter
import com.example.fakeartistny.ui.viewmodel.GameViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


private const val TAG = "AddPlayersFragment"
// TODO: On back behaviour. If it's the first player, reset the game; otherwise hide the word again
class AddPlayersFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentAddPlayerBinding? = null
    private val binding get() = _binding!!

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

        val adapter = PlayerListAdapter { player ->
            // TODO: on clicking player on list behaviour
            // Change color?

            // INITIAL
            viewModel.deletePlayer(player)
        }

        // Show keyboard immediately
        showSoftKeyboard(binding.editTextName)

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

            /**
             *  Listeners
             */
            editTextName.setOnEditorActionListener { _, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_NEXT -> {
                        addPlayer()
                        true
                    }
                    else -> false
                }
            }

            buttonAddPlayer.setOnClickListener {
                addPlayer()
            }

            buttonChangeColor.setOnClickListener {
                viewModel.cycleColors()
                updateColors()
            }

            buttonSettings.setOnClickListener {
//                findNavController().navigate(
//                    AddPlayersFragmentDirections.actionAddPlayersFragmentToSettingsFragment()
//                )
                hideSoftKeyboard(editTextName)
            }

            buttonStartGame.setOnClickListener {
                Log.d(TAG, viewModel.gameIsValid().toString())
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
                        .setPositiveButton("Okay") {_, _ ->

                        }
                        .show()
                }
            }
        }
    }

    private fun addPlayer() {
        if (binding.editTextName.text.toString() != "") {
            viewModel.addPlayer(
                binding.editTextName.text.toString(),
                ContextCompat.getColor(this.requireContext(), viewModel.currentColor.value!!)
            )
            binding.editTextName.setText("")
            updateColors()
        }
    }

    private fun updateColors() {
        binding.apply {
            // Update button's color

            buttonChangeColor.setColorFilter(
                ContextCompat.getColor(this.root.context, viewModel.currentColor.value!!)
            )

            // Update text color
            editTextName.setTextColor(
                ContextCompat.getColor(this.root.context, viewModel.currentColor.value!!)
            )
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm =
                this.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideSoftKeyboard(view: View) {
        val imm =
            this.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
