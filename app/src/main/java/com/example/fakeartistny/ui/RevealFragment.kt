package com.example.fakeartistny.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.BaseApplication
import com.example.fakeartistny.databinding.FragmentRevealBinding
import com.example.fakeartistny.model.Player
import com.example.fakeartistny.ui.viewmodel.GameViewModel
import com.example.fakeartistny.ui.viewmodel.Phase

private const val TAG = "RevealFragment"

class RevealFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels() {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentRevealBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRevealBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentPlayer.observe(this.viewLifecycleOwner) {
            bindPlayer(it!!)
        }

        binding.apply {
            buttonReveal.isEnabled = false
            textViewReveal.setOnClickListener {
                // Reveal role
                Log.d(TAG, viewModel.isFake().toString())
                if (viewModel.isFake()) {
                    textViewReveal.text = "Fake Artist!"
                } else {
                    Log.d(TAG, viewModel.word.value!!)
                    textViewReveal.text = viewModel.word.value
                }
                buttonReveal.isEnabled = true
            }

            buttonReveal.setOnClickListener {
                Log.d(TAG, "Phase is: " + viewModel.currentPhase)
                viewModel.next()
                if (viewModel.currentPhase != Phase.REVEAL) {
                    findNavController().navigate(
                        RevealFragmentDirections.actionRevealFragmentToOrderFragment()
                    )
                } else {
                    textViewReveal.text = "Click me!"
                    buttonReveal.isEnabled = false
                }
            }
        }
    }

    private fun bindPlayer(player: Player) {
        binding.apply {
            textViewPlayer.text = player.name
        }
    }
}