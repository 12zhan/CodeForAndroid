package com.mobilecodex.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilecodex.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 保存成功提示
            if (uiState.saveSuccess) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "设置已保存",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 错误提示
            if (uiState.error != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // ---- AI 设置 ----
            SettingsSection(title = "AI 服务", icon = Icons.Filled.Android) {
                SettingsTextField(
                    label = "API Key",
                    value = uiState.aiApiKeyInput,
                    onValueChange = { viewModel.updateAiApiKey(it) },
                    placeholder = "sk-...",
                    isPassword = true,
                    helperText = "OpenAI 兼容 API 密钥"
                )

                SettingsTextField(
                    label = "Base URL",
                    value = uiState.aiBaseUrlInput,
                    onValueChange = { viewModel.updateAiBaseUrl(it) },
                    placeholder = "https://api.openai.com/v1",
                    helperText = "API 端点地址（支持兼容接口）"
                )

                SettingsTextField(
                    label = "模型",
                    value = uiState.aiModelInput,
                    onValueChange = { viewModel.updateAiModel(it) },
                    placeholder = "gpt-4-turbo",
                    helperText = "模型 ID，如 gpt-4-turbo, gpt-3.5-turbo"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsTextField(
                        label = "Max Tokens",
                        value = uiState.aiMaxTokensInput,
                        onValueChange = { viewModel.updateAiMaxTokens(it) },
                        modifier = Modifier.weight(1f),
                        helperText = "最大返回令牌数"
                    )

                    SettingsTextField(
                        label = "Temperature",
                        value = uiState.aiTemperatureInput,
                        onValueChange = { viewModel.updateAiTemperature(it) },
                        modifier = Modifier.weight(1f),
                        helperText = "0.0 ~ 2.0"
                    )
                }

                SettingsTextField(
                    label = "系统提示词",
                    value = uiState.aiSystemPromptInput,
                    onValueChange = { viewModel.updateAiSystemPrompt(it) },
                    maxLines = 3,
                    helperText = "定义 AI 助手的行为和风格"
                )

                Button(
                    onClick = {
                        viewModel.saveAISettings()
                        viewModel.clearSaveSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("保存 AI 设置")
                }
            }

            // ---- GitHub 设置 ----
            SettingsSection(title = "GitHub", icon = Icons.Filled.Code) {
                SettingsTextField(
                    label = "Personal Access Token",
                    value = uiState.ghTokenInput,
                    onValueChange = { viewModel.updateGhToken(it) },
                    isPassword = true,
                    placeholder = "ghp_...",
                    helperText = "需要 repo 和 user 权限"
                )

                Button(
                    onClick = {
                        viewModel.saveGitHubSettings()
                        viewModel.clearSaveSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("保存 GitHub 设置")
                }
            }

            // ---- 编辑器设置 ----
            SettingsSection(title = "编辑器", icon = Icons.Filled.Edit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SettingsTextField(
                        label = "字体大小",
                        value = uiState.editorFontSizeInput,
                        onValueChange = { viewModel.updateEditorFontSize(it) },
                        modifier = Modifier.weight(1f)
                    )
                    SettingsTextField(
                        label = "Tab 宽度",
                        value = uiState.editorTabSizeInput,
                        onValueChange = { viewModel.updateEditorTabSize(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        viewModel.saveEditorSettings()
                        viewModel.clearSaveSuccess()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("保存编辑器设置")
                }
            }

            // ---- 外观 ----
            SettingsSection(title = "外观", icon = Icons.Filled.Palette) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("深色主题", fontSize = 15.sp)
                    Switch(
                        checked = uiState.isDarkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme() }
                    )
                }
            }

            // 底部间距
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    maxLines: Int = 1,
    helperText: String? = null
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, fontSize = 14.sp) },
            singleLine = maxLines == 1,
            maxLines = maxLines,
            visualTransformation = if (isPassword && !showPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = if (showPassword) "隐藏" else "显示",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )

        if (helperText != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = helperText,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
