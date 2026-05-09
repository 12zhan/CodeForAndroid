# Mobile Codex - 数据模型与 ViewModel 架构文档

## 项目结构

```
app/src/main/java/com/mobilecodex/
├── model/                          # 数据模型层
│   ├── AISettings.kt              # AI 服务配置
│   ├── AppSettings.kt             # 应用全局设置
│   ├── ChatConversation.kt        # 聊天对话
│   ├── ChatMessage.kt             # 聊天消息
│   ├── FileContent.kt             # GitHub 文件内容
│   ├── GitHubRepository.kt        # GitHub 仓库
│   ├── GitHubSettings.kt          # GitHub 配置
│   ├── GitHubUser.kt              # GitHub 用户
│   ├── GitTreeNode.kt             # Git 树节点
│   ├── VirtualFile.kt             # 虚拟文件系统文件
│   ├── WorkspaceOption.kt         # 工作区选项
│   └── WorkspaceState.kt          # 工作区状态
└── viewmodel/                      # ViewModel 层
    ├── MainViewModel.kt           # 主 ViewModel（全局状态）
    ├── GitHubViewModel.kt         # GitHub 操作
    ├── FileViewModel.kt           # 文件管理
    ├── ChatViewModel.kt           # AI 对话
    └── SettingsViewModel.kt       # 设置管理
```

## 数据模型详解

### 1. GitHub 相关模型

#### GitHubRepository
```kotlin
data class GitHubRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val owner: GitHubUser,
    val description: String?,
    val defaultBranch: String,
    val isPrivate: Boolean,
    // ... 更多字段
)
```
- 对应 GitHub API 的 Repository 对象
- 包含 `BINARY_EXTENSIONS` 常量，用于过滤二进制文件

#### GitHubUser
```kotlin
data class GitHubUser(
    val login: String,
    val id: Long,
    val avatarUrl: String,
    val name: String?,
    // ... 更多字段
)
```
- 提供 `displayName` 属性（优先使用真实姓名）
- 提供 `isOrganization` 属性判断账户类型

#### GitTreeNode
```kotlin
data class GitTreeNode(
    val path: String,
    val mode: String,
    val type: String,  // "blob" 或 "tree"
    val sha: String,
    // ... 更多字段
)
```
- `isFile` / `isDirectory` 判断节点类型
- `isTextFile` 基于扩展名判断是否为文本文件
- `parentPath` 获取父目录路径

#### FileContent
```kotlin
data class FileContent(
    val name: String,
    val path: String,
    val content: String?,
    val encoding: String?,
    // ... 更多字段
)
```
- `decodedContent()` 方法解码 Base64 内容

### 2. 虚拟文件系统（VFS）

#### VirtualFile
```kotlin
data class VirtualFile(
    val path: String,
    val content: String? = null,
    val isModified: Boolean = false,
    val isLoaded: Boolean = false,
    val sha: String? = null,
    // ... 更多字段
)
```
- `withContent()` - 创建已加载内容的副本
- `withModification()` - 创建已修改的副本
- `resetModification()` - 重置修改状态（保存后调用）

#### WorkspaceState
```kotlin
data class WorkspaceState(
    val currentWorkspace: WorkspaceOption? = null,
    val fileTree: List<GitTreeNode> = emptyList(),
    val virtualFiles: Map<String, VirtualFile> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```
- `modifiedFiles` - 获取已修改的文件列表
- `hasUnsavedChanges` - 判断是否有未保存的修改
- `withFile()` - 添加或更新虚拟文件
- `withWorkspace()` - 切换工作区

### 3. 聊天模型

#### ChatMessage
```kotlin
data class ChatMessage(
    val id: String,
    val role: MessageRole,  // USER, ASSISTANT, SYSTEM, FUNCTION
    val content: String,
    val timestamp: Long,
    val attachedFiles: List<String> = emptyList(),
    val functionName: String? = null,
    val functionCallId: String? = null,
    val isStreaming: Boolean = false
)
```
- 工厂方法：`user()`, `assistant()`, `system()`, `functionCall()`
- `withStreamingContent()` - 流式传输更新
- `finishStreaming()` - 完成流式传输

#### ChatConversation
```kotlin
data class ChatConversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val workspaceId: String? = null
)
```
- `withMessage()` - 添加消息（自动更新标题）
- `updateLastMessage()` - 更新最后一条消息（用于流式传输）
- `clearMessages()` - 清除所有消息

### 4. 设置模型

#### AISettings
```kotlin
data class AISettings(
    val provider: AIProvider = AIProvider.OPENAI,
    val apiKey: String = "",
    val endpoint: String,
    val model: String = "gpt-4",
    val systemPrompt: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val streamResponse: Boolean = true,
    val enableFunctionCalling: Boolean = true
)
```
- 支持多种 AI 提供商：OpenAI, Anthropic, Google, Azure, 自定义
- `availableModels` - 获取提供商支持的模型列表
- 提供不可变更新方法（`withXxx()`）

#### GitHubSettings
```kotlin
data class GitHubSettings(
    val token: String = "",
    val username: String = "",
    val defaultBranch: String = "main",
    val apiBaseUrl: String = "https://api.github.com",
    val pageSize: Int = 30,
    val includePrivateRepos: Boolean = true,
    val includeForkedRepos: Boolean = true
)
```
- `buildReposEndpoint()` - 构建仓库列表 API 端点
- `buildTreeEndpoint()` - 构建文件树 API 端点
- `buildContentEndpoint()` - 构建文件内容 API 端点
- `buildCreateBlobEndpoint()` - 构建创建 Blob API 端点
- `buildCreateCommitEndpoint()` - 构建创建 Commit API 端点

#### AppSettings
```kotlin
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val editorSettings: EditorSettings = EditorSettings.default(),
    val autoSave: Boolean = true,
    val autoSaveInterval: Long = 30_000,
    val showNotifications: Boolean = true,
    val hapticFeedback: Boolean = true,
    val enableAnimations: Boolean = true,
    val compactMode: Boolean = false
)
```
- 包含 `EditorSettings` 子配置
- 支持深色/浅色/跟随系统主题

#### WorkspaceOption
```kotlin
data class WorkspaceOption(
    val type: WorkspaceType,  // LOCAL 或 GITHUB
    val name: String,
    val id: String,
    val repository: GitHubRepository? = null,
    val branch: String? = null
)
```
- `displayName` - 获取显示名称（包含 emoji 图标）
- `fromRepository()` - 从 GitHub 仓库创建工作区选项

## ViewModel 架构详解

### 1. MainViewModel - 全局状态管理

**职责**：
- 管理应用的全局状态
- 协调不同模块之间的交互
- 处理工作区切换
- 管理对话和文件操作

**核心状态**：
```kotlin
data class AppUiState(
    val workspaceState: WorkspaceState,
    val workspaceOptions: List<WorkspaceOption>,
    val currentConversation: ChatConversation,
    val conversations: List<ChatConversation>,
    val aiSettings: AISettings,
    val githubSettings: GitHubSettings,
    val appSettings: AppSettings,
    val repositories: List<GitHubRepository>,
    val isLoading: Boolean,
    val error: String?
)
```

**核心方法**：
- `switchWorkspace()` - 切换工作区
- `fetchRepositories()` - 获取 GitHub 仓库列表
- `loadFileContent()` - 懒加载文件内容
- `saveFileContent()` - 保存文件到 VFS
- `commitChanges()` - 提交更改到 GitHub
- `createNewConversation()` - 创建新对话

### 2. GitHubViewModel - GitHub 操作

**职责**：
- 管理 GitHub API 调用
- 处理仓库列表、文件树、文件内容
- 实现 Git 提交流程

**核心状态**：
```kotlin
data class GitHubUiState(
    val repositories: List<GitHubRepository>,
    val currentUser: GitHubUser?,
    val selectedRepository: GitHubRepository?,
    val fileTree: List<GitTreeNode>,
    val isLoadingRepos: Boolean,
    val isLoadingTree: Boolean,
    val page: Int,
    val hasMore: Boolean
)
```

**核心方法**：
- `fetchCurrentUser()` - 获取当前用户信息
- `fetchRepositories()` - 获取仓库列表（支持分页）
- `loadRepositoryTree()` - 加载仓库文件树
- `getFileContent()` - 获取文件内容
- `commitChanges()` - 提交文件更改（完整 Git 流程）

**Git 提交流程**：
1. 为每个修改的文件创建 Blob
2. 创建新的 Tree
3. 创建 Commit
4. 更新 Ref

### 3. FileViewModel - 文件管理

**职责**：
- 管理文件树浏览
- 处理文件内容编辑
- 维护 VFS 状态

**核心状态**：
```kotlin
data class FileUiState(
    val fileTree: List<GitTreeNode>,
    val virtualFiles: Map<String, VirtualFile>,
    val selectedFile: VirtualFile?,
    val expandedFolders: Set<String>,
    val isLoadingFile: Boolean,
    val searchQuery: String,
    val searchResults: List<GitTreeNode>,
    val isEditing: Boolean,
    val editContent: String
)
```

**核心方法**：
- `loadFileTree()` - 加载文件树
- `selectFile()` - 选择文件（自动懒加载）
- `setFileContent()` - 设置文件内容
- `startEditing()` - 开始编辑
- `saveFile()` - 保存文件到 VFS
- `revertEdit()` - 撤销编辑
- `toggleFolder()` - 展开/折叠文件夹
- `searchFiles()` - 搜索文件
- `getFileTreeHierarchy()` - 获取层级文件树

### 4. ChatViewModel - AI 对话

**职责**：
- 管理 AI 对话
- 处理消息发送和接收
- 实现 Function Calling

**核心状态**：
```kotlin
data class ChatUiState(
    val conversation: ChatConversation,
    val conversations: List<ChatConversation>,
    val inputText: String,
    val attachedFiles: List<String>,
    val isGenerating: Boolean,
    val isStreaming: Boolean,
    val streamingContent: String
)
```

**核心方法**：
- `sendMessage()` - 发送消息
- `updateInputText()` - 更新输入文本
- `attachFile()` / `detachFile()` - 附加/移除文件
- `handleFunctionResult()` - 处理 Function Calling 结果
- `getAvailableTools()` - 获取可用的 Function Calling 工具
- `createNewConversation()` - 创建新对话
- `stopGenerating()` - 停止生成
- `regenerateLastResponse()` - 重新生成最后一条响应

**Function Calling 工具**：
- `listFiles` - 获取当前工作区中的文件列表
- `readFile` - 读取指定路径的文件内容
- `saveFile` - 保存或创建文件
- `githubCommitChanges` - 将修改提交到 GitHub 仓库

### 5. SettingsViewModel - 设置管理

**职责**：
- 管理所有配置项
- 测试连接
- 处理数据清除

**核心状态**：
```kotlin
data class SettingsUiState(
    val aiSettings: AISettings,
    val githubSettings: GitHubSettings,
    val appSettings: AppSettings,
    val githubUser: GitHubUser?,
    val isTestingConnection: Boolean,
    val connectionTestResult: ConnectionTestResult?,
    val showClearChatDialog: Boolean,
    val showClearWorkspaceDialog: Boolean,
    val showClearAllDialog: Boolean
)
```

**核心方法**：
- `updateAIProvider()` - 更新 AI 提供商
- `updateAIApiKey()` - 更新 AI API Key
- `updateGitHubToken()` - 更新 GitHub Token
- `testAIConnection()` - 测试 AI 连接
- `testGitHubConnection()` - 测试 GitHub 连接
- `saveAISettings()` / `saveGitHubSettings()` / `saveAppSettings()` - 保存设置
- `confirmClearChatHistory()` - 确认清除聊天历史
- `confirmClearWorkspace()` - 确认清除工作区
- `confirmClearAllData()` - 确认清除所有数据

## 数据流架构

```
┌─────────────────────────────────────────────────────────────────┐
│                          UI Layer                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  Chat UI │  │ Files UI │  │Settings UI│  │  Main UI │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │              │
├───────┼──────────────┼──────────────┼──────────────┼─────────────┤
│       │              │              │              │              │
│  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐       │
│  │ChatVM    │  │FileVM    │  │SettingsVM│  │MainVM    │       │
│  │          │  │          │  │          │  │          │       │
│  │ StateFlow│  │ StateFlow│  │ StateFlow│  │ StateFlow│       │
│  │ Events   │  │ Events   │  │ Events   │  │ Events   │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │              │
├───────┼──────────────┼──────────────┼──────────────┼─────────────┤
│       │              │              │              │              │
│  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐       │
│  │AIRepo    │  │FileRepo  │  │Settings  │  │GitHubRepo│       │
│  │          │  │          │  │Repo      │  │          │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │              │
├───────┼──────────────┼──────────────┼──────────────┼─────────────┤
│       │              │              │              │              │
│  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐       │
│  │OpenAI API│  │VFS/Local │  │DataStore │  │GitHub API│       │
│  │          │  │Storage   │  │          │  │          │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘       │
│                                                                  │
│                      Data Layer                                  │
└─────────────────────────────────────────────────────────────────┘
```

## 使用示例

### 在 Compose UI 中使用 ViewModel

```kotlin
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    
    // 监听事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChatEvent.ShowError -> {
                    // 显示错误 Snackbar
                }
                is ChatEvent.ShowMessage -> {
                    // 显示消息 Snackbar
                }
                is ChatEvent.FunctionCallRequest -> {
                    // 处理 Function Calling
                    handleFunctionCall(event.name, event.arguments, event.callId)
                }
                // ...
            }
        }
    }
    
    Column {
        // 消息列表
        LazyColumn {
            items(messages) { message ->
                MessageBubble(message = message)
            }
        }
        
        // 输入框
        ChatInput(
            text = uiState.inputText,
            onTextChange = viewModel::updateInputText,
            onSend = viewModel::sendMessage,
            isGenerating = isGenerating,
            onStop = viewModel::stopGenerating
        )
    }
}

@Composable
fun FilesScreen(
    viewModel: FileViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val fileTree by viewModel.fileTree.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    
    Row {
        // 文件树
        FileTree(
            tree = viewModel.getFileTreeHierarchy(),
            onFileClick = viewModel::selectFile,
            onFolderToggle = viewModel::toggleFolder
        )
        
        // 文件编辑器
        selectedFile?.let { file ->
            FileEditor(
                file = file,
                isEditing = isEditing,
                editContent = uiState.editContent,
                onEditContentChange = viewModel::updateEditContent,
                onStartEdit = viewModel::startEditing,
                onSave = viewModel::saveFile,
                onRevert = viewModel::revertEdit
            )
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val aiSettings by viewModel.aiSettings.collectAsState()
    val githubSettings by viewModel.githubSettings.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    
    LazyColumn {
        // AI 设置
        item {
            AISettingsSection(
                settings = aiSettings,
                onProviderChange = viewModel::updateAIProvider,
                onApiKeyChange = viewModel::updateAIApiKey,
                onModelChange = viewModel::updateAIModel,
                onTestConnection = viewModel::testAIConnection
            )
        }
        
        // GitHub 设置
        item {
            GitHubSettingsSection(
                settings = githubSettings,
                onTokenChange = viewModel::updateGitHubToken,
                onTestConnection = viewModel::testGitHubConnection
            )
        }
        
        // 应用设置
        item {
            AppSettingsSection(
                settings = appSettings,
                onThemeChange = viewModel::updateThemeMode,
                onLanguageChange = viewModel::updateLanguage
            )
        }
        
        // 数据操作
        item {
            DataOperationsSection(
                onClearChat = viewModel::showClearChatDialog,
                onClearWorkspace = viewModel::showClearWorkspaceDialog,
                onClearAll = viewModel::showClearAllDialog
            )
        }
    }
}
```

### Hilt 依赖注入配置

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideGitHubRepository(
        api: GitHubApi,
        settingsRepository: SettingsRepository
    ): GitHubRepository {
        return GitHubRepositoryImpl(api, settingsRepository)
    }
    
    @Provides
    @Singleton
    fun provideChatRepository(
        aiApi: AIApi,
        settingsRepository: SettingsRepository
    ): ChatRepository {
        return ChatRepositoryImpl(aiApi, settingsRepository)
    }
    
    // ... 更多依赖
}
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

1. **创建 Repository 层**
   - `GitHubRepository` - 封装 GitHub API 调用
   - `ChatRepository` - 封装 AI 对话逻辑
   - `FileRepository` - 封装文件系统操作
   - `SettingsRepository` - 封装 DataStore 操作

2. **创建 API 接口**
   - `GitHubApi` - Retrofit 接口
   - `OpenAIApi` - OpenAI API 接口

3. **创建 UI 层**
   - Jetpack Compose 组件
   - Material Design 3 主题
   - 导航组件

4. **集成第三方库**
   - Retrofit + OkHttp
   - Hilt
   - DataStore
   - compose-markdown (Markdown 渲染)
   - CodeView (代码高亮)

## 依赖项

```kotlin
// build.gradle.kts (app)
dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    
    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Markdown (可选)
    // implementation("com.github.jeziellandro:compose-markdown:0.1.0")
    
    // Code Editor (可选)
    // implementation("com.github.AhmedBafworker:CodeView:1.0.4")
}
```
