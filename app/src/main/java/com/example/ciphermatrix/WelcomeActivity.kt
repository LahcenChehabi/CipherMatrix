package com.example.ciphermatrix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale
import kotlin.random.Random

class WelcomeActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tvMatrixBackground: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val matrixChars = "0101010101ABCDEFUXYZ#@$&*+-%/=<>"
    private lateinit var tts: TextToSpeech
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        tvMatrixBackground = findViewById(R.id.tv_matrix_background)

        val tvWelcomeMsg = findViewById<TextView>(R.id.tv_welcome_msg)
        val userName = FirebaseAuth.getInstance().currentUser?.displayName
        tvWelcomeMsg.text = if (!userName.isNullOrBlank()) {
            "ACCESS GRANTED\nWelcome $userName\nto your secret world"
        } else {
            "ACCESS GRANTED\nWelcome\nto your secret world"
        }

        matrixAnimationRunnable.run()

        tts = TextToSpeech(this, this)

        // Safety net: if TTS never fires (no engine, error...), still move on after 6s
        handler.postDelayed({ goToDashboard() }, 6000)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    handler.post { goToDashboard() }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    handler.post { goToDashboard() }
                }
            })

            val name = FirebaseAuth.getInstance().currentUser?.displayName
            val greeting = if (!name.isNullOrBlank()) {
                "Access granted. Welcome $name, to your secret world."
            } else {
                "Access granted. Welcome to your secret world."
            }

            handler.postDelayed({
                tts.speak(greeting, TextToSpeech.QUEUE_FLUSH, null, "welcome_greeting")
            }, 500)
        }
    }

    private fun goToDashboard() {
        if (hasNavigated) return
        hasNavigated = true
        handler.removeCallbacks(matrixAnimationRunnable)
        val intent = Intent(this, SecurityDashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val matrixAnimationRunnable = object : Runnable {
        override fun run() {
            val sb = java.lang.StringBuilder()
            for (i in 0..70) {
                for (j in 0..45) {
                    sb.append(matrixChars[Random.nextInt(matrixChars.length)])
                }
                sb.append("\n")
            }
            tvMatrixBackground.text = sb.toString()
            handler.postDelayed(this, 50)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(matrixAnimationRunnable)
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}