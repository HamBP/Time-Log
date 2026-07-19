package me.algosketch.timelog.ui.feature.logtypeform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.ui.theme.toHex
import javax.inject.Inject

@HiltViewModel
class LogTypeFormViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogTypeFormUiState())
    val uiState = _uiState.asStateFlow()

    private var editingId: Int? = null

    /** typeId가 null이면 추가 모드, 값이 있으면 해당 타입을 불러와 수정 모드로 채운다. */
    fun load(typeId: Int?) {
        if (typeId == null) return
        editingId = typeId
        viewModelScope.launch {
            val entity = repository.getLogType(typeId) ?: return@launch
            val colorIndex = colorOptions
                .indexOfFirst { it.toHex().equals(entity.colorHex, ignoreCase = true) }
                .coerceAtLeast(0)
            val iconIndex = iconOptions.indexOf(entity.icon).coerceAtLeast(0)
            _uiState.update {
                it.copy(
                    name = entity.name,
                    selectedIconIndex = iconIndex,
                    selectedColorIndex = colorIndex,
                    includeEfficiency = entity.includeEfficiency,
                    isEditMode = true,
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onIconSelect(index: Int) {
        _uiState.update { it.copy(selectedIconIndex = index) }
    }

    fun onColorSelect(index: Int) {
        _uiState.update { it.copy(selectedColorIndex = index) }
    }

    fun onEfficiencyChange(includeEfficiency: Boolean) {
        _uiState.update { it.copy(includeEfficiency = includeEfficiency) }
    }

    fun onSave(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) return
        val colorHex = colorOptions[state.selectedColorIndex].toHex()
        val icon = iconOptions[state.selectedIconIndex]
        viewModelScope.launch {
            val id = editingId
            if (id == null) {
                repository.addLogType(state.name, colorHex, icon, state.includeEfficiency)
            } else {
                repository.updateLogType(id, state.name, colorHex, icon, state.includeEfficiency)
            }
            onSaved()
        }
    }
}
