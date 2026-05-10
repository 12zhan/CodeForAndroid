# MobileCodex 开发计划

## ✅ 已完成

### Phase 1 - 项目基础 (100%)
- [x] Gradle 构建配置（Kotlin DSL）
- [x] Hilt 依赖注入配置
- [x] AndroidManifest 配置
- [x] Material 3 主题
- [x] 项目目录结构

### Phase 2 - 数据层 (100%)
- [x] 13 个数据模型
- [x] OpenAI API 接口定义
- [x] GitHub API 接口定义
- [x] 3 个 Repository 实现
- [x] DataStore 设置持久化

### Phase 3 - 业务逻辑 (100%)
- [x] ChatViewModel（多会话管理）
- [x] GitHubViewModel（仓库浏览）
- [x] FileViewModel（文件编辑）
- [x] SettingsViewModel（设置管理）
- [x] MainViewModel（导航控制）

### Phase 4 - UI 界面 (100%)
- [x] ChatScreen（AI 对话界面）
- [x] FilesScreen（仓库 + 编辑器界面）
- [x] SettingsScreen（设置界面）
- [x] 侧边栏导航
- [x] 深色/浅色主题

---

## 🔜 待开发

### Phase 5 - 增强功能
- [ ] 流式响应（SSE/Stream）
- [ ] 语法高亮（集成 CodeMirror/Highlight.js）
- [ ] 文件保存/提交到 GitHub
- [ ] 代码差异对比（Diff View）
- [ ] 本地 SQLite 缓存（Room）
- [ ] 离线模式

### Phase 6 - 高级功能
- [ ] 多平台支持（KMP）
- [ ] Git 操作（commit, push, pull, branch）
- [ ] PR 查看与管理
- [ ] Issues 管理
- [ ] CI/CD 状态查看
- [ ] 代码审查（Code Review）
- [ ] WebView 预览

### Phase 7 - 优化
- [ ] 单元测试覆盖
- [ ] UI 测试
- [ ] 性能优化
- [ ] 无障碍支持
- [ ] 国际化（多语言）
- [ ] Google Play 发布

---

## 📊 当前状态

| 模块 | 进度 | 说明 |
|------|------|------|
| 项目配置 | 100% | Gradle + Manifest |
| 数据模型 | 100% | 13 个 Model |
| API 层 | 100% | OpenAI + GitHub |
| Repository | 100% | Chat + GitHub + Settings |
| ViewModel | 100% | 5 个 ViewModel |
| UI | 100% | 3 个 Screen + Theme |
| 入口 | 100% | App + Activity |
| **总体** | **~70%** | 核心功能完成，增强功能待开发 |
