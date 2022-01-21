package com.example.fakeartistny.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fakeartistny.databinding.ListItemPlayerBinding
import com.example.fakeartistny.model.Player

class PlayerListAdapter(
    private val clickListener: (Player) -> Unit
) : ListAdapter<Player, PlayerListAdapter.PlayerViewHolder>(DiffCallback) {

    class PlayerViewHolder(
        private var binding: ListItemPlayerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: Player) {
            binding.player = player
            binding.name.setTextColor(player.color)
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }

    // Get a reference to the RecyclerView
    private lateinit var mParent: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        mParent = parent

        return PlayerViewHolder(
            ListItemPlayerBinding.inflate(layoutInflater, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val player = getItem(position)

        // Scroll to the bottom of the recyclerview whenever a new item is added
        val recyclerView: RecyclerView = mParent as RecyclerView
        recyclerView.scrollToPosition(position)

        holder.itemView.setOnClickListener {
            clickListener(player)
        }
        holder.bind(player)
    }
}