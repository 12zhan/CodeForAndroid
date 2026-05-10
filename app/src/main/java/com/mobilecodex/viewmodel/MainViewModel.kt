package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import com.mobilecodex.model.WorkspaceOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MainUiState(
    val currentScreen: WorkspaceOption = WorkspaceOption.CHAT,
    val isSidebarOpen: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun navigateTo(screen: WorkspaceOption) {
        _uiState.update {
            it.copy(currentScreen = screen, isSidebarOpen = false)
        }
    }

    fun toggleSidebar() {
        _uiState.update { it.copy(isSidebarOpen = !it.isSidebarOpen) }
    }

    fun closeSidebar() {
        _uiState.update { it.copy(isSidebarOpen = false) }
    }
}
