package com.example.ciphermatrix

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.AudioAttributes
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class VaultAuthActivity : AppCompatActivity() {

    private lateinit var cardPinAuth: MaterialCardView
    private lateinit var etPin: EditText
    private lateinit var btnSubmitPin: Button
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView

    private lateinit var cardFingerprintAuth: MaterialCardView
    private lateinit var btnScanBiometric: Button
    private lateinit var btnMenu: ImageButton

    private var isSetupMode = false
    private var failedAttempts = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vault_auth)


        cardPinAuth = findViewById(R.id.card_pin_auth)
        etPin = findViewById(R.id.et_pin_input)
        btnSubmitPin = findViewById(R.id.btn_submit_pin)
        tvTitle = findViewById(R.id.tv_auth_title)
        tvSubtitle = findViewById(R.id.tv_auth_subtitle)

        cardFingerprintAuth = findViewById(R.id.card_fingerprint_auth)
        btnScanBiometric = findViewById(R.id.btn_scan_biometric)
        btnMenu = findViewById(R.id.btn_menu)


        btnMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menu.add("Code PIN")
            popup.menu.add("Finger print")

            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Code PIN" -> {
                        if (cardPinAuth.visibility != View.VISIBLE) {
                            cardFingerprintAuth.animate().alpha(0f).setDuration(200).withEndAction {
                                cardFingerprintAuth.visibility = View.GONE
                            }.start()

                            cardPinAuth.alpha = 0f
                            cardPinAuth.visibility = View.VISIBLE
                            cardPinAuth.animate().alpha(1f).setDuration(200).start()
                        }
                        true
                    }
                    "Finger print" -> {
                        if (cardFingerprintAuth.visibility != View.VISIBLE) {
                            cardPinAuth.animate().alpha(0f).setDuration(200).withEndAction {
                                cardPinAuth.visibility = View.GONE
                            }.start()

                            cardFingerprintAuth.alpha = 0f
                            cardFingerprintAuth.visibility = View.VISIBLE
                            cardFingerprintAuth.animate().alpha(1f).setDuration(200).withEndAction {
                                startFingerprintAuth()
                            }.start()
                        }
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        checkPinStatus()

        btnSubmitPin.setOnClickListener {
            val enteredPin = etPin.text.toString().trim()


            val isMathOperation = enteredPin.contains("+") || enteredPin.contains("-") ||
                    enteredPin.contains("*") || enteredPin.contains("/")

            if (isMathOperation) {
                etPin.text.clear()
                triggerErrorVibration()
                takeIntruderSelfie()
                SoundManager.playSound(this, R.raw.access_denied)
                return@setOnClickListener
            }


            if (enteredPin.length < 4) {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                triggerErrorVibration()
                SoundManager.playSound(this, R.raw.access_denied)
                return@setOnClickListener
            }

            if (isSetupMode) savePin(enteredPin) else verifyPin(enteredPin)
        }

        btnScanBiometric.setOnClickListener {
            startFingerprintAuth()
        }

        requestCameraPermissionIfNeeded()
    }

    private fun checkPinStatus() {
        val sharedPref = getSharedPreferences("VaultAuthPrefs", Context.MODE_PRIVATE)
        val savedPin = sharedPref.getString("MASTER_PIN", null)
        isSetupMode = (savedPin == null)
        if (isSetupMode) {
            tvTitle.text = "SYSTEM SETUP"
            btnSubmitPin.text = "SAVE PIN"
        } else {
            tvTitle.text = "VAULT LOCKED"
            btnSubmitPin.text = "ACCESS"
        }
    }

    private fun savePin(pin: String) {
        getSharedPreferences("VaultAuthPrefs", Context.MODE_PRIVATE).edit().putString("MASTER_PIN", pin).apply()
        Toast.makeText(this, "✅ PIN Saved!", Toast.LENGTH_SHORT).show()
        grantAccess()
    }

    private fun verifyPin(pin: String) {
        val savedPin = getSharedPreferences("VaultAuthPrefs", Context.MODE_PRIVATE).getString("MASTER_PIN", null)
        if (pin == savedPin) {
            failedAttempts = 0
            grantAccess()
        } else {
            failedAttempts++
            etPin.text.clear()
            triggerErrorVibration()


            Toast.makeText(this, "❌ Wrong PIN", Toast.LENGTH_SHORT).show()


            if (failedAttempts >= 3) {
                takeIntruderSelfie()


                val intent = Intent(this, IntruderAlertActivity::class.java)
                startActivity(intent)
                finish()
            } else {

                SoundManager.playSound(this, R.raw.access_denied)
            }
        }
    }

    private fun triggerErrorVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val timings = longArrayOf(0, 150, 80, 150)
            val amplitudes = intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE)

            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1), audioAttributes)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(300)
        }
    }

    @SuppressLint("MissingPermission")
    private fun takeIntruderSelfie() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var frontCameraId: String? = null
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.cameraIdList.let { cameraManager.getCameraCharacteristics(id) }
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = id
                    break
                }
            }

            if (frontCameraId == null) return

            cameraManager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    proceedWithCapture(camera)
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close() }
                override fun onError(camera: CameraDevice, error: Int) { camera.close() }
            }, null)

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun proceedWithCapture(cameraDevice: CameraDevice) {
        try {
            val imageReader = ImageReader.newInstance(480, 640, ImageFormat.JPEG, 1)
            val outputSurfaces = Collections.singletonList(imageReader.surface)

            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                saveSelfieToFile(bytes)
                image.close()
                cameraDevice.close()
            }, null)

            cameraDevice.createCaptureSession(outputSurfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    try {
                        val captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                        captureBuilder.addTarget(imageReader.surface)
                        captureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 270)

                        session.capture(captureBuilder.build(), null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }
                override fun onConfigureFailed(session: CameraCaptureSession) { cameraDevice.close() }
            }, null)

        } catch (e: CameraAccessException) {
            cameraDevice.close()
        }
    }

    private fun saveSelfieToFile(bytes: ByteArray) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "INTRUDER_$timeStamp.jpg"

            val directory = File(filesDir, ".CipherLogs")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            val outputStream = FileOutputStream(file)
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestCameraPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    private fun startFingerprintAuth() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    failedAttempts = 0
                    grantAccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                    SoundManager.playSound(this@VaultAuthActivity, R.raw.access_denied)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {}
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("CipherMatrix Authentication")
            .setSubtitle("Verify identity to access vault")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun grantAccess() {

        SoundManager.playSound(this, R.raw.access_granted)

        val target = if (FirebaseAuth.getInstance().currentUser == null) {
            LoginActivity::class.java
        } else {
            PasswordsVaultActivity::class.java
        }

        val intent = Intent(this, target)
        startActivity(intent)
        finish()
    }
}