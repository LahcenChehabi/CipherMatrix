package com.example.ciphermatrix

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.iv_splash_logo)
        val divider = findViewById<View>(R.id.divider_splash)
        val title = findViewById<TextView>(R.id.tv_splash_title)
        val subtitle = findViewById<TextView>(R.id.tv_splash_subtitle)
        val powered = findViewById<TextView>(R.id.tv_splash_powered)

        title.setShadowLayer(18f, 0f, 0f, Color.parseColor("#00FF41"))

        val views = listOf(logo, divider, title, subtitle, powered)
        views.forEach { it.alpha = 0f }
        val delays = listOf(100L, 500L, 650L, 900L, 1250L)
        views.forEachIndexed { index, view ->
            Handler(Looper.getMainLooper()).postDelayed({
                view.animate().alpha(1f).setDuration(450).start()
            }, delays[index])
        }

        // Simple startup sound effect
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.app_open)
            mediaPlayer?.start()
        } catch (e: Exception) {
            // No sound file yet, or device issue — continue silently
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2300)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}