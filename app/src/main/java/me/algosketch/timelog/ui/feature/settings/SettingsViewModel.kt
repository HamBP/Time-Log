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
}

private fun LogTypeEntity.toDomain() = LogType(
    id = id,
    name = name,
    icon = icon,
    color = colorHex.toComposeColor(),
    includeEfficiency = includeEfficiency,
)
