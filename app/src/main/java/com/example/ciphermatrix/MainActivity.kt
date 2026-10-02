package com.example.ciphermatrix

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import net.objecthunter.exp4j.ExpressionBuilder
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView
    private var inputSequence = ""

    // Hidden Camera Variables
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    // Voice Recognition Logic
    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spoken = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""

            var formatted = spoken.lowercase()
                .replace("plus", "+")
                .replace("minus", "-")
                .replace("times", "×")
                .replace("multiply", "×")
                .replace("divide", "÷")
                .replace("divided by", "÷")
                .replace(" ", "")

            formatted = formatted.replace("*", "×").replace("/", "÷")

            if (tvDisplay.text.toString() == "0") {
                inputSequence = formatted
            } else {
                inputSequence += formatted
            }
            tvDisplay.text = inputSequence
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tv_display)
        tvExpression = findViewById(R.id.tv_expression)

        cameraExecutor = Executors.newSingleThreadExecutor()
        if (allPermissionsGranted()) {
            startHiddenCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }

        findViewById<ImageButton>(R.id.btn_mic).setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechLauncher.launch(intent)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_news -> {
                    startActivity(Intent(this, NewsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    fun onNumberClick(view: View) {
        val btn = view as MaterialButton
        val text = btn.text.toString()

        when (text) {
            "AC" -> {
                inputSequence = ""
                tvDisplay.text = "0"
                tvExpression.text = ""
            }
            "DEL" -> {
                if (inputSequence.isNotEmpty()) {
                    inputSequence = inputSequence.dropLast(1)
                    tvDisplay.text = inputSequence.ifEmpty { "0" }
                }
            }
            "=" -> {
                val authPrefs = getSharedPreferences("MainAuthPrefs", Context.MODE_PRIVATE)
                val savedCode = authPrefs.getString("UNLOCK_CODE", null)

                if (savedCode == null && inputSequence.isNotEmpty()) {
                    // First '=' ever pressed on this device -> this becomes the unlock code
                    authPrefs.edit().putString("UNLOCK_CODE", inputSequence).apply()
                }

                if (inputSequence.isNotEmpty() && inputSequence == savedCode) {
                    // Correct code entered -> Go to Login
                    startActivity(Intent(this, LoginActivity::class.java))

                    inputSequence = ""
                    tvDisplay.text = "0"
                    tvExpression.text = ""
                } else {
                    // Normal calculation -> Take silent photo & Calculate
                    takeSilentPhoto()

                    try {
                        tvExpression.text = "$inputSequence ="

                        val evaluableExpression = inputSequence
                            .replace("×", "*")
                            .replace("÷", "/")
                            .replace("%", "/100")

                        val res = ExpressionBuilder(evaluableExpression).build().evaluate()

                        val finalResult = if (res % 1 == 0.0) res.toInt().toString() else res.toString()
                        tvDisplay.text = finalResult

                        inputSequence = finalResult

                    } catch (e: Exception) {
                        tvExpression.text = "Error"
                    }
                }
            }
            else -> {
                if (tvDisplay.text.toString() == "0" && text != ".") {
                    inputSequence = text
                } else {
                    inputSequence += text
                }
                tvDisplay.text = inputSequence
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startHiddenCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture!!)
            } catch (exc: Exception) {
                Log.e("CipherMatrix", "Failed to start hidden camera", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takeSilentPhoto() {
        val imageCapture = imageCapture ?: return

        val folder = File(filesDir, ".CipherLogs")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val photoFile = File(folder, "Intruder_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CipherMatrix", "Photo capture failed: ${exc.message}", exc)
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CipherMatrix", "Intruder captured! Saved at: ${photoFile.absolutePath}")
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}