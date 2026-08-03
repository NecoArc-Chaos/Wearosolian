# 首页数据官方 API 接入方案

由于目前的首页（HomeScreen）使用的是硬编码的模拟数据，本方案将接入 DysonNetwork 官方 API 来替换这些数据。

## 待接入的官方 API
1. **今日签文**：使用 `sphere/accounts/me/checkin` (或 `sphere/checkin`) 获取当前用户的签到状态和签文。
2. **通知计数**：使用 `messager/chat/summary` 获取未读消息总数。
3. **重要倒计时**：DysonNetwork 目前没有统一的倒计时 API，方案中将保持该组件，但尝试从更动态的数据源（如配置文件或简单的本地计算）中获取，或预留扩展接口。

## Proposed Changes

### [Component] Data & API Layer

#### [MODIFY] [Models.kt](file:///Users/nickole/wearosolian/app/src/main/kotlin/dev/solsynth/solian/data/model/Models.kt)
添加 `SnCheckInStatus` 数据模型。

#### [MODIFY] [SolianApi.kt](file:///Users/nickole/wearosolian/app/src/main/kotlin/dev/solsynth/solian/data/api/SolianApi.kt)
添加获取签到状态的 API 定义。

### [Component] UI Layer

#### [NEW] [HomeViewModel.kt](file:///Users/nickole/wearosolian/app/src/main/kotlin/dev/solsynth/solian/ui/home/HomeViewModel.kt)
创建 `HomeViewModel`，负责聚合首页所需的所有数据。

#### [MODIFY] [HomeScreen.kt](file:///Users/nickole/wearosolian/app/src/main/kotlin/dev/solsynth/solian/ui/home/HomeScreen.kt)
接入 `HomeViewModel`，并使用真实数据渲染界面。

## Verification Plan

### Automated Tests
- 编译并运行应用，检查首页加载状态。
- 使用模拟的 API 返回值验证界面显示是否正确（如果环境支持拦截）。

### Manual Verification
- 登录后进入首页，观察“今日签文”和“通知”是否成功加载。
- 确认未读消息数与“消息”页面的总和一致。
