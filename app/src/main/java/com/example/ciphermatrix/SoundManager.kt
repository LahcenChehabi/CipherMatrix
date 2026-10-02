package com.example.ciphermatrix

import android.content.Context
import android.media.MediaPlayer

object SoundManager {
    private var mediaPlayer: MediaPlayer? = null

    fun playSoundWithCallback(context: Context, soundRawId: Int, onComplete: (() -> Unit)?) {
        try {
            stopCurrentSound()
            mediaPlayer = MediaPlayer.create(context, soundRawId)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
                onComplete?.invoke()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            onComplete?.invoke()
        }
    }

    fun playSound(context: Context, soundRawId: Int) {
        playSoundWithCallback(context, soundRawId, null)
    }

    fun stopCurrentSound() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) { e.printStackTrace() }
        mediaPlayer = null
    }
}