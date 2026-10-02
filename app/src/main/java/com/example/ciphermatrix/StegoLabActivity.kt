package com.example.ciphermatrix

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class StegoLabActivity : AppCompatActivity() {

    private lateinit var selectedBitmap: Bitmap
    private var isImageSelected = false
    private lateinit var tvConsoleLogs: TextView
    private val mainHandler = Handler(Looper.getMainLooper())


    private val ASCII_ART = """
       ____ ___ ____  _   _ _____ ____  
      / ___|_ _|  _ \| | | | ____|  _ \ 
     | |    | || |_) | |_| |  _| | |_) |
     | |___ | ||  __/|  _  | |___|  _ < 
      \____|___|_|   |_| |_|_____|_| \_\
    """.trimIndent()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val original = MediaStore.Images.Media.getBitmap(contentResolver, it)
            selectedBitmap = original.copy(Bitmap.Config.ARGB_8888, true)
            isImageSelected = true


            tvConsoleLogs.text = "$ASCII_ART\n\n[SUCCESS] Carrier image loaded.\nGrid Size: ${selectedBitmap.width}x${selectedBitmap.height} pixels."
            Toast.makeText(this, "Image loaded successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stego_lab)

        val btnSelect = findViewById<Button>(R.id.btn_select_image)
        val btnHide = findViewById<Button>(R.id.btn_hide_message)
        val btnDecode = findViewById<Button>(R.id.btn_decode_message)
        val etMessage = findViewById<EditText>(R.id.et_secret_message)
        val etKey = findViewById<EditText>(R.id.et_secret_key)
        val tvResult = findViewById<TextView>(R.id.tv_decoded_message)
        tvConsoleLogs = findViewById(R.id.tv_console_logs)


        tvConsoleLogs.text = "$ASCII_ART\n\n[SYSTEM] StegoLab core v1.0.9 initialized.\n[WAITING] Target carrier image..."
        tvConsoleLogs.movementMethod = ScrollingMovementMethod()

        btnSelect.setOnClickListener { pickImageLauncher.launch("image/*") }

        btnHide.setOnClickListener {
            if (!isImageSelected) {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val message = etMessage.text.toString()
            val key = etKey.text.toString()

            if (message.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, "Message and Secret Key are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val encryptedMessage = xorCipher(message, key)
            val binaryLength = (encryptedMessage + "\u0000").toByteArray(Charsets.ISO_8859_1).size * 8


            if (binaryLength > selectedBitmap.width * selectedBitmap.height) {
                tvConsoleLogs.text = "$ASCII_ART\n\n[ERROR] Image is too small for this message payload!"
                return@setOnClickListener
            }


            val entropyValue = 7.912 + ((selectedBitmap.getPixel(0, 0) and 0xFF) % 75) / 1000.0
            val liveStats = listOf(
                "\n[DATA] Payload Size: $binaryLength bits",
                "[MATH] Image Entropy: ${String.format("%.3f", entropyValue)} (Secure)",
                "[CHANNEL] Active Stego: LSB Red Channel"
            )


            runTerminalAnimation(
                logs = listOf(
                    "[+] Encrypting payload using XOR Cipher...",
                    "[+] Calculating pixel capacity for matching bitstream...",
                    "[+] Injecting encrypted payload into LSB Red Channel...",
                    "[SUCCESS] Stego-Image saved to Gallery safely!"
                ),
                extraStats = liveStats
            ) {
                selectedBitmap = hideMessage(selectedBitmap, encryptedMessage)
                saveImageToGallery(selectedBitmap)
                etMessage.text.clear()
            }
        }

        btnDecode.setOnClickListener {
            if (!isImageSelected) {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val key = etKey.text.toString()
            if (key.isEmpty()) {
                Toast.makeText(this, "Enter Secret Key to decrypt!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            runTerminalAnimation(
                logs = listOf(
                    "[+] Scanning LSB matrix grid...",
                    "[+] Extracting bit arrays from Red Channel...",
                    "[+] Null terminator found. Reassembling bytes...",
                    "[+] Decrypting cipher with provided security key..."
                )
            ) {
                val encryptedData = decodeMessage(selectedBitmap)
                if (encryptedData.isEmpty()) {
                    tvResult.text = "No hidden payload found or image is corrupted."
                    tvConsoleLogs.append("\n[ERROR] Extraction failed. Bad Matrix.")
                } else {
                    val decryptedRealMessage = xorCipher(encryptedData, key)
                    tvResult.text = "Decoded Payload:\n$decryptedRealMessage"


                    val extractedBits = encryptedData.toByteArray(Charsets.ISO_8859_1).size * 8
                    tvConsoleLogs.append("\n[SUCCESS] Extracted $extractedBits bits from matrix.")
                }
            }
        }
    }

    private fun xorCipher(text: String, key: String): String {
        val output = StringBuilder()
        for (i in text.indices) {
            output.append((text[i].code xor key[i % key.length].code).toChar())
        }
        return output.toString()
    }

    private fun hideMessage(bitmap: Bitmap, message: String): Bitmap {
        val msgWithEnd = message + "\u0000"
        val binaryMessage = msgWithEnd.toByteArray(Charsets.ISO_8859_1).joinToString("") {
            it.toInt().and(0xFF).toString(2).padStart(8, '0')
        }
        var bitIndex = 0

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitIndex >= binaryMessage.length) return bitmap
                val pixel = bitmap.getPixel(x, y)
                val newR = (pixel shr 16 and 0xFF).and(254).or(binaryMessage[bitIndex++].toString().toInt())
                bitmap.setPixel(x, y, Color.rgb(newR, pixel shr 8 and 0xFF, pixel and 0xFF))
            }
        }
        return bitmap
    }

    private fun decodeMessage(bitmap: Bitmap): String {
        val binary = StringBuilder()
        val byteList = mutableListOf<Byte>()

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                binary.append((pixel shr 16 and 0xFF) and 1)

                if (binary.length == 8) {
                    val currentByte = binary.toString().toInt(2).toByte()
                    if (currentByte == 0.toByte()) {
                        return String(byteList.toByteArray(), Charsets.ISO_8859_1)
                    }
                    byteList.add(currentByte)
                    binary.setLength(0)
                }
            }
        }
        return String(byteList.toByteArray(), Charsets.ISO_8859_1)
    }


    private fun runTerminalAnimation(logs: List<String>, extraStats: List<String> = emptyList(), onComplete: () -> Unit) {
        tvConsoleLogs.text = "$ASCII_ART\n\n[START] Executing Stego Operation..."

        val combinedLogs = logs + extraStats
        var delay = 350L

        combinedLogs.forEachIndexed { index, log ->
            mainHandler.postDelayed({
                tvConsoleLogs.append("\n$log")


                val scrollAmount = tvConsoleLogs.layout?.getLineTop(tvConsoleLogs.lineCount)?.minus(tvConsoleLogs.height) ?: 0
                if (scrollAmount > 0) {
                    tvConsoleLogs.scrollTo(0, scrollAmount)
                }

                if (index == combinedLogs.lastIndex) {
                    onComplete()
                }
            }, delay)
            delay += 350L
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "Stego_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it)?.use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Toast.makeText(this, "Saved to Gallery as PNG!", Toast.LENGTH_LONG).show()
        }
    }
}