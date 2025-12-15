package com.eloypedrosa.battleship.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eloypedrosa.battleship.databinding.ActivityWinnerBinding

class WinnerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWinnerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWinnerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)
        binding.tvFinalScore.text = "Puntuación Final: $finalScore"

        binding.btnPlayAgain.setOnClickListener {
            // Regresar a la pantalla de Login para buscar una nueva partida
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}