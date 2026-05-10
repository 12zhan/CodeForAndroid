package com.mobilecodex.ui.screens.files

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobilecodex.model.GitHubRepository
import com.mobilecodex.model.GitTreeNode
import com.mobilecodex.model.GitHubContentItem
import com.mobilecodex.model.VirtualFile
import com.mobilecodex.viewmodel.FileViewModel
import com.mobilecodex.viewmodel.GitHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    gitHubViewModel: GitHubViewModel,
    fileViewModel: FileViewModel
) {
    val gitHubState by gitHubViewModel.uiState.collectAsState()
    val fileState by fileViewModel.uiState.collectAsState()

    if (gitHubState.selectedRepo == null) {
        // 仓库列表视图
        RepositoryListView(
            state = gitHubState,
            onSearchQueryChange = { gitHubViewModel.updateSearchQuery(it) },
            onSearch = { gitHubViewModel.searchRepositories() },
            onRefresh = { gitHubViewModel.loadRepositories() },
            onSelectRepo = { gitHubViewModel.selectRepository(it) },
            onClearSearch = { gitHubViewModel.clearSearch() }
        )
    } else {
        // 文件浏览视图
        val repo = gitHubState.selectedRepo!!
        val parts = repo.fullName.split("/")
        val owner = parts.getOrElse(0) { "" }
        val repoName = parts.getOrElse(1) { "" }

        FileExplorerView(
            repo = repo,
            fileTree = gitHubState.fileTree,
            directoryContents = fileState.directoryContents,
            openedFiles = fileState.openedFiles,
            activeFile = fileState.activeFile,
            currentPath = fileState.currentPath,
            isLoadingTree = gitHubState.isLoadingTree,
            isLoadingContent = fileState.isLoadingDirectory,
            isLoadingFile = fileState.isLoadingFile,
            isSaving = fileState.isSaving,
            commitMessage = fileState.commitMessage,
            error = gitHubState.error ?: fileState.error,
            onBack = { gitHubViewModel.goBackToRepositories() },
            onNavigateToDirectory = { path ->
                gitHubViewModel.navigateToDirectory(path)
            },
            onOpenFile = { item ->
                fileViewModel.openFile(
                    owner = owner,
                    repo = repoName,
                    path = item.path,
                    name = item.name,
                    downloadUrl = item.downloadUrl,
                    sha = item.sha
                )
            },
            onCloseFile = { fileViewModel.closeFile(it) },
            onSelectFile = { fileViewModel.selectFile(it) },
            onUpdateContent = { id, content ->
                fileViewModel.updateFileContent(id, content)
            },
            onCommitFile = {
                fileViewModel.commitActiveFile(owner, repoName)
            },
            onCommitMessageChange = { fileViewModel.updateCommitMessage(it) }
        )
    }
}

@Composable
private fun RepositoryListView(
    state: com.mobilecodex.viewmodel.GitHubUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRefresh: () -> Unit,
    onSelectRepo: (GitHubRepository) -> Unit,
    onClearSearch: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("仓库列表", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                }
            }
        )

        // 搜索栏
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("搜索仓库...") },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Filled.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // 错误提示
        if (state.error != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = state.error,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
        }

        // 仓库列表
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.repositories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "没有仓库",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Text(
                        "请确保已配置 GitHub Token",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.repositories, key = { it.id }) { repo ->
                    RepositoryCard(repo = repo, onClick = { onSelectRepo(repo) })
                }
            }
        }
    }
}

@Composable
private fun RepositoryCard(
    repo: GitHubRepository,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (repo.isPrivate) Icons.Filled.Lock else Icons.Filled.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = repo.fullName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (!repo.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = repo.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (repo.language != null) {
                    LabelChip(
                        icon = Icons.Filled.Code,
                        text = repo.language
                    )
                }
                LabelChip(
                    icon = Icons.Filled.Star,
                    text = repo.stars.toString()
                )
                LabelChip(
                    icon = Icons.Filled.CallSplit,
                    text = repo.forks.toString()
                )
            }
        }
    }
}

@Composable
private fun LabelChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// --- 文件浏览器视图 ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileExplorerView(
    repo: GitHubRepository,
    fileTree: List<GitTreeNode>,
    directoryContents: List<GitHubContentItem>,
    openedFiles: List<VirtualFile>,
    activeFile: VirtualFile?,
    currentPath: String,
    isLoadingTree: Boolean,
    isLoadingContent: Boolean,
    isLoadingFile: Boolean,
    isSaving: Boolean,
    commitMessage: String,
    error: String?,
    onBack: () -> Unit,
    onNavigateToDirectory: (String) -> Unit,
    onOpenFile: (GitHubContentItem) -> Unit,
    onCloseFile: (String) -> Unit,
    onSelectFile: (String) -> Unit,
    onUpdateContent: (String, String) -> Unit,
    onCommitFile: () -> Unit,
    onCommitMessageChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text(repo.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        repo.fullName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // 文件树侧边栏
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(260.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 2.dp
            ) {
                if (isLoadingTree) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        // 面包屑导航
                        if (currentPath.isNotEmpty()) {
                            item {
                                BreadcrumbNav(
                                    currentPath = currentPath,
                                    onNavigateUp = {
                                        val parent = currentPath.substringBeforeLast("/", "")
                                        onNavigateToDirectory(parent)
                                    }
                                )
                            }
                        }

                        // 目录内容
                        if (isLoadingContent) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                }
                            }
                        } else if (directoryContents.isNotEmpty()) {
                            items(directoryContents) { item ->
                                FileTreeItem(
                                    item = item,
                                    onClick = {
                                        if (item.isDirectory) {
                                            onNavigateToDirectory(item.path)
                                        } else {
                                            onOpenFile(item)
                                        }
                                    }
                                )
                            }
                        } else if (fileTree.isNotEmpty()) {
                            // 显示原始文件树（扁平列表）
                            items(fileTree.filter { !it.path.contains("/") || it.isDirectory }) { node ->
                                SimpleFileTreeItem(
                                    node = node,
                                    onClick = {
                                        // 简化：直接构造 content item
                                    }
                                )
                            }
                        }

                        if (directoryContents.isEmpty() && fileTree.isEmpty() && !isLoadingContent && !isLoadingTree) {
                            item {
                                Text(
                                    "文件树为空",
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // 编辑器主体
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // 已打开文件的标签栏
                if (openedFiles.isNotEmpty()) {
                    FileTabsRow(
                        files = openedFiles,
                        activeFileId = activeFile?.id,
                        onSelect = onSelectFile,
                        onClose = onCloseFile
                    )
                }

                // 文件内容编辑器
                if (activeFile != null) {
                    CodeEditor(
                        file = activeFile,
                        onContentChange = { onUpdateContent(activeFile.id, it) },
                        onSave = onCommitFile,
                        isSaving = isSaving,
                        commitMessage = commitMessage,
                        onCommitMessageChange = onCommitMessageChange,
                        isLoading = isLoadingFile,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "选择文件以查看内容",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbNav(
    currentPath: String,
    onNavigateUp: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowUpward,
                    contentDescription = "上级目录",
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = currentPath.ifEmpty { "/" },
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FileTreeItem(
    item: GitHubContentItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (item.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (item.isDirectory) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = item.name,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SimpleFileTreeItem(
    node: GitTreeNode,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (node.isDirectory) Icons.Filled.Folder else Icons.Filled.InsertDriveFile,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (node.isDirectory) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = node.name,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FileTabsRow(
    files: List<VirtualFile>,
    activeFileId: String?,
    onSelect: (String) -> Unit,
    onClose: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        LazyColumn {
            items(files, key = { it.id }) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (file.id == activeFileId) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .clickable { onSelect(file.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (file.isModified) {
                            Icon(
                                Icons.Filled.Circle,
                                contentDescription = "已修改",
                                modifier = Modifier.size(8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = file.name,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (file.id == activeFileId) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = file.language,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = { onClose(file.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "关闭",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeEditor(
    file: VirtualFile,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var showCommitDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // 状态栏
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = file.language,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${file.content.lines().size} 行",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (file.isModified) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "已修改",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 保存按钮
                if (file.isModified) {
                    Button(
                        onClick = { showCommitDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("提交", fontSize = 12.sp)
                    }
                }
            }
        }

        // 编辑器
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            BasicTextField(
                value = file.content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 20.sp
                ),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (file.content.isEmpty()) {
                            Text(
                                "// 文件为空",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }

    // 提交对话框
    if (showCommitDialog) {
        CommitDialog(
            commitMessage = commitMessage,
            onCommitMessageChange = onCommitMessageChange,
            fileName = file.name,
            onConfirm = {
                onSave()
                showCommitDialog = false
            },
            onDismiss = { showCommitDialog = false }
        )
    }
}

@Composable
private fun CommitDialog(
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    fileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("提交更改") },
        text = {
            Column {
                Text(
                    "文件: $fileName",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = commitMessage,
                    onValueChange = onCommitMessageChange,
                    label = { Text("提交信息") },
                    placeholder = { Text("描述你的更改...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
