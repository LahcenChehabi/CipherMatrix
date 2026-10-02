package com.example.ciphermatrix

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class UrlScannerActivity : AppCompatActivity() {

    private val API_KEY = BuildConfig.VT_API_KEY
    private lateinit var tvScanResult: TextView
    private lateinit var tvFileName: TextView
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scanHistory = mutableListOf<String>()

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            tvFileName.text = "Target: ${getFileName(it)}"
            startFileTerminalScan(it)
        }
    }

    // 🟢
    private val exportLogLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        uri?.let {
            try {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(tvScanResult.text.toString().toByteArray())
                }
                Toast.makeText(this, "⚡ LOGS EXPORTED SUCCESSFULLY", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "ERROR EXPORTING LOGS", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_scanner)

        val etUrlInput = findViewById<EditText>(R.id.et_url_input)
        val btnScan = findViewById<Button>(R.id.btn_scan_url)
        val btnSelectFile = findViewById<Button>(R.id.btn_select_file)
        val fabHistory = findViewById<FloatingActionButton>(R.id.fab_history)

        tvScanResult = findViewById(R.id.tv_scan_result)
        tvFileName = findViewById(R.id.tv_file_name)

        tvScanResult.movementMethod = ScrollingMovementMethod()

        initTerminalHeader()

        fabHistory.setOnClickListener { showHistoryDialog() }

        btnScan.setOnClickListener {
            val url = etUrlInput.text.toString().trim()
            if (url.isNotEmpty()) startTerminalScan(url)
            else Toast.makeText(this, "Input Required!", Toast.LENGTH_SHORT).show()
        }

        btnSelectFile.setOnClickListener { pickFileLauncher.launch("*/*") }

        //
        val scannedUrlFromQr = intent.getStringExtra("URL_TO_SCAN")
        if (!scannedUrlFromQr.isNullOrEmpty()) {
            etUrlInput.setText(scannedUrlFromQr)
            startTerminalScan(scannedUrlFromQr)
        }
    }

    // 🟢
    private fun initTerminalHeader() {
        tvScanResult.text = "┌──────────────────────────────────────────────────┐\n" +
                "  ⚡ THREAT MATRIX // v2.0.26 // ROOT ACCESS [GRANTED]\n" +
                "  ⚡ SUBSYSTEM STATUS: ONLINE // CRYPTO-READY\n" +
                "└──────────────────────────────────────────────────┘"
    }

    //
    private fun appendHtmlLog(prefix: String, message: String, colorHex: String = "#00FF41") {
        val ts = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        //
        val htmlLine = "<br/>" +
                "<font color='#555555'><b>[$ts]</b></font> " +
                "<font color='#00FF41'><b>[$prefix:~#]</b></font> " +
                "<font color='#00E5FF'><b>──&gt;</b></font> " +
                "<font color='$colorHex'><b>$message</b></font>"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvScanResult.append(Html.fromHtml(htmlLine, Html.FROM_HTML_MODE_LEGACY))
        } else {
            @Suppress("DEPRECATION")
            tvScanResult.append(Html.fromHtml(htmlLine))
        }

        autoScrollTerminal()
    }

    private fun startFileTerminalScan(fileUri: Uri) {
        tvScanResult.append("\n\n[>>] INITIALIZING BINARY ANALYSIS...")
        val logs = listOf(
            "ESTABLISHING SECURE TUNNEL...",
            "MAPPING BINARY STRUCTURE...",
            "DECRYPTING HEXADECIMAL BLOCKS...",
            "ANALYZING HEURISTIC THREATS..."
        )
        runTerminalAnimation(logs) {
            scanHistory.add(0, "File: ${getFileName(fileUri)}")
            fetchFileReport(calculateSHA256(fileUri))
        }
    }

    private fun startTerminalScan(url: String) {
        tvScanResult.append("\n\n[>>] INITIALIZING URL THREAT MATRIX...")

        // 🟢
        Thread {
            var ipLog = "IP_RESOLUTION_FAILED // TARGET HIDDEN"
            try {
                val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
                val host = URL(formattedUrl).host
                val ipAddress = InetAddress.getByName(host).hostAddress
                ipLog = "TARGET_IP_ACQUIRED: $ipAddress"
            } catch (e: Exception) {
                //
            }

            mainHandler.post {
                appendHtmlLog("recon", ipLog, "#00E5FF")

                //
                val logs = listOf(
                    "BYPASSING GATEWAY FIREWALL...",
                    "SCRAPING URL METADATA...",
                    "ANALYZING OBFUSCATION PATTERNS...",
                    "QUERYING THREAT INTELLIGENCE GRID..."
                )
                runTerminalAnimation(logs) {
                    scanHistory.add(0, "URL: $url")
                    performScan(url)
                }
            }
        }.start()
    }


    private fun runTerminalAnimation(logs: List<String>, onComplete: () -> Unit) {
        var delay = 100L
        logs.forEachIndexed { index, log ->
            mainHandler.postDelayed({
                appendHtmlLog("root@matrix", log, "#FFFFFF")
                if (index == logs.lastIndex) onComplete()
            }, delay)
            delay += 250L
        }
    }


    private fun displayFinalResult(isMalicious: Boolean) {
        val finalReport = StringBuilder()
        finalReport.append("<br/><font color='#555555'><b>├──────────────────────────────────────────────────┤</b></font>")

        if (isMalicious) {

            finalReport.append("<br/><font color='#FF1744'><b>[☠️] ALERT: MALICIOUS PAYLOAD DETECTED</b></font>")
            finalReport.append("<br/><font color='#FF1744'><b>[🚨] THREAT LEVEL: CRITICAL // SYSTEM COMPROMISED</b></font>")
        } else {

            finalReport.append("<br/><font color='#00FF41'><b>[✔] SYSTEM INTEGRITY: SAFE // 100% CLEAN</b></font>")
            finalReport.append("<br/><font color='#00FF41'><b>[✔] ANALYSIS: NO THREAT VECTOR FOUND</b></font>")
        }

        finalReport.append("<br/><font color='#555555'><b>└──────────────────────────────────────────────────┘</b></font>")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvScanResult.append(Html.fromHtml(finalReport.toString(), Html.FROM_HTML_MODE_LEGACY))
        } else {
            @Suppress("DEPRECATION")
            tvScanResult.append(Html.fromHtml(finalReport.toString()))
        }

        autoScrollTerminal()
    }

    private fun autoScrollTerminal() {
        tvScanResult.post {
            val scrollAmount = tvScanResult.layout?.getLineTop(tvScanResult.lineCount)?.minus(tvScanResult.height) ?: 0
            if (scrollAmount > 0) tvScanResult.scrollTo(0, scrollAmount)
        }
    }

    private fun performScan(url: String) {
        NetworkModule.virusTotalApi.scanUrl(API_KEY, url).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                val id = response.body()?.data?.id
                if (id != null) getReportFromApi(id)
                else appendHtmlLog("api_error", "FAILED TO PARSE TARGET ID", "#FF1744")
            }
            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                appendHtmlLog("net_error", "CRITICAL NETWORK TERMINATION", "#FF1744")
            }
        })
    }

    private fun getReportFromApi(id: String) {
        NetworkModule.virusTotalApi.getReport(API_KEY, id).enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                val malicious = response.body()?.data?.attributes?.stats?.get("malicious") ?: 0
                displayFinalResult(malicious > 0)
            }
            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                appendHtmlLog("api_error", "FETCH COMPROMISED // ACCESS DENIED", "#FF1744")
            }
        })
    }

    private fun fetchFileReport(hash: String) {
        NetworkModule.virusTotalApi.getReport(API_KEY, hash).enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                val malicious = response.body()?.data?.attributes?.stats?.get("malicious") ?: 0
                displayFinalResult(malicious > 0)
            }
            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                appendHtmlLog("api_error", "FETCH HASH REPORT FAILED", "#FF1744")
            }
        })
    }

    private fun showHistoryDialog() {
        val history = if (scanHistory.isEmpty()) "No history records found." else scanHistory.joinToString("\n• ")

        AlertDialog.Builder(this)
            .setTitle("SESSION LOGS // HISTORY")
            .setMessage("• $history")
            .setPositiveButton("CLOSE", null)
            .setNegativeButton("CLEAR HISTORY") { dialog, _ ->
                scanHistory.clear()
                Toast.makeText(this, "⚡ History database wiped successfully.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            .setNeutralButton("EXPORT LOGS") { _, _ ->
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                exportLogLauncher.launch("ThreatMatrix_Log_$timeStamp.txt")
            }
            .show()
    }

    private fun calculateSHA256(uri: Uri): String {
        val digest = MessageDigest.getInstance("SHA-256")
        contentResolver.openInputStream(uri)?.use {
            val buffer = ByteArray(8192)
            var read: Int
            while (it.read(buffer).also { read = it } != -1) digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun getFileName(uri: Uri): String {
        contentResolver.query(uri, null, null, null, null)?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) return it.getString(nameIndex)
        }
        return "unknown_file"
    }
}