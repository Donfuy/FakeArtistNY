package com.donfuy.fakeartistny.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.donfuy.fakeartistny.BaseApplication
import com.donfuy.fakeartistny.R
import com.donfuy.fakeartistny.databinding.FragmentAddWordBinding
import com.donfuy.fakeartistny.model.Player
import com.donfuy.fakeartistny.ui.viewmodel.GameViewModel
import com.donfuy.fakeartistny.ui.viewmodel.Phase

@Suppress("unused")
private const val TAG = "AddWordFragment"

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


        @Suppress("UNUSED_VARIABLE")
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            showResetGameAlertDialog(requireContext()) {
                viewModel.resetGame()
                findNavController().navigate(
                    AddWordFragmentDirections.actionAddWordFragmentToAddPlayersFragment()
                )
            }
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
                buttonAddWord.isEnabled = (text.toString() != "")
            }

            (textInputCategory.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            (textInputCategory.editText as? AutoCompleteTextView)?.setText(
                resources.getStringArray(R.array.word_categories)[0],
                false
            )

            buttonAddWord.setOnClickListener {
                addWord(editTextWord.text.toString(), textInputCategory.editText?.text.toString())
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

    private fun addWord(word: String, category: String) {
        if (!viewModel.currentPlayerIsFake()) {
            viewModel.addWord(word, category)
        }
    }

    private fun bindPlayer(player: Player) {
        // Get background color
        val bgColor = ContextCompat.getColor(
            requireContext(),
            getBgColor(requireContext(), viewModel.currentPlayer.value!!.color)
        )

        binding.apply {
            addWordConstraintLayout.setBackgroundColor(bgColor)

            // Set status bar and button color to a darkened version of bgColor
            darkenStatusBar(requireActivity(), bgColor)
            buttonAddWord.setBackgroundColor(ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f))
            textViewName.text = player.name
            buttonAddWord.isEnabled = false
        }
    }
}