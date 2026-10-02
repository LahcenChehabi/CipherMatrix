<div align="center">

<img src="assets/logo.png" width="140" alt="CipherMatrix logo">

# 🕶️ CipherMatrix

### A calculator that isn't just a calculator.

A disguised Android security vault — encrypted password manager, threat intelligence dashboard, and a full cybersecurity toolkit, hidden behind an ordinary calculator interface.

![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-SDK%2029%2B-3DDC84?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase&logoColor=white)
![Encryption](https://img.shields.io/badge/Encryption-AES--256%20%7C%20Keystore-blueviolet)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)

</div>

---

## 📑 Table of Contents

- [Overview](#-overview)
- [How It Works](#-how-it-works)
- [Architecture: Accounts & Data Isolation](#-architecture-accounts--data-isolation)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Security Notes](#-security-notes)
- [Roadmap](#-roadmap)
- [License](#-license)

---

## 🔎 Overview

CipherMatrix is built around a simple idea: **security tools that hide in plain sight.** The app opens as a fully working calculator — punch in numbers, do real math, even speak an equation out loud. But that calculator is a front door. Type the right sequence and the screen transitions into a personal login, then a Matrix-style command center giving access to a private encrypted vault, network tools, threat intelligence, and more.

Every account is fully isolated: each person who uses the app gets their own unlock code, their own login, and their own private vault — no shared data between users.

### At a Glance

| | |
|---|---|
| **Entry point** | A fully functional calculator, with voice input |
| **Accounts** | Firebase Authentication, one isolated vault per user |
| **Encryption** | AES-256-GCM, keys generated and sealed in the Android Keystore |
| **Security tools** | 8 built-in modules — vault, URL scanner, network inspector, privacy audit, steganography lab, threat map, QR scanner, intruder logs |
| **Intrusion response** | Silent front-camera capture on every wrong unlock attempt |

## ⚙️ How It Works

```mermaid
flowchart TD
    A[App opens as a<br/>working Calculator] -->|User types a sequence| B{Sequence ends<br/>with '='}
    B -->|First time ever: saved as<br/>this device's unlock code| C[🔑 Login Screen]
    B -->|Correct unlock code| C
    B -->|Anything else| D[Normal calculation result<br/>shown on screen]
    D -->|Silently in the background| E[📸 Front camera takes<br/>a hidden photo]
    E --> F[Saved to a hidden<br/>Intruder Logs folder]
    C -->|Sign in or create account| G[🎬 Matrix Welcome Screen<br/>Dynamic TTS voice greeting]
    G --> H[🖥️ Security Dashboard]
    H --> I[🔐 Password Vault<br/>PIN / Fingerprint gate]
    H --> J[🌐 URL & QR Scanner<br/>VirusTotal check]
    H --> K[📡 Network Inspector]
    H --> L[🕵️ Privacy Audit]
    H --> M[🖼️ Steganography Lab]
    H --> N[🗺️ Global Threat Map]
    H --> O[📰 Security News Feed]
    H --> P[🚨 Intruder Logs Gallery]
```

**In short:** every calculation you make is real. Every wrong unlock attempt is quietly logged with a photo. The right code leads to a login, and only a valid account reaches the vault.

## 🔐 Architecture: Accounts & Data Isolation

Each account created in the app is isolated from every other account, end to end — from sign-in to the actual password data.

```mermaid
flowchart LR
    subgraph Device["📱 On-device"]
        A[Calculator unlock code<br/>unique per device] --> B[Login / Create Account]
    end

    subgraph FirebaseAuth["🔑 Firebase Authentication"]
        B --> C[Email + Password<br/>verified against account]
        C --> D[Unique UID issued<br/>per account]
    end

    subgraph Firestore["☁️ Firestore Database"]
        D --> E["users/{uid}/vault/"]
        E --> F[Only this account's<br/>AES-256-GCM encrypted credentials]
    end

    F -.->|Security Rule| G{"request.auth.uid<br/>== userId ?"}
    G -->|Yes| F
    G -->|No, different account| H[🚫 Access Denied]
```

**What this guarantees:**
- Two people using the app never see each other's saved passwords — each account's data lives in its own `users/{uid}/vault` path.
- The Firestore security rules enforce this server-side: even if someone tried to query another account's data directly, the rule `request.auth.uid == userId` blocks it.
- Access control is enforced at the database level, not just in the app's interface.
- Every credential is encrypted with a key that is generated and sealed inside the Android Keystore on first use — it never exists as plain text in source code or leaves secure hardware.

## ✨ Features

### 🔑 Accounts & Vault Access
| Feature | Description |
|---|---|
| **Calculator disguise** | A fully functional calculator (with voice input) is the app's real launcher icon and home screen |
| **Per-device unlock code** | The first calculation ever entered on a device silently becomes that device's unlock sequence |
| **Intruder Selfie** | Any *incorrect* unlock attempt silently snaps a front-camera photo and stores it in a hidden log |
| **Intruder Logs Gallery** | A gallery view (with timestamps) of everyone who tried to guess the code |
| **Account login system** | Email/password accounts via Firebase Authentication — create an account or sign back in |
| **Dynamic voice greeting** | Text-to-Speech welcomes each account by name on login — no fixed recording |
| **Per-user vault isolation** | Every account's saved credentials live in their own private Firestore path, enforced server-side |
| **PIN + Fingerprint vault lock** | A second layer on top of login: PIN code or biometric authentication gates the vault itself |
| **Auto re-lock** | The vault automatically re-locks itself the moment the app is minimized |
| **Keystore-backed AES-256-GCM** | Credentials are encrypted with a hardware-sealed key — not a string in source code |

### 🛰️ Threat & Network Tools
| Feature | Description |
|---|---|
| **URL Scanner** | Submits any link to the VirusTotal API and reports back whether it's flagged as malicious |
| **Secure QR Scanner** | Scans QR codes and automatically routes any URL through the URL Scanner before you ever open it |
| **Network Inspector** | Discovers devices on the local Wi-Fi network and reports connectivity details |
| **Privacy Audit** | Scans installed apps and permissions on the device, flagging potential privacy risks in a terminal-style readout |
| **Steganography Lab** | Hides and extracts secret text messages inside image files |
| **Global Threat Map** | An embedded live map visualizing cyberattacks happening around the world in real time |

### 🧠 Awareness & Experience
| Feature | Description |
|---|---|
| **Security News Feed** | Pulls live cybersecurity headlines from a news API |
| **Matrix-style UI** | Custom animated "digital rain" and glitch-text views used throughout the app |
| **Immersive sound design** | Distinct sound cues for access granted, access denied, and intrusion alerts |
| **Voice input** | Speak an equation out loud on the calculator screen and it's transcribed and calculated |

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | AndroidX, Material 3 Components, custom `Canvas`-based views |
| Camera | Camera2 API (silent front-camera capture), CameraX (calculator) |
| Authentication | Firebase Authentication (Email/Password) |
| Biometrics | AndroidX Biometric (fingerprint vault gate) |
| Speech | Android TextToSpeech (dynamic voice greeting) |
| Networking | Retrofit2 + Gson, OkHttp logging interceptor |
| Cloud data | Firebase Firestore (per-user isolated collections) |
| Image loading | Glide |
| QR scanning | ZXing (`journeyapps` embedded) |
| Cryptography | AES-256-GCM via the Android Keystore |
| Async | Kotlin Coroutines |
| Math engine | exp4j (expression evaluation for the calculator) |
| External APIs | VirusTotal (URL reputation), News API (security headlines) |

## 📂 Project Structure

```
CipherMatrix/
└── app/src/main/
    ├── java/com/example/ciphermatrix/
    │   ├── MainActivity.kt              Calculator front-end + hidden camera trigger
    │   ├── LoginActivity.kt             Account login / registration (Firebase Auth)
    │   ├── WelcomeActivity.kt           Matrix intro animation + dynamic TTS greeting
    │   ├── SecurityDashboardActivity.kt Central hub / fake terminal animation
    │   ├── VaultAuthActivity.kt         PIN + fingerprint gate for the vault
    │   ├── PasswordsVaultActivity.kt    Encrypted, per-user credential manager
    │   ├── CryptoHelper.kt              Keystore-backed AES-256-GCM encrypt/decrypt
    │   ├── Credential.kt / CredentialAdapter.kt
    │   ├── UrlScannerActivity.kt / VirusTotalService.kt
    │   ├── SecureQrScannerActivity.kt
    │   ├── NetworkInspectorActivity.kt / NetworkDevice.kt
    │   ├── PrivacyAuditActivity.kt
    │   ├── StegoLabActivity.kt
    │   ├── GlobalThreatMapActivity.kt
    │   ├── IntruderAlertActivity.kt / IntruderLogsActivity.kt
    │   ├── NewsActivity.kt / NewsAdapter.kt / NewsArticle.kt
    │   ├── SoundManager.kt
    │   └── MatrixRainView.kt / RainViewDashboard.kt / ErrorMatrixView.kt
    └── res/raw/                         Sound effects (access granted/denied, intruder alert)
```

## 🔒 Security Notes

- **Per-user data isolation is enforced server-side** via Firestore security rules, not just in the app's UI — this holds even if the client code were bypassed.
- **Encryption keys never touch source code.** The AES-256-GCM key used to protect the vault is generated on first use and sealed inside the Android Keystore, backed by hardware where available.
- API keys (VirusTotal, News API) live in `local.properties` / `BuildConfig` fields, never committed to source.
- The hidden-camera "intruder selfie" feature captures photos without the other person's knowledge — this is intended purely as a personal anti-tamper feature for *your own device*.

## 🛣️ Roadmap

- [x] Per-user login / account system (Firebase Authentication)
- [x] Per-user vault isolation at the database level
- [x] Dynamic voice greeting (TTS) instead of a fixed recording
- [x] Keystore-backed AES-256-GCM encryption (no hardcoded key)
- [ ] More detection & security tools added over time

## 📄 License

Copyright (c) 2026 Lahcen Chehabi. All rights reserved.

This source code is made publicly visible for viewing and evaluation purposes only. No permission is granted to use, copy, modify, merge, publish, distribute, sublicense, or sell this software, in original or modified form, for any purpose without prior written permission from the author. See [LICENSE](LICENSE) for full terms.

---

<div align="center">

**Built by [Lahcen Chehabi](https://github.com/LahcenChehabi)**

</div>