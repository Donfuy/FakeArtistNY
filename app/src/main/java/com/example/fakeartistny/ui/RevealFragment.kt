package com.example.fakeartistny.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.BaseApplication
import com.example.fakeartistny.R
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

        // Get background color
        val bgColor = ContextCompat.getColor(
            requireContext(),
            bgColor(viewModel.currentPlayer.value!!.color)
        )

        binding.apply {
            revealConstraintLayout.setBackgroundColor(bgColor)

            // Set status bar and button color to a darkened version of bgColor
            // TODO: Turn this ColorUtils call into an extension function
            requireActivity().window.statusBarColor =
                ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f)
            buttonReveal.setBackgroundColor(ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f))
            textViewName.text = player.name
        }
    }

    /**
     * Returns the background color for a given foreground (player) color
     */
    private fun bgColor(color: Int): Int {
        return when (color) {
            ContextCompat.getColor(requireContext(), R.color.fg_dark_green) -> R.color.bg_dark_green
            ContextCompat.getColor(requireContext(), R.color.fg_green) -> R.color.bg_green
            ContextCompat.getColor(requireContext(), R.color.fg_red) -> R.color.bg_red
            ContextCompat.getColor(requireContext(), R.color.fg_pink) -> R.color.bg_pink
            ContextCompat.getColor(requireContext(), R.color.fg_lilac) -> R.color.bg_lilac
            ContextCompat.getColor(requireContext(), R.color.fg_blue) -> R.color.bg_blue
            ContextCompat.getColor(requireContext(), R.color.fg_light_blue) -> R.color.bg_light_blue
            ContextCompat.getColor(requireContext(), R.color.fg_orange) -> R.color.bg_orange
            ContextCompat.getColor(requireContext(), R.color.fg_purple) -> R.color.bg_purple
            ContextCompat.getColor(requireContext(), R.color.fg_brown) -> R.color.bg_brown
            else -> R.color.bg_dark_green
        }
    }

}
