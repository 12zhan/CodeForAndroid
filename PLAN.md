# Mobile Codex - 数据模型与 ViewModel 架构设计计划

## 目标
为 Mobile Codex Android 应用生成数据模型（Models）和 ViewModel 架构设计代码，基于 Kotlin、Jetpack Compose、MVVM 架构。

## 技术栈
- Kotlin
- Jetpack Compose
- MVVM 架构
- Hilt 依赖注入
- Kotlin Coroutines & Flow
- Retrofit + OkHttp
- DataStore

## 实施状态

### ✅ 已完成

#### 1. 数据模型（12个文件）
- `GitHubRepository.kt` - GitHub 仓库模型
- `GitHubUser.kt` - GitHub 用户模型
- `GitTreeNode.kt` - Git 树节点模型
- `FileContent.kt` - 文件内容模型
- `VirtualFile.kt` - 虚拟文件系统模型
- `WorkspaceState.kt` - 工作区状态模型
- `ChatMessage.kt` - 聊天消息模型
- `ChatConversation.kt` - 聊天对话模型
- `AISettings.kt` - AI 设置模型
- `GitHubSettings.kt` - GitHub 设置模型
- `AppSettings.kt` - 应用设置模型
- `WorkspaceOption.kt` - 工作区选项模型

#### 2. ViewModel（5个文件）
- `MainViewModel.kt` - 主 ViewModel（全局状态管理）
- `GitHubViewModel.kt` - GitHub 操作 ViewModel
- `FileViewModel.kt` - 文件管理 ViewModel
- `ChatViewModel.kt` - AI 对话 ViewModel
- `SettingsViewModel.kt` - 设置管理 ViewModel

#### 3. 文档
- `ARCHITECTURE.md` - 架构文档
- `PLAN.md` - 计划文档

### ⏳ 待实现

#### 4. 数据仓库层（Repository）
- `GitHubRepository` - 封装 GitHub API 调用
- `FileRepository` - 封装文件系统操作（VFS）
- `ChatRepository` - 封装 AI 对话逻辑
- `SettingsRepository` - 封装 DataStore 操作

#### 5. 依赖注入模块（DI）
- `AppModule.kt` - 应用级依赖
- `ViewModelModule.kt` - ViewModel 依赖

#### 6. API 接口
- `GitHubApi.kt` - GitHub REST API 接口
- `OpenAIApi.kt` - OpenAI API 接口

#### 7. UI 层
- Jetpack Compose 组件
- Material Design 3 主题
- 导航组件

## 文件结构预览
```
app/src/main/java/com/mobilecodex/
├── model/                          ✅ 已完成
│   ├── AISettings.kt
│   ├── AppSettings.kt
│   ├── ChatConversation.kt
│   ├── ChatMessage.kt
│   ├── FileContent.kt
│   ├── GitHubRepository.kt
│   ├── GitHubSettings.kt
│   ├── GitHubUser.kt
│   ├── GitTreeNode.kt
│   ├── VirtualFile.kt
│   ├── WorkspaceOption.kt
│   └── WorkspaceState.kt
├── viewmodel/                      ✅ 已完成
│   ├── MainViewModel.kt
│   ├── GitHubViewModel.kt
│   ├── FileViewModel.kt
│   ├── ChatViewModel.kt
│   └── SettingsViewModel.kt
├── data/                           ⏳ 待实现
│   ├── repository/
│   │   ├── GitHubRepository.kt
│   │   ├── FileRepository.kt
│   │   ├── ChatRepository.kt
│   │   └── SettingsRepository.kt
│   ├── api/
│   │   ├── GitHubApi.kt
│   │   └── OpenAIApi.kt
│   └── local/
│       ├── DataStoreManager.kt
│       └── AppDatabase.kt
├── di/                             ⏳ 待实现
│   ├── AppModule.kt
│   └── ViewModelModule.kt
└── ui/                             ⏳ 待实现
    ├── theme/
    ├── components/
    └── screens/
```

## 设计原则

1. **单一数据源 (Single Source of Truth)**
   - 所有状态都通过 StateFlow 管理
   - UI 只能通过 ViewModel 修改状态

2. **不可变性 (Immutability)**
   - 所有数据类都是不可变的
   - 使用 `copy()` 方法创建新实例

3. **单向数据流 (Unidirectional Data Flow)**
   - UI → ViewModel → Repository → Data Source
   - Data Source → Repository → ViewModel → UI

4. **关注点分离 (Separation of Concerns)**
   - Model: 数据定义
   - ViewModel: 业务逻辑和状态管理
   - UI: 渲染和用户交互

5. **响应式编程 (Reactive Programming)**
   - 使用 Kotlin Flow 进行异步操作
   - 使用 StateFlow 管理可观察状态
   - 使用 SharedFlow 处理一次性事件

## 下一步实现

1. 创建 Repository 层
2. 创建 API 接口
3. 创建 Hilt 依赖注入模块
4. 创建 UI 层（Jetpack Compose）
5. 集成第三方库
