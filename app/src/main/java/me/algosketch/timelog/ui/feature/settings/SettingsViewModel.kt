package me.algosketch.timelog.ui.feature.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        SettingsUiState(
            logTypes = listOf(
                LogType("일하는 중", "▶", Color(0xFF4ADE80), true),
                LogType("쉬는 중", "☕", Color(0xFFFB923C), false),
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onShowAddForm() {
        _uiState.update { it.copy(showAddForm = true) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(newTypeName = name) }
    }

    fun onColorSelect(index: Int) {
        _uiState.update { it.copy(selectedColorIndex = index) }
    }

    fun onIconSelect(index: Int) {
        _uiState.update { it.copy(selectedIconIndex = index) }
    }

    fun onAddType() {
        val state = _uiState.value
        if (state.newTypeName.isBlank()) return
        val newType = LogType(
            name = state.newTypeName,
            icon = iconOptions[state.selectedIconIndex],
            color = colorOptions[state.selectedColorIndex],
            includeEfficiency = true,
        )
        _uiState.update {
            it.copy(
                logTypes = it.logTypes + newType,
                showAddForm = false,
                newTypeName = "",
                selectedColorIndex = 2,
                selectedIconIndex = 2,
            )
        }
    }

    fun onCancelAddType() {
        _uiState.update {
            it.copy(
                showAddForm = false,
                newTypeName = "",
                selectedColorIndex = 2,
                selectedIconIndex = 2,
            )
        }
    }

    fun onEfficiencyToggle(index: Int) {
        _uiState.update { state ->
            state.copy(
                logTypes = state.logTypes.mapIndexed { i, type ->
                    if (i == index) type.copy(includeEfficiency = !type.includeEfficiency) else type
                }
            )
        }
    }
}
