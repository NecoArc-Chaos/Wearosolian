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
| Posts, Articles & Moments | Multiple content types |
| Instant Messaging | Real-time chat with group support |
| Realms | Communities organized by shared interests |
| OAuth Integration | Secure third-party authentication |
| Check-in | Location and status sharing |
| Countdown | Track special dates and festivals |
| RSS Reader | Subscribe to external feeds |
| Wallet | Credit system for transactions |
| Stickers | Custom sticker expressions |
| Rich Text Editor | Markdown-based with extended syntax |
| Social Features | Friends list and blocklist |
| File Management | Upload and organize files |
| AI Features | Smart assistance throughout the app |
| Fitness & Health | Track fitness goals on your wrist |
| Fediverse | Interact with other fediverse instances (experimental) |

---

## Getting Started

### For Users

1. **Download the APK** from [GitHub Releases](https://github.com/NecoArc-Chaos/Wearosolian/releases)
2. **Sideload** to your Wear OS device via ADB or a companion app
3. **Sign up** on Solar Network and start exploring from your wrist!

### For Developers

#### Prerequisites

- [Flutter SDK](https://flutter.dev) (≥3.10.0)
- Android SDK with Wear OS system images (if using emulator)

#### Running

```bash
# Install dependencies
flutter pub get

# Generate code
dart run build_runner build

# Run on connected Wear OS device/emulator
flutter run
```

#### Building

```bash
# Build APK
flutter build apk

# Build App Bundle
flutter build appbundle
```

---

## Differences from Upstream

- ✅ **Android / Wear OS only** — all desktop (Windows/macOS/Linux), iOS, and web code removed
- ✅ Minimal dependencies — no `window_manager`, `desktop_drop`, `tray_manager`, etc.
- ✅ Apple Sign-In → OIDC web flow (native `sign_in_with_apple` removed)
- ✅ Desktop RPC / Discord presence stubbed out
- ✅ Call window / multi-window code stubbed out

---

## Server

The backend is the same as upstream: **[Solsynth/DysonNetwork](https://github.com/Solsynth/DysonNetwork)**

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Frontend** | Flutter (Dart) — Wear OS target |
| **State** | Riverpod + Hooks |
| **Local DB** | Drift (SQLite) |
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
