package com.example.ciphermatrix

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IntruderAlertActivity : AppCompatActivity() {

    private lateinit var viewRedFlash: View
    private lateinit var tvAlertText: TextView
    private lateinit var ivClownFace: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intruder_alert)

        viewRedFlash = findViewById(R.id.view_red_flash)
        tvAlertText = findViewById(R.id.tv_alert_text)
        ivClownFace = findViewById(R.id.iv_clown_face)


        triggerRedFlash()


        val hackerMessage = "☠ WARNING: SECURITY BREACH ☠\n⚠ INTRUDER DETECTED ⚠\n🔒 SYSTEM LOCKED 🔒"
        startTypewriterEffect(hackerMessage)


        SoundManager.playSoundWithCallback(this, R.raw.robot_voice) {

            SoundManager.playSoundWithCallback(this, R.raw.intruder_alert) {

                triggerClownJumpScare()
            }
        }
    }


    private fun startTypewriterEffect(text: String) {
        var index = 0
        val handler = Handler(Looper.getMainLooper())
        val delay: Long = 60

        val runnable = object : Runnable {
            override fun run() {
                if (index <= text.length) {
                    tvAlertText.text = text.substring(0, index)
                    index++
                    handler.postDelayed(this, delay)
                }
            }
        }
        handler.post(runnable)
    }


    private fun triggerRedFlash() {
        viewRedFlash.visibility = View.VISIBLE
        val flashAnim = ObjectAnimator.ofFloat(viewRedFlash, "alpha", 0.0f, 1.0f)
        flashAnim.duration = 350
        flashAnim.repeatCount = ValueAnimator.INFINITE
        flashAnim.repeatMode = ValueAnimator.REVERSE
        flashAnim.start()
    }

    //
    private fun triggerClownJumpScare() {

        tvAlertText.visibility = View.GONE


        SoundManager.playSound(this, R.raw.clown_laugh)
        ivClownFace.visibility = View.VISIBLE


        ivClownFace.scaleX = 0.1f
        ivClownFace.scaleY = 0.1f
        ivClownFace.alpha = 0.0f

        //
        ivClownFace.animate()
            .scaleX(2.5f)
            .scaleY(2.5f)
            .alpha(1.0f)
            .setDuration(400)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {

                Handler(Looper.getMainLooper()).postDelayed({
                    SoundManager.stopCurrentSound()
                    finishAffinity()
                }, 4000)
            }.start()
    }
}