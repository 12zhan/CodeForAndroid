@file:OptIn(ExperimentalMaterial3Api::class)

package com.mobilecodex.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilecodex.model.ChatMessage
import com.mobilecodex.model.MessageRole
import com.mobilecodex.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    text = uiState.activeConversation?.title ?: "AI 对话",
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            actions = {
                if (uiState.activeConversation != null) {
                    IconButton(onClick = {
                        viewModel.createNewConversation()
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "新建对话")
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "设置")
                }
            }
        )

        // 侧边对话列表和主聊天区域
        Row(modifier = Modifier.fillMaxSize()) {
            // 对话列表侧边栏
            ConversationSidebar(
                conversations = uiState.conversations,
                activeId = uiState.activeConversation?.id,
                onSelect = { viewModel.selectConversation(it) },
                onDelete = { viewModel.deleteConversation(it) },
                modifier = Modifier
                    .fillMaxHeight()
                    .width(260.dp)
            )

            // 聊天主区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 错误提示
                if (uiState.error != null) {
                    ErrorBanner(
                        message = uiState.error!!,
                        onDismiss = { viewModel.clearError() }
                    )
                }

                // 消息列表
                if (uiState.activeConversation != null) {
                    ChatMessageList(
                        messages = uiState.activeConversation!!.messages,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    EmptyChatPlaceholder(
                        onCreateNew = { viewModel.createNewConversation() },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 输入栏
                ChatInputBar(
                    text = uiState.inputText,
                    onTextChange = { viewModel.updateInput(it) },
                    onSend = { viewModel.sendMessage() },
                    onStop = { viewModel.stopStreaming() },
                    isSending = uiState.isSending,
                    isStreaming = uiState.isStreaming
                )
            }
        }
    }
}

@Composable
private fun ConversationSidebar(
    conversations: List<com.mobilecodex.model.ChatConversation>,
    activeId: String?,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(conversations, key = { it.id }) { conv ->
                ConversationItem(
                    conversation = conv,
                    isActive = conv.id == activeId,
                    onClick = { onSelect(conv.id) },
                    onDelete = { onDelete(conv.id) }
                )
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: com.mobilecodex.model.ChatConversation,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = conversation.previewText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            IconButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除对话",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除对话") },
            text = { Text("确定要删除「${conversation.title}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ChatMessageList(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val messagesToShow = messages.filter { it.role != MessageRole.SYSTEM }

    LaunchedEffect(messages.size) {
        if (messagesToShow.isNotEmpty()) {
            listState.animateScrollToItem(messagesToShow.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(messagesToShow, key = { it.id }) { message ->
            MessageBubble(message = message)
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // 角色标签
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Icon(
                    Icons.Filled.Android,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI 助手",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                Text(
                    text = "你",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 消息气泡
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // 代码块检测（含流式光标）
                val parts = parseMessageContent(message.content, message.isStreaming)
                parts.forEach { part ->
                    when (part) {
                        is MessagePart.Code -> {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = part.content,
                                    modifier = Modifier.padding(8.dp),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = textColor
                                )
                            }
                        }
                        is MessagePart.Text -> {
                            Text(
                                text = part.content,
                                color = textColor,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                        is MessagePart.Cursor -> {
                            // 流式闪烁光标
                            Text(
                                text = "▌",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // 时间
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(message.timestamp)),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatPlaceholder(
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Chat,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "开始新的 AI 对话",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
        Text(
            text = "与 AI 助手讨论代码、调试问题或获取建议",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateNew,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("新建对话")
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isSending: Boolean,
    isStreaming: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (isStreaming) "AI 正在回复..." else "输入消息...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                enabled = !isSending,
                maxLines = 5,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (isStreaming) {
                // 停止按钮
                FilledIconButton(
                    onClick = onStop,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "停止",
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                // 发送按钮
                FilledIconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isSending,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "发送",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "关闭",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// --- 消息解析辅助 ---

private sealed class MessagePart {
    data class Text(val content: String) : MessagePart()
    data class Code(val content: String) : MessagePart()
    data object Cursor : MessagePart()
}

private fun parseMessageContent(content: String, isStreaming: Boolean = false): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    val regex = Regex("```(?:\\w+)?\\n?([\\s\\S]*?)```")
    var lastIndex = 0

    regex.findAll(content).forEach { match ->
        // 代码块之前的文本
        if (match.range.first > lastIndex) {
            val text = content.substring(lastIndex, match.range.first).trim()
            if (text.isNotEmpty()) {
                parts.add(MessagePart.Text(text))
            }
        }
        // 代码块
        val code = match.groupValues[1].trim()
        if (code.isNotEmpty()) {
            parts.add(MessagePart.Code(code))
        }
        lastIndex = match.range.last + 1
    }

    // 剩余文本
    if (lastIndex < content.length) {
        val text = content.substring(lastIndex).trim()
        if (text.isNotEmpty()) {
            parts.add(MessagePart.Text(text))
        }
    }

    // 流式光标
    if (isStreaming) {
        parts.add(MessagePart.Cursor)
    }

    return parts.ifEmpty {
        if (isStreaming) listOf(MessagePart.Cursor) else listOf(MessagePart.Text(content))
    }
}
