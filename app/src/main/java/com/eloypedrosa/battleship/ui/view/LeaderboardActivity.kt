package com.eloypedrosa.battleship.ui.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.eloypedrosa.battleship.databinding.ActivityLeaderboardBinding
import com.eloypedrosa.battleship.ui.adapter.LeaderboardAdapter
import com.eloypedrosa.battleship.ui.viewmodel.LeaderboardViewModel
import kotlinx.coroutines.launch

class LeaderboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaderboardBinding
    private val viewModel: LeaderboardViewModel by viewModels()
    private val adapter = LeaderboardAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)
        binding.rvLeaderboard.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            viewModel.leaderboard.collect { users ->
                adapter.submitList(users)
            }
        }
    }
}