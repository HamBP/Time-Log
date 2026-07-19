package me.algosketch.timelog.ui.feature.logtypeform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.algosketch.timelog.R
import me.algosketch.timelog.ui.component.TimeLogAppBar
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen
import me.algosketch.timelog.ui.util.toMaterialIcon

private val cardBorderColor = Color.White.copy(alpha = 0.06f)

@Composable
fun LogTypeFormScreen(
    typeId: Int? = null,
    onBack: () -> Unit = {},
    viewModel: LogTypeFormViewModel = hiltViewModel(),
) {
    LaunchedEffect(typeId) { viewModel.load(typeId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LogTypeFormContent(
        uiState = uiState,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onIconSelect = viewModel::onIconSelect,
        onColorSelect = viewModel::onColorSelect,
        onEfficiencyChange = viewModel::onEfficiencyChange,
        onSave = { viewModel.onSave(onBack) },
    )
}

@Composable
private fun LogTypeFormContent(
    uiState: LogTypeFormUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onIconSelect: (Int) -> Unit,
    onColorSelect: (Int) -> Unit,
    onEfficiencyChange: (Boolean) -> Unit,
    onSave: () -> Unit,
) {
    val title = stringResource(
        if (uiState.isEditMode) R.string.log_type_edit_title else R.string.log_type_add_title
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        TimeLogAppBar(title = title, onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormSection(label = stringResource(R.string.field_name)) {
                NameField(name = uiState.name, onNameChange = onNameChange)
            }

            FormSection(label = stringResource(R.string.field_icon)) {
                IconGrid(
                    selectedIconIndex = uiState.selectedIconIndex,
                    onIconSelect = onIconSelect,
                )
            }

            FormSection(label = stringResource(R.string.efficiency_check_title)) {
                EfficiencySegment(
                    includeEfficiency = uiState.includeEfficiency,
                    onEfficiencyChange = onEfficiencyChange,
                )
            }

            // 색상 선택은 효율성 "포함"일 때만 노출한다.
            if (uiState.includeEfficiency) {
                FormSection(label = stringResource(R.string.field_color)) {
                    ColorRow(
                        selectedColorIndex = uiState.selectedColorIndex,
                        onColorSelect = onColorSelect,
                    )
                }
            }

            SaveButton(
                label = stringResource(
                    if (uiState.isEditMode) R.string.btn_save else R.string.btn_add
                ),
                enabled = uiState.name.isNotBlank(),
                onClick = onSave,
            )
        }
    }
}

@Composable
private fun FormSection(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.72.sp,
            color = TextTertiary,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun NameField(
    name: String,
    onNameChange: (String) -> Unit,
) {
    BasicTextField(
        value = name,
        onValueChange = onNameChange,
        textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
        cursorBrush = SolidColor(WorkGreen),
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Background)
            .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        decorationBox = { innerTextField ->
            Box {
                if (name.isEmpty()) {
                    Text(
                        text = stringResource(R.string.field_name_hint),
                        fontSize = 14.sp,
                        color = TextPrimary.copy(alpha = 0.5f),
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun IconGrid(
    selectedIconIndex: Int,
    onIconSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        iconOptions.chunked(6).forEachIndexed { rowIdx, rowIcons ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowIcons.forEachIndexed { colIdx, icon ->
                    IconCell(
                        icon = icon,
                        isSelected = rowIdx * 6 + colIdx == selectedIconIndex,
                        onClick = { onIconSelect(rowIdx * 6 + colIdx) },
                    )
                }
            }
        }
    }
}

@Composable
private fun IconCell(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) WorkGreen.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
            .border(
                width = 1.dp,
                color = if (isSelected) WorkGreen.copy(alpha = 0.45f) else cardBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon.toMaterialIcon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) WorkGreen else TextTertiary
        )
    }
}

@Composable
private fun ColorRow(
    selectedColorIndex: Int,
    onColorSelect: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        colorOptions.forEachIndexed { index, color ->
            ColorSwatch(
                color = color,
                isSelected = index == selectedColorIndex,
                onClick = { onColorSelect(index) },
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.13f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else cardBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

@Composable
private fun EfficiencySegment(
    includeEfficiency: Boolean,
    onEfficiencyChange: (Boolean) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SegmentButton(
            label = stringResource(R.string.segment_include),
            isSelected = includeEfficiency,
            onClick = { onEfficiencyChange(true) },
            modifier = Modifier.weight(1f),
        )
        SegmentButton(
            label = stringResource(R.string.segment_exclude),
            isSelected = !includeEfficiency,
            onClick = { onEfficiencyChange(false) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SegmentButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) WorkGreen.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
            .border(
                width = 1.dp,
                color = if (isSelected) WorkGreen.copy(alpha = 0.35f) else cardBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) WorkGreen else TextTertiary,
            lineHeight = 19.5.sp,
        )
    }
}

@Composable
private fun SaveButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(WorkGreen.copy(alpha = 0.08f))
            .border(1.dp, WorkGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = WorkGreen,
            lineHeight = 22.5.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewLogTypeFormAdd() {
    LogTypeFormContent(
        uiState = LogTypeFormUiState(name = ""),
        onBack = {}, onNameChange = {}, onIconSelect = {},
        onColorSelect = {}, onEfficiencyChange = {}, onSave = {},
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewLogTypeFormEditExcluded() {
    LogTypeFormContent(
        uiState = LogTypeFormUiState(
            name = "쉬는 중",
            includeEfficiency = false,
            isEditMode = true,
        ),
        onBack = {}, onNameChange = {}, onIconSelect = {},
        onColorSelect = {}, onEfficiencyChange = {}, onSave = {},
    )
}
