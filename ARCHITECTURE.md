# MobileCodex 架构设计文档

## 1. 架构概览

```
┌─────────────────────────────────────────────────┐
│                  UI Layer (Compose)               │
│  ┌──────────┐  ┌──────────┐  ┌───────────────┐  │
│  │ChatScreen│  │FilesScreen│  │SettingsScreen │  │
│  └────┬─────┘  └─────┬────┘  └───────┬───────┘  │
│       │               │               │          │
├───────┼───────────────┼───────────────┼──────────┤
│  ViewModel Layer (StateFlow + MVVM)    │          │
│  ┌────┴─────┐  ┌──────┴────┐  ┌──────┴───────┐  │
│  │ChatViewModel│ │FileViewModel│ │SettingsVM  │   │
│  │GitHubViewModel│ │MainViewModel│            │   │
│  └────┬─────┘  └──────┬────┘  └──────┬───────┘  │
│       │               │               │          │
├───────┼───────────────┼───────────────┼──────────┤
│  Repository Layer                       │          │
│  ┌────┴─────┐  ┌──────┴────┐  ┌──────┴───────┐  │
│  │ChatRepo  │  │GitHubRepo │  │SettingsRepo  │   │
│  └────┬─────┘  └──────┬────┘  └──────┬───────┘  │
│       │               │               │          │
├───────┼───────────────┼───────────────┼──────────┤
│  Data Layer                             │          │
│  ┌────┴─────┐  ┌──────┴────┐  ┌──────┴───────┐  │
│  │OpenAIApi │  │ GitHubApi │  │  DataStore   │   │
│  └──────────┘  └───────────┘  └──────────────┘  │
└─────────────────────────────────────────────────┘
```

## 2. 数据流

### 单向数据流 (UDF)
```
User Action → ViewModel.onAction()
  → Repository.suspend fun()
    → API.call()
      → Result<T>
    ← Result<T>
  ← StateFlow.update()
→ UI recomposes
```

### 状态管理
- 每个 ViewModel 持有自己的 `MutableStateFlow<UiState>`
- UI 通过 `collectAsState()` 订阅状态变化
- 跨 ViewModel 通信通过直接引用（SettingsViewModel 被其他 VM 共享）

## 3. 依赖注入

使用 Hilt 进行依赖注入：
- `@HiltAndroidApp` → Application
- `@AndroidEntryPoint` → Activity
- `@HiltViewModel` → ViewModel
- `@ViewModelScoped` → Repository

依赖关系图：
```
MainActivity
  ├── MainViewModel
  ├── SettingsViewModel → SettingsRepository → DataStore
  ├── ChatViewModel → ChatRepository → OpenAIApi
  ├── GitHubViewModel → GitHubRepository → GitHubApi
  └── FileViewModel → GitHubRepository → GitHubApi
```

## 4. 导航设计

```
MainActivity (ModalNavigationDrawer)
  ├── 侧边栏
  │   ├── AI 对话 (CHAT)
  │   ├── 仓库列表 (REPOSITORIES)
  │   ├── 文件浏览 (FILES)
  │   └── 设置 (SETTINGS)
  └── 主内容区
      ├── ChatScreen
      │   ├── 对话列表（左侧边栏）
      │   └── 聊天区域
      ├── FilesScreen
      │   ├── 仓库列表 / 文件树
      │   └── 代码编辑器
      └── SettingsScreen (全屏覆盖)
```

## 5. API 层设计

### OpenAI API (OpenAIApi.kt)
- `POST chat/completions` - 标准 Chat Completions
- `GET models` - 模型列表
- 鉴权方式：`Authorization: Bearer {apiKey}`
- 支持自定义 Base URL（兼容其他 LLM 服务）

### GitHub API (GitHubApi.kt)
- 鉴权方式：`Authorization: Bearer {token}`
- 完整 REST API 封装
- 支持分页、搜索、分支

## 6. 关键设计决策

| 决策 | 原因 |
|------|------|
| DataStore 而非 Room | 设置类数据更适合键值存储 |
| 单 Activity 架构 | Compose Navigation 最佳实践 |
| ViewModel 间直接引用 | 简化设置共享，避免过度抽象 |
| Result<T> 错误处理 | 显式成功/失败语义 |
| Flow + StateFlow | 响应式 UI 更新 |
