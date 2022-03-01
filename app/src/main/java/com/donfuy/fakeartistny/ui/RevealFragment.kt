package com.donfuy.fakeartistny.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.donfuy.fakeartistny.BaseApplication
import com.donfuy.fakeartistny.databinding.FragmentRevealBinding
import com.donfuy.fakeartistny.R
import com.donfuy.fakeartistny.model.Player
import com.donfuy.fakeartistny.ui.viewmodel.GameViewModel
import com.donfuy.fakeartistny.ui.viewmodel.Phase

private const val TAG = "RevealFragment"

class RevealFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentRevealBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("UNUSED_VARIABLE")
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            showResetGameAlertDialog(requireContext()) {
                viewModel.resetGame()
                findNavController().navigate(
                    RevealFragmentDirections.actionRevealFragmentToAddPlayersFragment()
                )
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                Log.d(TAG, viewModel.currentPlayerIsFake().toString())
                if (viewModel.currentPlayerIsFake()) {
                    textViewReveal.visibility = View.INVISIBLE
                    textViewRevealFake.visibility = View.VISIBLE
                } else {
                    textViewReveal.text = viewModel.word.value
                }
                buttonReveal.isEnabled = true
            }

            buttonReveal.setOnClickListener {
                viewModel.next()
                if (viewModel.currentPhase != Phase.REVEAL) {
                    findNavController().navigate(
                        RevealFragmentDirections.actionRevealFragmentToOrderFragment()
                    )
                } else {
                    textViewReveal.visibility = View.VISIBLE
                    textViewReveal.text = getText(R.string.tap_to_reveal_prompt)
                    textViewRevealFake.visibility = View.GONE
                    buttonReveal.isEnabled = false
                }
            }
        }
    }

    private fun bindPlayer(player: Player) {

        // Get background color
        val bgColor = ContextCompat.getColor(
            requireContext(),
            getBgColor(requireContext(), viewModel.currentPlayer.value!!.color)
        )

        binding.apply {
            revealConstraintLayout.setBackgroundColor(bgColor)

            // Set status bar and button color to a darkened version of bgColor
            darkenStatusBar(requireActivity(), bgColor)
            buttonReveal.setBackgroundColor(ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f))
            textViewName.text = player.name
        }
    }
}
