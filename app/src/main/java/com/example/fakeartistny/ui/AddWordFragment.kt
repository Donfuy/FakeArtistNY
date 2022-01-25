package com.example.fakeartistny.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.BaseApplication
import com.example.fakeartistny.R
import com.example.fakeartistny.databinding.FragmentAddWordBinding
import com.example.fakeartistny.model.Player
import com.example.fakeartistny.ui.viewmodel.GameViewModel
import com.example.fakeartistny.ui.viewmodel.Phase
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "AddWordFragment"

// TODO: On back behaviour - If it's the first player, reset game - if it's not, set current player to the last one
// TODO: If it's the fake artists' turn, don't add the word
// TODO: Let the app choose a word for you from a dictionary (shuffle button)
class AddWordFragment : Fragment() {

    private val viewModel: GameViewModel by activityViewModels {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentAddWordBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            showResetGameAlertDialog(requireContext()) {
                viewModel.resetGame()
                findNavController().navigate(
                    AddWordFragmentDirections.actionAddWordFragmentToAddPlayersFragment()
                )
            }

//            if (viewModel.isFirstPlayer()) {
//                // Ask to reset game
//                showResetGameAlertDialog(requireContext()) {
//                    viewModel.back()
//                    findNavController().navigate(
//                        AddWordFragmentDirections.actionAddWordFragmentToAddPlayersFragment()
//                    )
//                }
//            } else {
//                // Ask to go back to the last player
//                showGoBackAlertDialog(requireContext()) {
//                    viewModel.back()
//                    binding.editTextWord.setText("")
//                }
//            }
        }
    }

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

        val wordCategories = resources.getStringArray(R.array.word_categories)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_category, wordCategories)

        viewModel.currentPlayer.observe(this.viewLifecycleOwner) {
            bindPlayer(it!!)
        }

        binding.apply {
            // Views logic
            editTextWord.requestFocus()

            // If text field is empty, disable the next button

            editTextWord.doAfterTextChanged { text ->
                Log.d(TAG, text.toString())
                buttonAddWord.isEnabled = (text.toString() != "")
            }


            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            (textInputCategory.editText as? AutoCompleteTextView)?.setText(
                resources.getStringArray(R.array.word_categories)[0],
                false
            )

            buttonAddWord.setOnClickListener {
                addWord(editTextWord.text.toString())
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

    private fun addWord(word: String) {
        if (!viewModel.currentPlayerIsFake()) {
            viewModel.words.add(word)
        }
    }

    private fun bindPlayer(player: Player) {

        // Get background color
        val bgColor = ContextCompat.getColor(
            requireContext(),
            bgColor(viewModel.currentPlayer.value!!.color)
        )

        binding.apply {
            addWordConstraintLayout.setBackgroundColor(bgColor)

            // Set status bar and button color to a darkened version of bgColor
            requireActivity().window.statusBarColor =
                ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f)
            buttonAddWord.setBackgroundColor(ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f))
            textViewName.text = player.name
            buttonAddWord.isEnabled = false
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