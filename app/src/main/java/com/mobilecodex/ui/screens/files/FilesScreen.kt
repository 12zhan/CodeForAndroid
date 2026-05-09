package com.mobilecodex.ui.screens.files

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilecodex.model.GitTreeNode

/**
 * 文件树节点数据（UI 层用）
 */
data class FileTreeItem(
    val node: GitTreeNode,
    val level: Int,
    val isExpanded: Boolean,
    val hasChildren: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen() {
    // 示例文件树数据
    var selectedFile by remember { mutableStateOf<GitTreeNode?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editContent by remember { mutableStateOf("") }
    var expandedFolders by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }

    // 示例文件树
    val sampleTree = remember {
        listOf(
            GitTreeNode("app", "040000", "tree", "abc1", null, ""),
            GitTreeNode("app/src", "040000", "tree", "abc2", null, ""),
            GitTreeNode("app/src/main", "040000", "tree", "abc3", null, ""),
            GitTreeNode("app/src/main/java", "040000", "tree", "abc4", null, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex", "040000", "tree", "abc5", null, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/model", "040000", "tree", "abc6", null, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/model/AISettings.kt", "100644", "blob", "def1", 2048, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/model/AppSettings.kt", "100644", "blob", "def2", 1536, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/model/ChatMessage.kt", "100644", "blob", "def3", 1024, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/viewmodel", "040000", "tree", "abc7", null, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/viewmodel/MainViewModel.kt", "100644", "blob", "def4", 4096, ""),
            GitTreeNode("app/src/main/java/com/mobilecodex/viewmodel/ChatViewModel.kt", "100644", "blob", "def5", 3072, ""),
            GitTreeNode("app/src/main/AndroidManifest.xml", "100644", "blob", "ghi1", 512, ""),
            GitTreeNode("app/build.gradle.kts", "100644", "blob", "ghi2", 1024, ""),
            GitTreeNode("build.gradle.kts", "100644", "blob", "ghi3", 256, ""),
            GitTreeNode("settings.gradle.kts", "100644", "blob", "ghi4", 128, ""),
            GitTreeNode("gradle.properties", "100644", "blob", "ghi5", 192, ""),
            GitTreeNode("README.md", "100644", "blob", "ghi6", 64, ""),
            GitTreeNode(".gitignore", "100644", "blob", "ghi7", 446, "")
        )
    }

    // 构建层级结构
    val treeItems = remember(sampleTree, expandedFolders) {
        buildTreeItems(sampleTree, expandedFolders)
    }

    // 过滤搜索结果
    val filteredItems = remember(treeItems, searchQuery) {
        if (searchQuery.isBlank()) treeItems
        else treeItems.filter { item ->
            item.node.fileName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // 操作栏
        FileActionBar(
            onExpandAll = { expandedFolders = sampleTree.filter { it.isDirectory }.map { it.path }.toSet() },
            onCollapseAll = { expandedFolders = emptySet<String>() }
        )

        // 文件列表和编辑器
        if (selectedFile != null && selectedFile!!.isFile) {
            // 显示编辑器
            FileEditorView(
                file = selectedFile!!,
                content = editContent,
                isEditing = isEditing,
                onContentChange = { editContent = it },
                onStartEdit = {
                    editContent = "// 文件内容: ${selectedFile!!.fileName}\n// 这里是代码编辑区域\n\npackage com.mobilecodex\n\nimport androidx.compose.runtime.Composable\n\n@Composable\nfun Example() {\n    // TODO: 实现\n}"
                    isEditing = true
                },
                onSave = { isEditing = false },
                onRevert = { isEditing = false },
                onClose = {
                    selectedFile = null
                    isEditing = false
                }
            )
        } else {
            // 显示文件树
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (filteredItems.isEmpty()) {
                    item {
                        EmptyFilesPlaceholder()
                    }
                }

                items(filteredItems, key = { it.node.path }) { item ->
                    FileTreeRow(
                        item = item,
                        isSelected = selectedFile?.path == item.node.path,
                        onClick = {
                            if (item.node.isDirectory) {
                                expandedFolders = if (item.node.path in expandedFolders) {
                                    expandedFolders - item.node.path
                                } else {
                                    expandedFolders + item.node.path
                                }
                            } else {
                                selectedFile = item.node
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = "搜索文件...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun FileActionBar(
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onExpandAll) {
                Icon(
                    imageVector = Icons.Default.UnfoldMore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("展开全部", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onCollapseAll) {
                Icon(
                    imageVector = Icons.Default.UnfoldLess,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("折叠全部", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun FileTreeRow(
    item: FileTreeItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        color = backgroundColor,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = (16 + item.level * 20).dp,
                    end = 16.dp,
                    top = 6.dp,
                    bottom = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 展开/折叠图标
            if (item.node.isDirectory) {
                Icon(
                    imageVector = if (item.isExpanded) Icons.Default.KeyboardArrowDown
                    else Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            // 文件图标
            Text(
                text = getFileEmoji(item.node),
                style = MaterialTheme.typography.bodyMedium
            )

            // 文件名
            Text(
                text = item.node.fileName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = if (item.node.isFile) FontFamily.Monospace else FontFamily.Default
                ),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            // 文件大小
            if (item.node.isFile && item.node.size != null) {
                Text(
                    text = formatFileSize(item.node.size!!),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun FileEditorView(
    file: GitTreeNode,
    content: String,
    isEditing: Boolean,
    onContentChange: (String) -> Unit,
    onStartEdit: () -> Unit,
    onSave: () -> Unit,
    onRevert: () -> Unit,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 文件信息栏
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = getFileEmoji(file),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 操作栏
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isEditing) {
                    FilledTonalButton(
                        onClick = onStartEdit,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("编辑", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onRevert,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("撤销", style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = onSave,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 编辑器区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            if (isEditing) {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            } else {
                // 只读预览
                val previewContent = remember(file) {
                    buildString {
                        appendLine("// ${file.fileName}")
                        appendLine("// 路径: ${file.path}")
                        appendLine("// 大小: ${file.size?.let { formatFileSize(it) } ?: "未知"}")
                        appendLine()
                        appendLine("// 点击「编辑」按钮加载并编辑文件内容")
                        appendLine("// 文件内容将在首次编辑时从 GitHub 懒加载")
                    }
                }
                Text(
                    text = previewContent,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyFilesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                text = "暂无文件",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "切换到 GitHub 仓库工作区\n即可浏览和编辑代码文件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// ==================== 工具函数 ====================

private fun buildTreeItems(
    tree: List<GitTreeNode>,
    expandedFolders: Set<String>
): List<FileTreeItem> {
    val result = mutableListOf<FileTreeItem>()

    fun addChildren(parentPath: String, level: Int) {
        val children = tree.filter { node ->
            val nodeParent = node.parentPath ?: ""
            nodeParent == parentPath
        }.sortedWith { a, b ->
            when {
                a.isDirectory && !b.isDirectory -> -1
                !a.isDirectory && b.isDirectory -> 1
                else -> a.fileName.compareTo(b.fileName)
            }
        }

        children.forEach { node ->
            val isExpanded = node.path in expandedFolders
            val hasChildren = tree.any { it.parentPath == node.path }

            result.add(
                FileTreeItem(
                    node = node,
                    level = level,
                    isExpanded = isExpanded,
                    hasChildren = hasChildren
                )
            )

            if (node.isDirectory && isExpanded) {
                addChildren(node.path, level + 1)
            }
        }
    }

    addChildren("", 0)
    return result
}

private fun getFileEmoji(node: GitTreeNode): String {
    return when {
        node.isDirectory -> if (node.path in emptySet<String>()) "📂" else "📁"
        node.extension == "kt" -> "🟣"
        node.extension == "java" -> "☕"
        node.extension == "xml" -> "📄"
        node.extension == "json" -> "📋"
        node.extension == "md" -> "📝"
        node.extension == "gradle" || node.extension == "kts" -> "🐘"
        node.extension == "properties" -> "⚙️"
        node.extension == "yml" || node.extension == "yaml" -> "📋"
        node.extension == "gitignore" -> "🙈"
        else -> "📄"
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "${bytes}B"
        bytes < 1024 * 1024 -> "${bytes / 1024}KB"
        else -> "${"%.1f".format(bytes / (1024.0 * 1024.0))}MB"
    }
}
