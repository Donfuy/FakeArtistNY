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
import com.example.fakeartistny.databinding.FragmentAddWordBinding
import com.example.fakeartistny.model.Player
import com.example.fakeartistny.ui.viewmodel.GameViewModel
import com.example.fakeartistny.ui.viewmodel.Phase

private const val TAG = "AddWordFragment"

// TODO: On back behaviour
class AddWordFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentAddWordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentPlayer.observe(this.viewLifecycleOwner) {
            bindPlayer(it!!)
        }

        binding.apply {
            // Views logic
            buttonAddWord.setOnClickListener {
                Log.d(TAG, "Phase is: " + viewModel.currentPhase)
                viewModel.words.add(editTextWord.text.toString())
                Log.d(TAG, editTextWord.text.toString())
                viewModel.next()
                if (viewModel.currentPhase != Phase.WORDS) {
                    findNavController().navigate(
                        AddWordFragmentDirections.actionAddWordFragmentToRevealFragment()
                    )
                } else {
                    // Clear EditText
                    editTextWord.setText("")
                }
            }
        }
    }

    private fun bindPlayer(player: Player) {
        binding.apply {
            textViewName.text = player.name
        }
    }
}