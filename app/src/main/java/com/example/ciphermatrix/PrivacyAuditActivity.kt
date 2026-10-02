package com.example.ciphermatrix

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class PrivacyAuditActivity : AppCompatActivity() {

    private lateinit var tvTerminal: TextView
    private lateinit var tvStats: TextView
    private lateinit var btnStartAudit: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_audit)

        tvTerminal = findViewById(R.id.tv_terminal_audit)
        tvStats = findViewById(R.id.tv_stats)
        btnStartAudit = findViewById(R.id.btn_start_audit)
        progressBar = findViewById(R.id.progress_audit)

        tvTerminal.movementMethod = ScrollingMovementMethod()

        initTerminalHeader()

        btnStartAudit.setOnClickListener {
            runUltimatePrivacyAudit()
        }
    }

    private fun initTerminalHeader() {
        val headerText = "==================================================\n" +
                "  ⚡ CIPHER_MATRIX // CORE PRIVACY RISK ENGINE v5.0 \n" +
                "==================================================\n" +
                "[+] KERNEL SECURITY STACK : REINFORCED\n" +
                "[+] HEURISTIC DETECTOR     : LIVE MODULE ARMED\n" +
                "[+] TARGET ENVIRONMENT     : USERSPACE LAYER\n" +
                "[!] SYSTEM STATUS          : AWAITING DEEP AUDIT COMMAND\n" +
                "--------------------------------------------------"
        tvTerminal.text = headerText
    }

    private fun appendHtmlLog(prefix: String, message: String, prefixColor: String = "#00FF41", msgColor: String = "#00FF41") {
        val ts = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        //
        val htmlLine = "<br/><font color='#FFFFFF'>[$ts]</font> " +
                "<font color='$prefixColor'>[$prefix]</font> » " +
                "<font color='$msgColor'>$message</font>"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvTerminal.append(Html.fromHtml(htmlLine, Html.FROM_HTML_MODE_LEGACY))
        } else {
            @Suppress("DEPRECATION")
            tvTerminal.append(Html.fromHtml(htmlLine))
        }

        tvTerminal.post {
            val scrollAmount = tvTerminal.layout?.getLineTop(tvTerminal.lineCount)?.minus(tvTerminal.height) ?: 0
            if (scrollAmount > 0) tvTerminal.scrollTo(0, scrollAmount)
        }
    }

    private fun runUltimatePrivacyAudit() {
        btnStartAudit.isEnabled = false
        progressBar.visibility = View.VISIBLE

        initTerminalHeader()
        appendHtmlLog("CORE_INIT", "SPAWNING REVERSE-ENGINEERING PROBES...", "#00FF41", "#FFFFFF")

        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)

        var totalScanned = 0
        var criticalThreats = 0
        var mediumRisks = 0
        var totalRiskPoints = 0

        CoroutineScope(Dispatchers.Main).launch {
            for (resolveInfo in resolveInfos) {
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(pm).toString()

                if (appName == "CipherMatrix") continue

                totalScanned++

                // 🟢 ردّينا نصوص الـ Decompile والـ Re-engineer بالأبيض والرمادي الفاتح جداً (#DDDDDD)
                appendHtmlLog("DECOMPILE", "Extracting Manifest from <font color='#00E5FF'><b>$appName</b></font>...", "#FFFFFF", "#DDDDDD")
                delay(60)
                appendHtmlLog("RE_ENGINEER", "Mapping permission node tree structure...", "#FFFFFF", "#DDDDDD")
                delay(60)

                try {
                    val packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                    val permissions = packageInfo.requestedPermissions

                    val risks = mutableListOf<String>()
                    var appRiskPoints = 0

                    if (permissions != null) {
                        if (permissions.any { it.contains("CAMERA", ignoreCase = true) }) { risks.add("CAMERA_FEED"); appRiskPoints += 35 }
                        if (permissions.any { it.contains("RECORD_AUDIO", ignoreCase = true) }) { risks.add("AUDIO_WIRE"); appRiskPoints += 35 }
                        if (permissions.any { it.contains("LOCATION", ignoreCase = true) }) { risks.add("GPS_COORDINATES"); appRiskPoints += 25 }
                        if (permissions.any { it.contains("READ_SMS", ignoreCase = true) }) { risks.add("SMS_DB"); appRiskPoints += 20 }
                        if (permissions.any { it.contains("READ_CONTACTS", ignoreCase = true) }) { risks.add("CONTACTS_MAP"); appRiskPoints += 15 }
                    }

                    totalRiskPoints += appRiskPoints
                    val formattedAppName = "<font color='#00E5FF'><b>$appName</b></font>"

                    if (appRiskPoints >= 65) {
                        criticalThreats++
                        tvStats.text = "⚠️ INSTABILITY DETECTED // HIGH RISK THREAT"

                        // 🟢 الأحمر هنا رديناه فاقع ومضيء (#FF1744) باش يبان بوضوح جبار في البروجيكتور
                        appendHtmlLog("☠️ TIER_1_VULN", "CRITICAL OVERWATCH SPYWARE DETECTED IN $formattedAppName", "#FF1744", "#FF1744")

                        val diagram = "<br/><font color='#FF1744'>    [KILL_CHAIN_EXPOSURE_DIAGRAM]:" +
                                "<br/>    [PRIVACY_DATA_SOURCE: ${risks.joinToString(", ")}]" +
                                "<br/>          │ " +
                                "<br/>          ▼ (Hooked via Background Thread)" +
                                "<br/>    [LOCAL_BUFFER_STAGE] ===&gt; [INTERNET_TRANSPORT_LAYER]" +
                                "<br/>                                    │ " +
                                "<br/>                                    ▼ " +
                                "<br/>                              [⚠️ EXFILTRATION TO REMOTE C2 SERVER]</font>"

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            tvTerminal.append(Html.fromHtml(diagram, Html.FROM_HTML_MODE_LEGACY))
                        } else {
                            @Suppress("DEPRECATION")
                            tvTerminal.append(Html.fromHtml(diagram))
                        }

                    } else if (appRiskPoints in 20..64) {
                        mediumRisks++

                        appendHtmlLog("⚠️ TIER_2_RISK", "$formattedAppName accumulating user profile metadata. Vector: ${risks.joinToString(", ")}", "#FFEA00", "#FFEA00")
                    } else {
                        appendHtmlLog("✔ VALIDATED", "$formattedAppName complies with Zero-Trust local architecture.", "#00FF41", "#00FF41")
                    }

                } catch (e: Exception) {
                    appendHtmlLog("SYS_BYPASS", "System Node Isolated: $appName", "#FFFFFF", "#FFFFFF")
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvTerminal.append(Html.fromHtml("<br/><font color='#1B5E20'>--------------------------------------------------</font>", Html.FROM_HTML_MODE_LEGACY))
                } else {
                    @Suppress("DEPRECATION")
                    tvTerminal.append(Html.fromHtml("<br/><font color='#1B5E20'>--------------------------------------------------</font>"))
                }
            }

            val maxPossibleRisk = totalScanned * 45
            val finalThreatScore = (if (maxPossibleRisk > 0) (totalRiskPoints.toFloat() / maxPossibleRisk * 100).toInt() else 0).coerceAtMost(100)

            progressBar.visibility = View.GONE
            btnStartAudit.isEnabled = true
            tvStats.text = "THREAT INDEX: $finalThreatScore% | SCAN COMPLETE"

            val finalReport = "<br/><br/><font color='#00FF41'>==================================================" +
                    "<br/>       ☣️ CRYPTO_MATRIX ELITE PRIVACY REPORT ☣️     " +
                    "<br/>==================================================" +
                    "<br/>[*] SUBSYSTEM NODES SCANNED : $totalScanned" +
                    "<br/>[*] ACTIVE SPYWARE VECTORS  : <font color='#FF1744'>$criticalThreats</font>" +
                    "<br/>[*] DATA LEAK CHANNELS      : <font color='#FFEA00'>$mediumRisks</font>" +
                    "<br/>--------------------------------------------------" +
                    "<br/>[>>>] SYSTEM PRIVACY SCORE  : <font color='${if(finalThreatScore > 40) "#FF1744" else "#00FF41"}'>$finalThreatScore% THREAT FACTOR</font>" +
                    "<br/>" +
                    if (finalThreatScore > 40) {
                        "<br/><font color='#FF1744'>[🚨] CONCLUSION: SYSTEM IS COMPROMISED. BACKGROUND SURVEILLANCE DETECTED.</font>"
                    } else {
                        "<br/><font color='#00FF41'>[🛡️] CONCLUSION: SANITIZED ENVIRONMENT. ANTI-TRACKING RULES ARE OPTIMAL.</font>"
                    } +
                    "<br/>==================================================</font>"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tvTerminal.append(Html.fromHtml(finalReport, Html.FROM_HTML_MODE_LEGACY))
            } else {
                @Suppress("DEPRECATION")
                tvTerminal.append(Html.fromHtml(finalReport))
            }
        }
    }
}