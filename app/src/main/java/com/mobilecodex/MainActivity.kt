package com.mobilecodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobilecodex.model.WorkspaceOption
import com.mobilecodex.ui.theme.MobileCodexTheme
import com.mobilecodex.ui.screens.chat.ChatScreen
import com.mobilecodex.ui.screens.files.FilesScreen
import com.mobilecodex.ui.screens.settings.SettingsScreen
import com.mobilecodex.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by mainViewModel.uiState.collectAsState()
            val settings by settingsViewModel.uiState.collectAsState()

            MobileCodexTheme(darkTheme = settings.isDarkTheme) {
                MainApp(
                    currentScreen = uiState.currentScreen,
                    isSidebarOpen = uiState.isSidebarOpen,
                    onNavigate = { mainViewModel.navigateTo(it) },
                    onToggleSidebar = { mainViewModel.toggleSidebar() },
                    onCloseSidebar = { mainViewModel.closeSidebar() },
                    mainViewModel = mainViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
private fun MainApp(
    currentScreen: WorkspaceOption,
    isSidebarOpen: Boolean,
    onNavigate: (WorkspaceOption) -> Unit,
    onToggleSidebar: () -> Unit,
    onCloseSidebar: () -> Unit,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val gitHubViewModel: GitHubViewModel = hiltViewModel()
    val fileViewModel: FileViewModel = hiltViewModel()

    // 导航状态
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsScreen(
            viewModel = settingsViewModel,
            onBack = { showSettings = false }
        )
        return
    }

    // 抽屉式侧边栏
    ModalNavigationDrawer(
        drawerState = rememberDrawerState(
            if (isSidebarOpen) DrawerValue.Open else DrawerValue.Closed
        ),
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                SidebarContent(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        onNavigate(screen)
                    },
                    onOpenSettings = {
                        showSettings = true
                        onCloseSidebar()
                    }
                )
            }
        }
    ) {
        Scaffold(
            floatingActionButton = {
                // 侧边栏切换按钮
                if (!isSidebarOpen) {
                    FloatingActionButton(
                        onClick = onToggleSidebar,
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "菜单",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    WorkspaceOption.CHAT -> {
                        ChatScreen(
                            viewModel = chatViewModel,
                            onNavigateToSettings = { showSettings = true }
                        )
                    }
                    WorkspaceOption.REPOSITORIES,
                    WorkspaceOption.FILES -> {
                        FilesScreen(
                            gitHubViewModel = gitHubViewModel,
                            fileViewModel = fileViewModel
                        )
                    }
                    WorkspaceOption.SETTINGS -> {
                        showSettings = true
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarContent(
    currentScreen: WorkspaceOption,
    onNavigate: (WorkspaceOption) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 品牌区
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "MobileCodex",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI-Powered Code Editor",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 导航菜单
        WorkspaceOption.values().forEach { option ->
            NavigationDrawerItem(
                icon = {
                    Icon(
                        when (option) {
                            WorkspaceOption.CHAT -> Icons.Filled.Chat
                            WorkspaceOption.REPOSITORIES -> Icons.Filled.Folder
                            WorkspaceOption.FILES -> Icons.Filled.Description
                            WorkspaceOption.SETTINGS -> Icons.Filled.Settings
                        },
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        option.displayName,
                        fontWeight = if (currentScreen == option) FontWeight.Bold else FontWeight.Normal
                    )
                },
                selected = currentScreen == option,
                onClick = { onNavigate(option) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 底部设置
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            icon = {
                Icon(Icons.Filled.Settings, contentDescription = null)
            },
            label = { Text("设置") },
            selected = false,
            onClick = onOpenSettings,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
