package com.example.ciphermatrix

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NetworkInspectorActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvIp: TextView
    private lateinit var btnScan: Button
    private lateinit var progressBar: ProgressBar


    private lateinit var btnTopology: Button
    private lateinit var topologyView: TopologyView
    private lateinit var scrollTerminal: View
    private var discoveredDevicesList = mutableListOf<NetworkDevice>()


    private lateinit var nsdManager: NsdManager
    private val mDnsDevices = ConcurrentHashMap<String, String>()


    private var currentNetworkMode = NetworkMode.WIFI_SCANNER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_inspector)

        tvStatus = findViewById(R.id.tv_connection_status)
        tvIp = findViewById(R.id.tv_ip_address)
        btnScan = findViewById(R.id.btn_run_scan)
        progressBar = findViewById(R.id.progress_scan)


        btnTopology = findViewById(R.id.btn_view_topology)
        topologyView = findViewById(R.id.topology_view)
        scrollTerminal = findViewById(R.id.scroll_terminal)
        // ----------------------------------------------

        tvIp.movementMethod = ScrollingMovementMethod()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        performFullSecurityAudit()


        startNsdDiscovery()

        btnScan.setOnClickListener { performNetworkScan() }


        btnTopology.setOnClickListener {
            if (topologyView.visibility == View.VISIBLE) {
                topologyView.visibility = View.GONE
                scrollTerminal.visibility = View.VISIBLE
                btnTopology.text = "VIEW_NETWORK_TOPOLOGY"
            } else {
                if (discoveredDevicesList.isNotEmpty()) {
                    scrollTerminal.visibility = View.GONE
                    topologyView.visibility = View.VISIBLE
                    topologyView.devices = discoveredDevicesList
                    btnTopology.text = "VIEW_TERMINAL_LOGS"
                } else {
                    logToTerminal("⚠️ CANNOT GENERATE TOPOLOGY: RUN NETWORK PROBE FIRST TO DISCOVER NODES.")
                }
            }
        }
        // --------------------------------------------------
    }


    private fun startNsdDiscovery() {
        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        val serviceTypes = listOf("_googlecast._tcp", "_http._tcp", "_ipp._tcp", "_smb._tcp", "_spotify-connect._tcp")

        serviceTypes.forEach { type ->
            try {
                nsdManager.discoverServices(type, NsdManager.PROTOCOL_DNS_SD, object : NsdManager.DiscoveryListener {
                    override fun onDiscoveryStarted(regType: String) {}
                    override fun onServiceFound(service: NsdServiceInfo) {
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                val ip = serviceInfo.host?.hostAddress
                                val name = serviceInfo.serviceName.uppercase(Locale.getDefault())
                                if (ip != null) {
                                    mDnsDevices[ip] = "SMART_NODE: $name"
                                }
                            }
                        })
                    }
                    override fun onServiceLost(service: NsdServiceInfo) {}
                    override fun onDiscoveryStopped(serviceType: String) {}
                    override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {}
                    override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {}
                })
            } catch (e: Exception) { /* Ignore setup errors */ }
        }
    }

    private fun logToTerminal(message: String) {
        tvIp.append("\n\n[chehabi@core:~]$ $message")
        tvIp.post {
            val scrollAmount = tvIp.layout.getLineTop(tvIp.lineCount) - tvIp.height
            if (scrollAmount > 0) tvIp.scrollTo(0, scrollAmount)
        }
    }


    private fun decodeModelNumber(rawModel: String): String {
        val modelUpper = rawModel.uppercase(Locale.getDefault()).trim()

        return when {
            //
            modelUpper.contains("2312DRA50C") || modelUpper.contains("2312DRA50G") || modelUpper.contains("2312DRA50I") -> "REDMI NOTE 13 PRO 4G"
            modelUpper.contains("23117RA68G") || modelUpper.contains("2312CRAD3C") -> "REDMI NOTE 13 PRO 5G"
            modelUpper.contains("23090RA98G") -> "REDMI NOTE 13 PRO PLUS"

            //
            modelUpper.contains("24090RA29") || modelUpper.contains("NOTE 14 PRO") || modelUpper.contains("NOTE14PRO") -> "REDMI NOTE 14 PRO"
            modelUpper.contains("24115RA8E") || modelUpper.contains("NOTE 14 PRO+") || modelUpper.contains("NOTE14PRO+") -> "REDMI NOTE 14 PRO PLUS"

            //
            modelUpper.contains("24069RA21") || modelUpper.contains("TURBO 3") -> "REDMI TURBO 3"
            modelUpper.contains("2412DRT0C") || modelUpper.contains("TURBO 4") -> "REDMI TURBO 4"
            modelUpper.contains("TURBO") -> "REDMI TURBO SERIES"

            //
            modelUpper.contains("2201116TG") -> "REDMI NOTE 11 PRO"
            modelUpper.contains("22101316G") -> "XIAOMI 12T PRO"

            //
            modelUpper.contains("IPHONE11,8") || modelUpper.contains("IPHONE XR") -> "IPHONE XR"
            modelUpper.contains("IPHONE12,1") || modelUpper.contains("IPHONE 11") -> "IPHONE 11"
            modelUpper.contains("IPHONE13,4") || modelUpper.contains("IPHONE 12 PRO MAX") -> "IPHONE 12 PRO MAX"
            modelUpper.contains("IPHONE14,5") || modelUpper.contains("IPHONE 13") -> "IPHONE 13"
            modelUpper.contains("IPHONE14,2") || modelUpper.contains("IPHONE14,3") || modelUpper.contains("IPHONE 13 PRO") -> "IPHONE 13 PRO"
            modelUpper.contains("IPHONE15,2") || modelUpper.contains("IPHONE15,3") || modelUpper.contains("IPHONE 14 PRO") -> "IPHONE 14 PRO"
            modelUpper.contains("IPHONE15,4") || modelUpper.contains("IPHONE15,5") || modelUpper.contains("IPHONE 15") -> "IPHONE 15"
            modelUpper.contains("IPHONE16,1") || modelUpper.contains("IPHONE16,2") || modelUpper.contains("IPHONE 15 PRO") -> "IPHONE 15 PRO"
            modelUpper.contains("IPHONE17,3") || modelUpper.contains("IPHONE17,4") || modelUpper.contains("IPHONE 16") -> "IPHONE 16"
            modelUpper.contains("IPHONE17,1") || modelUpper.contains("IPHONE17,2") || modelUpper.contains("IPHONE 16 PRO") -> "IPHONE 16 PRO"

            //
            modelUpper.contains("SM-S948") || modelUpper.contains("SM-S946") || modelUpper.contains("SM-S941") || modelUpper.contains("S26") -> "GALAXY S26 SERIES"
            modelUpper.contains("SM-S928") -> "GALAXY S24 ULTRA"
            modelUpper.contains("SM-S918") -> "GALAXY S23 ULTRA"
            modelUpper.contains("SM-A556") -> "GALAXY A55 5G"
            else -> rawModel
        }
    }

    private fun performFullSecurityAudit() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork)

        val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        val myIp = getIPAddress(true)

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        val ssid = if (isWifi) info.ssid.replace("\"", "") else "CELLULAR_NET"

        var encryptionType = "WPA2-PSK [SECURE]"
        var isSecure = true

        if (isWifi) {
            val scanResults = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                wifiManager.scanResults
            } else null

            val currentScan = scanResults?.find { it.SSID == ssid }
            currentScan?.let {
                val capsStr = it.capabilities.uppercase()
                encryptionType = when {
                    capsStr.contains("WPA3") -> "WPA3-SAE [ULTRA_SECURE]"
                    capsStr.contains("WPA2") -> "WPA2-CCMP [SECURE]"
                    capsStr.contains("WEP") -> "WEP [LEGACY_VULNERABLE]"
                    else -> "OPEN_NETWORK [DANGER_NO_ENCRYPTION]"
                }
                if (capsStr.contains("OPEN") || encryptionType.contains("DANGER")) isSecure = false
            }
        }

        currentNetworkMode = if (isWifi && myIp != "0.0.0.0") {
            NetworkMode.WIFI_SCANNER
        } else {
            NetworkMode.HOTSPOT_MASTER
        }

        tvIp.text = "========================================\n" +
                "   CYBER_MATRIX NETWORK AUDIT MODULE    \n" +
                "========================================"

        tvStatus.text = if (isSecure) "STATUS: SECURE_NODE (WIFI_MODE)" else "STATUS: COMPROMISED/OPEN_NET"
        tvStatus.setTextColor(if (isSecure) Color.parseColor("#00FF41") else Color.RED)

        val auditDetails = StringBuilder().apply {
            append("⚡ INITIALIZING RECONNAISSANCE INTERFACE...\n")
            append("├── [SYS_MODE]  » ${if (isWifi) "WIRELESS_INFRASTRUCTURE" else "ISOLATED_HOTSPOT_AP"}\n")
            append("├── [GATE_SSID] » $ssid\n")
            append("├── [LOCAL_IP]  » $myIp\n")
            append("└── [SECURITY]  » $encryptionType")
        }.toString()

        logToTerminal(auditDetails)
    }

    private fun checkOpenPortsForHost(ip: String): List<OpenPort> {
        val detectedPorts = mutableListOf<OpenPort>()
        val portsToScan = intArrayOf(22, 80, 139, 443, 445, 554, 3389, 5357, 5555, 62078, 8080, 8008, 8009)

        for (port in portsToScan) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(ip, port), 85)
                    val serviceName = PortAnalyzer.getServiceName(port)
                    detectedPorts.add(OpenPort(portNumber = port, serviceName = serviceName))
                }
            } catch (e: Exception) { }
        }
        return detectedPorts
    }

    private fun performNetworkScan() {
        //
        scrollTerminal.visibility = View.VISIBLE
        topologyView.visibility = View.GONE
        btnTopology.text = "VIEW_NETWORK_TOPOLOGY"

        logToTerminal("📡 DEPLOYING PARALLEL PROBES ACROSS NETWORK FABRIC...")
        progressBar.visibility = View.VISIBLE
        btnScan.isEnabled = false

        val myIp = getIPAddress(true)
        val subnet = myIp.substringBeforeLast(".")

        CoroutineScope(Dispatchers.IO).launch {
            val jobs = (1..254).map { i ->
                async {
                    val ip = "$subnet.$i"
                    try {
                        val address = InetAddress.getByName(ip)
                        val startTime = System.currentTimeMillis()

                        if (address.isReachable(120)) {
                            val latency = (System.currentTimeMillis() - startTime).toInt()
                            val openPorts = checkOpenPortsForHost(ip)

                            val role = when {
                                ip.endsWith(".1") -> DeviceRole.GATEWAY
                                ip == myIp -> DeviceRole.MY_DEVICE
                                else -> DeviceRole.CONNECTED_CLIENT
                            }

                            val resolvedHostName = when (role) {
                                DeviceRole.MY_DEVICE -> {
                                    val manufacturer = Build.MANUFACTURER.uppercase(Locale.getDefault())
                                    val decodedModel = decodeModelNumber(Build.MODEL)
                                    if (decodedModel.startsWith(manufacturer)) decodedModel else "$manufacturer $decodedModel"
                                }
                                DeviceRole.GATEWAY -> {
                                    val rawName = address.hostName
                                    if (rawName == ip) "CENTRAL_GATEWAY_ROUTER" else rawName.uppercase(Locale.getDefault())
                                }
                                DeviceRole.CONNECTED_CLIENT -> {
                                    //
                                    val smartName = mDnsDevices[ip]
                                    if (smartName != null) {
                                        smartName
                                    } else {
                                        //
                                        val rawName = address.hostName
                                        val nameUpper = rawName.uppercase(Locale.getDefault())

                                        val isGeneric = rawName == ip || rawName.isNullOrBlank() ||
                                                nameUpper.contains("ANONYMOUS") ||
                                                nameUpper.contains("LOCALHOST") ||
                                                nameUpper.contains("UNKNOWN")

                                        if (!isGeneric) {
                                            decodeModelNumber(nameUpper)
                                        } else {
                                            val isIPhone = openPorts.any { it.portNumber == 62078 }
                                            val isWindows = openPorts.any { it.portNumber in intArrayOf(139, 445, 3389) }
                                            val isLinux = openPorts.any { it.portNumber == 22 }
                                            val isAndroidAdb = openPorts.any { it.portNumber == 5555 }
                                            val isIpCamera = openPorts.any { it.portNumber == 554 }
                                            val isWebDevice = openPorts.any { it.portNumber in intArrayOf(80, 443, 8080) }

                                            when {
                                                isIPhone -> "APPLE_IPHONE_NODE"
                                                isWindows -> "WINDOWS_PC_NODE"
                                                isLinux -> "LINUX_SERVER_NODE"
                                                isAndroidAdb -> "ANDROID_DEVICE_NODE"
                                                isIpCamera -> "IP_STREAMING_CAMERA"
                                                isWebDevice -> "NET_INFRASTRUCTURE_NODE"
                                                else -> "ACTIVE_NODE (PORTS_SHIELDED)"
                                            }
                                        }
                                    }
                                }
                            }

                            NetworkDevice(
                                ipAddress = ip,
                                deviceName = resolvedHostName,
                                openPorts = openPorts,
                                role = role,
                                latencyMs = latency
                            )
                        } else null
                    } catch (e: Exception) { null }
                }
            }

            val discoveredDevices = jobs.awaitAll().filterNotNull()

            withContext(Dispatchers.Main) {
                progressBar.visibility = View.GONE
                btnScan.isEnabled = true

                //
                discoveredDevicesList.clear()
                discoveredDevicesList.addAll(discoveredDevices)
                // ----------------------------------------------------

                logToTerminal("/* MAP SCAN COMPLETE — DISCOVERED NODES: ${discoveredDevices.size} */")

                discoveredDevices.forEach { device ->
                    val sb = StringBuilder().apply {
                        val roleIcon = when(device.role) {
                            DeviceRole.GATEWAY -> "▶ [INFRASTRUCTURE_ROUTER]"
                            DeviceRole.MY_DEVICE -> "⚡ [HOST_CONTROLLER (YOU)]"
                            DeviceRole.CONNECTED_CLIENT -> "▼ [TARGET_NODE]"
                        }

                        append("$roleIcon\n")
                        append("├── IPv4_ADDR » ${device.ipAddress}\n")
                        append("├── HOST_NAME » ${device.deviceName}\n")
                        append("├── PING_RTT  » ${device.latencyMs} ms\n")

                        if (device.openPorts.isEmpty()) {
                            append("└── PORT_STEALTH » [✔] SAFE / ALL CHANNELS CLOSED")
                        } else {
                            val vulnTag = if(device.isVulnerable) "[☠ VULNERABLE]" else "[SECURE]"
                            append("└── EXPOSED_CHANNELS $vulnTag:\n")
                            device.openPorts.forEachIndexed { index, port ->
                                val isLast = index == device.openPorts.lastIndex
                                val prefix = if (isLast) "    └── " else "    ├── "
                                append("$prefix$port\n")
                            }
                        }
                    }
                    logToTerminal(sb.toString().trimEnd())
                }
            }
        }
    }

    private fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress ?: ""
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4 && isIPv4) return sAddr
                    }
                }
            }
        } catch (ex: Exception) { }
        return "0.0.0.0"
    }
}

// =========================================================
// 🚀 CUSTOM VIEW: RENDERS CONCENTRIC RADAR TOPOLOGY CHANNELS
// =========================================================
class TopologyView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var devices: List<NetworkDevice> = emptyList()
        set(value) { field = value; invalidate() }

    //
    private var pulseRadius = 0f
    private var animator: ValueAnimator? = null

    private val pulsePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00FF41")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    // ------------------------------------------

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2200FF41") // خطوط ربط باهتة خضراء تليق بستايل الماتريكس
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }

    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00FF41")
        typeface = android.graphics.Typeface.MONOSPACE
        textAlign = Paint.Align.CENTER
    }

    //
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startRadarAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun startRadarAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                val fraction = it.animatedValue as Float
                pulseRadius = (Math.min(width, height) * 0.45f) * fraction
                pulsePaint.alpha = ((1f - fraction) * 200).toInt()
                invalidate()
            }
            start()
        }
    }
    // -----------------------------------------

    override fun onDraw(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f

        //
        val radius = Math.min(width, height) * 0.35f

        //
        canvas.drawCircle(centerX, centerY, pulseRadius, pulsePaint)

        //
        nodePaint.color = Color.parseColor("#00E5FF") //
        canvas.drawCircle(centerX, centerY, 35f, nodePaint)

        textPaint.color = Color.parseColor("#00E5FF")
        textPaint.textSize = 28f
        canvas.drawText("GATEWAY", centerX, centerY + 65, textPaint)

        //
        val clientDevices = devices.filter { it.role != DeviceRole.GATEWAY }
        if (clientDevices.isEmpty()) return

        //
        clientDevices.forEachIndexed { index, device ->
            val angle = (2 * Math.PI * index / clientDevices.size)
            val nodeX = centerX + radius * Math.cos(angle).toFloat()
            val nodeY = centerY + radius * Math.sin(angle).toFloat()

            //
            canvas.drawLine(centerX, centerY, nodeX, nodeY, linePaint)

            //
            nodePaint.color = if (device.isVulnerable) {
                Color.parseColor("#FF1744")
            } else {
                Color.parseColor("#00FF41")
            }
            canvas.drawCircle(nodeX, nodeY, 25f, nodePaint)


            textPaint.color = Color.WHITE
            textPaint.textSize = 20f
            val label = if (device.deviceName.length > 12) device.deviceName.substring(0, 10) + ".." else device.deviceName
            canvas.drawText(label, nodeX, nodeY - 35, textPaint)

            textPaint.color = Color.parseColor("#88AAAAAA")
            canvas.drawText(device.ipAddress.substringAfterLast("."), nodeX, nodeY + 45, textPaint)
        }
    }
}