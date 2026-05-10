# MobileCodex - AI-Powered Mobile Code Editor

> 一个基于 Kotlin + Jetpack Compose 的 Android 移动端 AI 编程助手

## ✨ 功能特性

### 🤖 AI 对话
- 多会话管理，支持创建/切换/删除对话
- 集成 OpenAI Chat Completions API（兼容任何 OpenAI 格式的 API）
- 智能代码块检测与格式化渲染
- 可自定义系统提示词、模型、温度等参数
- 支持 GPT-4 Turbo、GPT-3.5 Turbo 等模型

### 📦 GitHub 集成
- 浏览个人及组织仓库列表
- 仓库搜索（按名称、描述）
- 递归文件树浏览，支持深层目录导航
- 文件内容查看与在线编辑（Base64 自动解码）
- 多分支支持

### 📝 代码编辑器
- 多文件 Tab 标签页管理
- 等宽字体代码显示
- 修改状态实时追踪（isModified 标记）
- 自动识别 40+ 编程语言
- 行数统计与语言标签

### ⚙️ 设置系统
- AI 服务配置（API Key / Base URL / Model）
- GitHub Personal Access Token 配置
- 编辑器个性化（字体大小 / Tab 宽度）
- 深色 / 浅色主题切换
- 所有设置通过 DataStore 持久化

## 🏗️ 技术架构

| 层级 | 技术栈 |
|------|--------|
| **UI** | Jetpack Compose + Material 3 |
| **架构** | MVVM + Hilt DI |
| **网络** | Retrofit 2 + OkHttp 4 |
| **序列化** | Gson |
| **持久化** | DataStore Preferences |
| **异步** | Kotlin Coroutines + Flow |

## 📁 项目结构

```
app/src/main/java/com/mobilecodex/
├── MainActivity.kt          # 主 Activity（@AndroidEntryPoint）
├── MobileCodexApp.kt        # Application（@HiltAndroidApp）
├── model/                   # 数据模型（13 个）
│   ├── ChatMessage.kt       # 聊天消息
│   ├── ChatConversation.kt  # 对话会话
│   ├── GitHubRepository.kt  # GitHub 仓库
│   ├── GitHubUser.kt        # GitHub 用户
│   ├── GitTreeNode.kt       # Git 树节点 + 内容项
│   ├── FileContent.kt       # 文件内容
│   ├── VirtualFile.kt       # 虚拟文件（编辑用）
│   ├── AISettings.kt        # AI 设置
│   ├── GitHubSettings.kt    # GitHub 设置
│   ├── EditorSettings.kt    # 编辑器设置
│   ├── AppSettings.kt       # 综合设置
│   ├── WorkspaceState.kt    # 工作区状态
│   └── WorkspaceOption.kt   # 导航选项
├── data/
│   ├── api/
│   │   ├── OpenAIApi.kt     # OpenAI API 接口
│   │   └── GitHubApi.kt     # GitHub REST API 接口
│   └── repository/
│       ├── ChatRepository.kt     # 聊天仓库
│       ├── GitHubRepository.kt   # GitHub 仓库
│       └── SettingsRepository.kt # 设置仓库（DataStore）
├── di/
│   ├── NetworkModule.kt     # 网络依赖注入
│   └── GsonModule.kt        # Gson 依赖注入
├── viewmodel/
│   ├── MainViewModel.kt     # 主导航
│   ├── ChatViewModel.kt     # 聊天逻辑
│   ├── GitHubViewModel.kt   # GitHub 浏览逻辑
│   ├── FileViewModel.kt     # 文件编辑逻辑
│   └── SettingsViewModel.kt # 设置管理逻辑
└── ui/
    ├── theme/
    │   └── Theme.kt         # Material 3 主题
    └── screens/
        ├── chat/
        │   └── ChatScreen.kt    # AI 对话界面
        ├── files/
        │   └── FilesScreen.kt   # 仓库浏览 + 文件编辑界面
        └── settings/
            └── SettingsScreen.kt # 设置界面
```

## 🚀 快速开始

### 前置条件
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤

1. 克隆仓库
```bash
git clone <repo-url>
cd MobileCodex
```

2. 用 Android Studio 打开项目

3. 同步 Gradle

4. 配置 API 密钥
   - 打开应用 → 设置
   - 填入 OpenAI API Key（格式：`sk-...`）
   - 填入 GitHub Personal Access Token（需要 `repo` 和 `user` 权限）

5. 运行到设备或模拟器（API 26+）

## 📋 支持的 API 端点

### OpenAI 兼容
- `POST /v1/chat/completions` - Chat Completions
- `GET /v1/models` - 模型列表

兼容任何 OpenAI 格式的 API（如 Azure OpenAI、本地 LLM 等）

### GitHub REST API
- `GET /user` - 用户信息
- `GET /user/repos` - 仓库列表
- `GET /search/repositories` - 仓库搜索
- `GET /repos/{owner}/{repo}/contents/{path}` - 文件/目录内容
- `GET /repos/{owner}/{repo}/git/trees/{sha}` - Git 树

## 📄 License

MIT
