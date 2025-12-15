package com.eloypedrosa.battleship.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.eloypedrosa.battleship.databinding.ActivityLoginBinding
import com.eloypedrosa.battleship.ui.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        viewModel.navigateToGame.observe(this) { event ->
            event?.let { (gameId, playerId) ->
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("GAME_ID", gameId)
                    putExtra("PLAYER_ID", playerId)
                }
                startActivity(intent)
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (!error.isNullOrEmpty()) Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        }
    }
}