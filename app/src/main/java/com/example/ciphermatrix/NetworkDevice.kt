package com.example.ciphermatrix


data class OpenPort(
    val portNumber: Int,
    val serviceName: String,
    val riskLevel: PortAnalyzer.RiskLevel = PortAnalyzer.getRiskLevel(portNumber)
) {
    override fun toString(): String {
        return "PORT $portNumber » $serviceName [$riskLevel]"
    }
}


data class NetworkDevice(
    val ipAddress: String,
    val deviceName: String,
    val openPorts: List<OpenPort>,
    val role: DeviceRole,
    val latencyMs: Int,
    val isVulnerable: Boolean = openPorts.isNotEmpty(),
    var isBlocked: Boolean = false
)


enum class DeviceRole {
    GATEWAY,
    MY_DEVICE,
    CONNECTED_CLIENT
}


data class NetworkStatus(
    val myIpAddress: String,
    val networkName: String,
    val encryptionType: String,
    val isSecure: Boolean,
    val activeMode: NetworkMode
)


enum class NetworkMode {
    WIFI_SCANNER,
    HOTSPOT_MASTER
}


object PortAnalyzer {
    enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

    fun getServiceName(port: Int): String {
        return when (port) {
            22 -> "SSH (Secure Shell Access)"
            80 -> "HTTP (Unencrypted Web Interface)"
            135 -> "MS-RPC (Windows Management)"
            139, 445 -> "SMB (Windows File Sharing - OS_VULN)"
            443 -> "HTTPS (Secure Web Server)"
            554 -> "RTSP (IP Camera Streaming Video)"
            3389 -> "RDP (Remote Desktop)"
            5353 -> "mDNS (Service Discovery - Android/IoT)"
            5357 -> "WS-Discovery (Windows Network Discovery)"
            5555 -> "ADB (Android Debug Bridge)"
            8008, 8009 -> "Google Cast (Android/Chromecast)"
            8080 -> "HTTP-Proxy Server"
            62078 -> "Apple Mobile Device (iOS Lockdown Service)"
            else -> "UNKNOWN SERVICE"
        }
    }

    fun getRiskLevel(port: Int): RiskLevel {
        return when (port) {
            22, 3389, 445, 5555 -> RiskLevel.CRITICAL
            21, 23, 80 -> RiskLevel.HIGH
            else -> RiskLevel.LOW
        }
    }
}