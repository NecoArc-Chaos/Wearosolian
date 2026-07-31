# Wearosolian

<p align="center">
  <img src="assets/icons/icon.webp" width="120" alt="Wearosolian Logo">
</p>

<p align="center">
  <b>Solar Network client for Wear OS</b>
</p>

<p align="center">
  <a href="LICENSE.txt"><img src="https://img.shields.io/badge/license-AGPL--3.0-blue" alt="License"></a>
</p>

---

**Wearosolian** is a Wear OS fork of [Solian (Solar Network)](https://github.com/Solsynth/HyperNet.Surface) — a peaceful social networking platform. This fork strips all desktop (Windows/macOS/Linux), iOS, and web code, targeting **Android / Wear OS** exclusively.

> This is a third-party fork. The official project is at [Solsynth/HyperNet.Surface](https://github.com/Solsynth/HyperNet.Surface).

---

## Features

| Feature | Description |
|---------|-------------|
| Timeline | Chronological feed of posts |
| Posts | Create and view posts |
| Instant Messaging | Real-time chat with group support |
| OAuth Integration | Secure third-party authentication |
| Check-in | Location and status sharing |
| Countdown | Track special dates and festivals |
| Account | Profile and status management |
| QR Login | Scan QR code to authenticate |

---

## Getting Started

### For Users

1. **Download the APK** from [GitHub Releases](https://github.com/NecoArc-Chaos/Wearosolian/releases)
2. **Sideload** to your Wear OS device via ADB or a companion app
3. **Sign up** on Solar Network and start exploring from your wrist!

### For Developers

#### Prerequisites

- Android Studio (Ladybug or newer)
- Android SDK with Wear OS system images (if using emulator)
- JDK 21

#### Running

```bash
# Clone the repository
git clone https://github.com/NecoArc-Chaos/Wearosolian.git
cd Wearosolian

# Build and run on connected Wear OS device/emulator
./gradlew installDebug
```

#### Building

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore configuration)
./gradlew assembleRelease
```

---

## Differences from Upstream

- ✅ **Wear OS only**
- ✅ Minimal dependencies — no `window_manager`, `desktop_drop`, `tray_manager`, etc.
- ✅ Pure Kotlin + Jetpack Compose (Wear Compose Material 3)
- ✅ Retrofit + OkHttp for networking
- ✅ EncryptedSharedPreferences for secure token storage

---

## Server

The backend is the same as upstream: **[Solsynth/DysonNetwork](https://github.com/Solsynth/DysonNetwork)**

---

## Wear OS Adaptation

This fork is optimized for Wear OS smartwatches:

| Feature | Implementation |
|---------|---------------|
| **Crown scrolling** | Wear Compose `RotaryScrollableDefaults` |
| **Round watch** | `rememberIsScreenRound()` + dynamic padding |
| **Scaling list** | `ScalingLazyColumn` with rotary behavior |
| **Ambient mode** | Implemented |
| **Swipe-to-back** | Stub (not yet implemented) |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Wear Compose Material 3 |
| **Networking** | Retrofit + OkHttp + WebSocket |
| **Local Storage** | EncryptedSharedPreferences |
| **Backend** | .NET + PostgreSQL |
| **Protocols** | ActivityPub (Fediverse), WebSockets, REST API |

---

## License

This project is licensed under **AGPL-3.0**, same as upstream. See [LICENSE.txt](./LICENSE.txt).

Original authorship and copyright attribution to **LittleSheep, Solsynth**, and the Solar Network contributors must be retained.

Third-party deployments must not impersonate the official Solar Network service.

---

<p align="center">
  Forked with ❤️ from <a href="https://github.com/Solsynth/HyperNet.Surface">Solsynth/HyperNet.Surface</a>
</p>
