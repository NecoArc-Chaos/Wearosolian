# Wearosolian

<p align="center">
  <img src="assets/icons/icon.webp" width="120" alt="Wearosolian Logo">
</p>

<p align="center">
  <b>Wear OS 版 Solar Network 客户端</b>
</p>

<p align="center">
  <a href="LICENSE.txt"><img src="https://img.shields.io/badge/license-AGPL--3.0-blue" alt="License"></a>
</p>

---

**Wearosolian** 是 [Solian (Solar Network)](https://github.com/Solsynth/HyperNet.Surface) 的 Wear OS 分支。Solian 是一个宁静和谐的社交平台。本分支移除了所有桌面端（Windows/macOS/Linux）、iOS 和 Web 代码，**专注于 Android / Wear OS**。

> 这是第三方分支。官方项目位于 [Solsynth/HyperNet.Surface](https://github.com/Solsynth/HyperNet.Surface)。

---

## 功能特性

| 功能 | 描述 |
|------|------|
| 时间线 | 按时间顺序展示动态 |
| 帖子、文章与瞬间 | 多种内容类型 |
| 即时通讯 | 实时聊天，支持群组 |
| 领域 | 按兴趣组织的社区 |
| OAuth 集成 | 安全的第三方认证 |
| 签到 | 位置和状态分享 |
| 倒计时 | 追踪特殊日期和节日 |
| RSS 阅读器 | 订阅外部资讯源 |
| 钱包 | 积分交易系统 |
| 贴纸 | 自定义贴纸表达 |
| 富文本编辑器 | 基于 Markdown，支持扩展语法 |
| 社交功能 | 好友列表和黑名单管理 |
| 文件管理 | 上传和管理文件 |
| AI 功能 | 智能助手功能 |
| 运动与健康 | 在手腕上追踪运动目标 |
| 联邦网络 | 与其他联邦实例互动（实验性） |

---

## 开始使用

### 普通用户

1. 从 [GitHub Releases](https://github.com/NecoArc-Chaos/Wearosolian/releases) 下载 APK
2. 通过 ADB 或配套应用**侧载**到 Wear OS 设备
3. 在 Solar Network 上**注册账号**，开始在手腕上探索！

### 开发者

#### 前置要求

- [Flutter SDK](https://flutter.dev)（≥3.10.0）
- Android SDK（使用模拟器需 Wear OS 系统镜像）

#### 运行

```bash
# 安装依赖
flutter pub get

# 生成代码
dart run build_runner build

# 在连接的 Wear OS 设备/模拟器上运行
flutter run
```

#### 构建

```bash
# 构建 APK
flutter build apk

# 构建 App Bundle
flutter build appbundle
```

---

## 与上游的区别

- ✅ **仅 Android / Wear OS** — 移除了所有桌面端、iOS 和 Web 代码
- ✅ 精简依赖 — 无 `window_manager`、`desktop_drop`、`tray_manager` 等
- ✅ Apple 登录 → OIDC Web 流程（移除了原生 `sign_in_with_apple`）
- ✅ 桌面 RPC / Discord 在线状态已移除
- ✅ 通话窗口 / 多窗口代码已移除

---

## 服务端

与上游相同：**[Solsynth/DysonNetwork](https://github.com/Solsynth/DysonNetwork)**

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **前端** | Flutter (Dart) — Wear OS 目标 |
| **状态管理** | Riverpod + Hooks |
| **本地数据库** | Drift (SQLite) |
| **后端** | .NET + PostgreSQL |
| **协议** | ActivityPub (联邦宇宙)、WebSockets、REST API |

---

## 许可协议

本项目采用 **AGPL-3.0** 授权，与上游一致。详见 [LICENSE.txt](./LICENSE.txt)。

原始作者身份和版权归属 **LittleSheep、Solsynth** 及 Solar Network 贡献者必须保留。

第三方部署不得冒充官方 Solar Network 服务。

---

<p align="center">
  基于 <a href="https://github.com/Solsynth/HyperNet.Surface">Solsynth/HyperNet.Surface</a> 修改
</p>
