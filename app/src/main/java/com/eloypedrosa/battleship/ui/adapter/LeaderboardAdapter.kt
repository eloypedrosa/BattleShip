package com.eloypedrosa.battleship.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eloypedrosa.battleship.data.model.User
import com.eloypedrosa.battleship.databinding.ItemLeaderboardUserBinding

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.UserViewHolder>() {
    private var users: List<User> = emptyList()

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemLeaderboardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.tvPosition.text = "#${position + 1}"
        holder.binding.tvUsername.text = user.username
        holder.binding.tvScore.text = "${user.totalScore} pts"
        holder.binding.tvWins.text = "🏆 ${user.gamesWon}"
    }

    override fun getItemCount() = users.size
    class UserViewHolder(val binding: ItemLeaderboardUserBinding) : RecyclerView.ViewHolder(binding.root)
}