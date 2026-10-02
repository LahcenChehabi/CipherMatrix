package com.example.ciphermatrix

import android.content.Intent
import android.os.Bundle
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class SecureQrScannerActivity : AppCompatActivity() {

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "🚨 SYSTEM: SCANNER TERMINATED", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val scannedData = result.contents.trim()

            if (URLUtil.isValidUrl(scannedData) && (scannedData.startsWith("http://") || scannedData.startsWith("https://"))) {
                val intent = Intent(this, UrlScannerActivity::class.java).apply {
                    putExtra("URL_TO_SCAN", scannedData)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "☠️ PARSE ERROR: INVALID URL VECTOR", Toast.LENGTH_LONG).show()
                startScanner()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startScanner()
    }

    private fun startScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("[SYSTEM] ALIGN QR-CODE WITHIN THE MATRIX SQUARE")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)


            setOrientationLocked(true)
        }
        barcodeLauncher.launch(options)
    }
}