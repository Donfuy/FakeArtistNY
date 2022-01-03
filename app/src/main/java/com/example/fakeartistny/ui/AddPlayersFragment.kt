package com.example.fakeartistny.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.BaseApplication
import com.example.fakeartistny.databinding.FragmentAddPlayerBinding
import com.example.fakeartistny.ui.adapter.PlayerListAdapter
import com.example.fakeartistny.ui.viewmodel.GameViewModel

// TODO: On back behaviour. If it's the first player, reset the game; otherwise hide the word again
class AddPlayersFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels() {
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
    ): View? {
        _binding = FragmentAddPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PlayerListAdapter {player ->
            // TODO: on clicking player on list behaviour
            // Change color?

            // INITIAL
            viewModel.deletePlayer(player)
        }

        viewModel.allPlayers.observe(this.viewLifecycleOwner) { players ->
            players.let {
                adapter.submitList(it)
            }
        }

        binding.apply {
            playersRecyclerView.adapter = adapter

            buttonAddPlayer.setOnClickListener {
                viewModel.addPlayer(binding.editTextName.text.toString(), 0)
                binding.editTextName.setText("")
            }

            buttonStartGame.setOnClickListener {
                // Start game
                viewModel.next()
                findNavController().navigate(
                    AddPlayersFragmentDirections.actionAddPlayersFragmentToAddWordFragment()
                )
            }
        }
    }
}