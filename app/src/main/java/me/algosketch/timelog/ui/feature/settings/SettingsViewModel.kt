package me.algosketch.timelog.ui.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import me.algosketch.timelog.ui.theme.toComposeColor
import me.algosketch.timelog.ui.theme.toHex
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLogTypesFlow().collect { entities ->
                _uiState.update { state ->
                    state.copy(logTypes = entities.map { it.toDomain() })
                }
            }
        }
    }

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
        viewModelScope.launch {
            repository.addLogType(
                name = state.newTypeName,
                colorHex = colorOptions[state.selectedColorIndex].toHex(),
                icon = iconOptions[state.selectedIconIndex],
                includeEfficiency = true,
            )
        }
        _uiState.update {
            it.copy(
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
        val type = _uiState.value.logTypes.getOrNull(index) ?: return
        viewModelScope.launch {
            repository.updateLogTypeEfficiency(type.id, !type.includeEfficiency)
        }
    }
}

private fun LogTypeEntity.toDomain() = LogType(
    id = id,
    name = name,
    icon = icon,
    color = colorHex.toComposeColor(),
    includeEfficiency = includeEfficiency,
)
