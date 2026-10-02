package com.example.ciphermatrix

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import kotlin.random.Random

class SecurityDashboardActivity : AppCompatActivity() {

    //
    private lateinit var tvTerminal: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val terminalLines = mutableListOf<String>()

    //
    private val fakeCommands = listOf(
        "> Ping 192.168.1.1... OK",
        "> Bypassing Firewall [####......]",
        "> Decrypting Payload... DONE",
        "> Injecting SQL... SUCCESS",
        "> WARNING: Intrusion Detected!",
        "> Rerouting IP through Proxy...",
        "> Extracting SHA-256 Hashes...",
        "> Root access granted."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔒 Anti-Screenshot Defense
        window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
        )

        setContentView(R.layout.activity_security_dashboard)

        // 🚀 Navigation Links
        findViewById<MaterialCardView>(R.id.card_passwords_vault).setOnClickListener {
            startActivity(Intent(this, VaultAuthActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_stego_lab).setOnClickListener {
            startActivity(Intent(this, StegoLabActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_url_scanner).setOnClickListener {
            startActivity(Intent(this, UrlScannerActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_network_inspector).setOnClickListener {
            startActivity(Intent(this, NetworkInspectorActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_privacy_audit).setOnClickListener {
            startActivity(Intent(this, PrivacyAuditActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_secure_qr).setOnClickListener {
            startActivity(Intent(this, SecureQrScannerActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.card_intruder_logs).setOnClickListener {
            startActivity(Intent(this, IntruderLogsActivity::class.java))
        }

        // ======================================3D
        // ==========================================
        findViewById<MaterialCardView>(R.id.card_global_threat_map).setOnClickListener {
            startActivity(Intent(this, GlobalThreatMapActivity::class.java))
        }
        // ==========================================

        //
        tvTerminal = findViewById(R.id.tv_terminal_output)
        startTerminalLogs()
    }

    private fun startTerminalLogs() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                //
                if (terminalLines.size >= 5) {
                    terminalLines.removeAt(0)
                }

                //
                terminalLines.add(fakeCommands[Random.nextInt(fakeCommands.size)])
                tvTerminal.text = terminalLines.joinToString("\n")

                //
                handler.postDelayed(this, Random.nextLong(800, 1500))
            }
        }, 1000)
    }
}