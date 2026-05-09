package com.mobilecodex.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mobilecodex.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var aiSettings by remember { mutableStateOf(AISettings.default()) }
    var githubSettings by remember { mutableStateOf(GitHubSettings.default()) }
    var appSettings by remember { mutableStateOf(AppSettings.default()) }

    // 对话框状态
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showClearWorkspaceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==================== AI 设置 ====================
        item {
            SectionHeader(
                icon = Icons.Default.SmartToy,
                title = "AI 设置"
            )
        }

        item {
            AISettingsCard(
                settings = aiSettings,
                onSettingsChange = { aiSettings = it }
            )
        }

        // ==================== GitHub 设置 ====================
        item {
            SectionHeader(
                icon = Icons.Default.Code,
                title = "GitHub 设置"
            )
        }

        item {
            GitHubSettingsCard(
                settings = githubSettings,
                onSettingsChange = { githubSettings = it }
            )
        }

        // ==================== 外观设置 ====================
        item {
            SectionHeader(
                icon = Icons.Default.Palette,
                title = "外观设置"
            )
        }

        item {
            AppearanceSettingsCard(
                settings = appSettings,
                onSettingsChange = { appSettings = it }
            )
        }

        // ==================== 编辑器设置 ====================
        item {
            SectionHeader(
                icon = Icons.Default.Code,
                title = "编辑器设置"
            )
        }

        item {
            EditorSettingsCard(
                settings = appSettings.editorSettings,
                onSettingsChange = { editorSettings ->
                    appSettings = appSettings.withEditorSettings(editorSettings)
                }
            )
        }

        // ==================== 数据操作 ====================
        item {
            SectionHeader(
                icon = Icons.Default.Storage,
                title = "数据操作"
            )
        }

        item {
            DataOperationsCard(
                onClearChat = { showClearChatDialog = true },
                onClearWorkspace = { showClearWorkspaceDialog = true }
            )
        }

        // 底部间距
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // 清除聊天记录确认对话框
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("清除聊天记录") },
            text = { Text("确定要清除所有聊天记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { showClearChatDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 清除工作区确认对话框
    if (showClearWorkspaceDialog) {
        AlertDialog(
            onDismissRequest = { showClearWorkspaceDialog = false },
            icon = { Icon(Icons.Default.FolderDelete, contentDescription = null) },
            title = { Text("清除本地工作区") },
            text = { Text("确定要清除本地工作区的所有文件缓存吗？") },
            confirmButton = {
                TextButton(
                    onClick = { showClearWorkspaceDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearWorkspaceDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ==================== 组件 ====================

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AISettingsCard(
    settings: AISettings,
    onSettingsChange: (AISettings) -> Unit
) {
    var showApiKey by remember { mutableStateOf(false) }
    var expandedProvider by remember { mutableStateOf(false) }
    var expandedModel by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // AI 提供商选择
            ExposedDropdownMenuBox(
                expanded = expandedProvider,
                onExpandedChange = { expandedProvider = it }
            ) {
                OutlinedTextField(
                    value = settings.provider.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("AI 提供商") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProvider) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedProvider,
                    onDismissRequest = { expandedProvider = false }
                ) {
                    AIProvider.entries.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.displayName) },
                            onClick = {
                                onSettingsChange(settings.withProvider(provider))
                                expandedProvider = false
                            }
                        )
                    }
                }
            }

            // API Key
            OutlinedTextField(
                value = settings.apiKey,
                onValueChange = { onSettingsChange(settings.withApiKey(it)) },
                label = { Text("API Key") },
                placeholder = { Text("sk-...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            imageVector = if (showApiKey) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (showApiKey) "隐藏" else "显示"
                        )
                    }
                }
            )

            // Endpoint（仅自定义提供商时显示）
            if (settings.provider == AIProvider.CUSTOM) {
                OutlinedTextField(
                    value = settings.endpoint,
                    onValueChange = { onSettingsChange(settings.copy(endpoint = it)) },
                    label = { Text("API Endpoint") },
                    placeholder = { Text("https://api.example.com/v1") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 模型选择
            val availableModels = settings.provider.availableModels
            if (availableModels.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expandedModel,
                    onExpandedChange = { expandedModel = it }
                ) {
                    OutlinedTextField(
                        value = settings.model,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("模型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedModel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedModel,
                        onDismissRequest = { expandedModel = false }
                    ) {
                        availableModels.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    onSettingsChange(settings.withModel(model))
                                    expandedModel = false
                                }
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = settings.model,
                    onValueChange = { onSettingsChange(settings.withModel(it)) },
                    label = { Text("模型名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 温度滑块
            Column {
                Text(
                    text = "温度: ${"%.1f".format(settings.temperature)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.temperature,
                    onValueChange = { onSettingsChange(settings.withTemperature(it)) },
                    valueRange = 0f..2f,
                    steps = 19
                )
            }

            // 流式响应开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("流式响应", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "实时显示 AI 回复",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = settings.streamResponse,
                    onCheckedChange = { onSettingsChange(settings.copy(streamResponse = it)) }
                )
            }

            // Function Calling 开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Function Calling", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "允许 AI 调用工具函数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = settings.enableFunctionCalling,
                    onCheckedChange = { onSettingsChange(settings.copy(enableFunctionCalling = it)) }
                )
            }

            // 系统提示词
            var expandedSystemPrompt by remember { mutableStateOf(false) }
            TextButton(onClick = { expandedSystemPrompt = !expandedSystemPrompt }) {
                Text("系统提示词")
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expandedSystemPrompt) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expandedSystemPrompt) {
                OutlinedTextField(
                    value = settings.systemPrompt,
                    onValueChange = { onSettingsChange(settings.withSystemPrompt(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 200.dp),
                    label = { Text("System Prompt") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun GitHubSettingsCard(
    settings: GitHubSettings,
    onSettingsChange: (GitHubSettings) -> Unit
) {
    var showToken by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Token
            OutlinedTextField(
                value = settings.token,
                onValueChange = { onSettingsChange(settings.withToken(it)) },
                label = { Text("Personal Access Token") },
                placeholder = { Text("ghp_...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = if (showToken) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showToken = !showToken }) {
                        Icon(
                            imageVector = if (showToken) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (showToken) "隐藏" else "显示"
                        )
                    }
                }
            )

            // 默认分支
            OutlinedTextField(
                value = settings.defaultBranch,
                onValueChange = { onSettingsChange(settings.withDefaultBranch(it)) },
                label = { Text("默认分支") },
                placeholder = { Text("main") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // 包含私有仓库
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("显示私有仓库", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "在仓库列表中包含私有仓库",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = settings.includePrivateRepos,
                    onCheckedChange = { onSettingsChange(settings.withIncludePrivateRepos(it)) }
                )
            }

            // 包含 Fork 仓库
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("显示 Fork 仓库", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "在仓库列表中包含 Fork 的仓库",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = settings.includeForkedRepos,
                    onCheckedChange = { onSettingsChange(settings.withIncludeForkedRepos(it)) }
                )
            }

            // 测试连接按钮
            Button(
                onClick = { /* TODO: 测试 GitHub 连接 */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = settings.isConfigured
            ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("测试连接")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppearanceSettingsCard(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit
) {
    var expandedTheme by remember { mutableStateOf(false) }
    var expandedLanguage by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 主题选择
            ExposedDropdownMenuBox(
                expanded = expandedTheme,
                onExpandedChange = { expandedTheme = it }
            ) {
                OutlinedTextField(
                    value = settings.themeMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("主题模式") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTheme) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedTheme,
                    onDismissRequest = { expandedTheme = false }
                ) {
                    ThemeMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName) },
                            onClick = {
                                onSettingsChange(settings.withThemeMode(mode))
                                expandedTheme = false
                            }
                        )
                    }
                }
            }

            // 语言选择
            ExposedDropdownMenuBox(
                expanded = expandedLanguage,
                onExpandedChange = { expandedLanguage = it }
            ) {
                OutlinedTextField(
                    value = settings.language.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("语言") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedLanguage,
                    onDismissRequest = { expandedLanguage = false }
                ) {
                    AppLanguage.entries.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.displayName) },
                            onClick = {
                                onSettingsChange(settings.withLanguage(language))
                                expandedLanguage = false
                            }
                        )
                    }
                }
            }

            // 动画开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("动画效果", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = settings.enableAnimations,
                    onCheckedChange = { onSettingsChange(settings.withEnableAnimations(it)) }
                )
            }

            // 紧凑模式
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("紧凑模式", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "减小间距和元素大小",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = settings.compactMode,
                    onCheckedChange = { onSettingsChange(settings.withCompactMode(it)) }
                )
            }
        }
    }
}

@Composable
private fun EditorSettingsCard(
    settings: EditorSettings,
    onSettingsChange: (EditorSettings) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 字体大小
            Column {
                Text(
                    text = "字体大小: ${settings.fontSize}sp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.fontSize.toFloat(),
                    onValueChange = { onSettingsChange(settings.withFontSize(it.toInt())) },
                    valueRange = 10f..24f,
                    steps = 13
                )
            }

            // Tab 大小
            Column {
                Text(
                    text = "Tab 大小: ${settings.tabSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = settings.tabSize.toFloat(),
                    onValueChange = { onSettingsChange(settings.withTabSize(it.toInt())) },
                    valueRange = 2f..8f,
                    steps = 5
                )
            }

            // 显示行号
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("显示行号", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = settings.showLineNumbers,
                    onCheckedChange = { onSettingsChange(settings.withShowLineNumbers(it)) }
                )
            }

            // 自动换行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("自动换行", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = settings.wordWrap,
                    onCheckedChange = { onSettingsChange(settings.withWordWrap(it)) }
                )
            }

            // 语法高亮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("语法高亮", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = settings.syntaxHighlighting,
                    onCheckedChange = { onSettingsChange(settings.withSyntaxHighlighting(it)) }
                )
            }
        }
    }
}

@Composable
private fun DataOperationsCard(
    onClearChat: () -> Unit,
    onClearWorkspace: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onClearChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("清除聊天记录")
            }

            OutlinedButton(
                onClick = onClearWorkspace,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.FolderDelete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("清除本地工作区")
            }
        }
    }
}
