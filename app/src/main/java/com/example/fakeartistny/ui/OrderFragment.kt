package com.example.fakeartistny.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.fakeartistny.BaseApplication
import com.example.fakeartistny.databinding.FragmentOrderBinding
import com.example.fakeartistny.ui.adapter.PlayerListAdapter
import com.example.fakeartistny.ui.viewmodel.GameViewModel

class OrderFragment : Fragment(){

    private val viewModel: GameViewModel by activityViewModels() {
        GameViewModel.GameViewModelFactory(
            (activity?.application as BaseApplication).database.playerDao()
        )
    }

    private var _binding: FragmentOrderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Set adapter. Use the PlayerListAdapter for now.
        val adapter = PlayerListAdapter { player ->

        }

        adapter.submitList(viewModel.playerOrder)

        binding.apply {
            recyclerViewOrder.adapter = adapter
            buttonNewGame.setOnClickListener {
                viewModel.next()
                viewModel.resetGame()
                findNavController().navigate(
                    OrderFragmentDirections.actionOrderFragmentToAddPlayersFragment()
                )
            }
        }
    }
}