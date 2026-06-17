package me.algosketch.timelog.ui.feature.settings

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import me.algosketch.timelog.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextSecondary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen
import me.algosketch.timelog.ui.util.toMaterialIcon

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onShowAddForm = viewModel::onShowAddForm,
        onNameChange = viewModel::onNameChange,
        onColorSelect = viewModel::onColorSelect,
        onIconSelect = viewModel::onIconSelect,
        onAddType = viewModel::onAddType,
        onCancelAddType = viewModel::onCancelAddType,
        onEfficiencyToggle = viewModel::onEfficiencyToggle,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onShowAddForm: () -> Unit,
    onNameChange: (String) -> Unit,
    onColorSelect: (Int) -> Unit,
    onIconSelect: (Int) -> Unit,
    onAddType: () -> Unit,
    onCancelAddType: () -> Unit,
    onEfficiencyToggle: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader()

        Text(
            text = stringResource(R.string.log_type_section_title),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.66.sp,
            color = TextTertiary,
            lineHeight = 16.5.sp,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.logTypes.forEachIndexed { index, logType ->
                LogTypeCard(
                    logType = logType,
                    onEditClick = {},
                    onEfficiencyToggle = { onEfficiencyToggle(index) }
                )
            }

            if (uiState.showAddForm) {
                NewTypeFormCard(
                    name = uiState.newTypeName,
                    onNameChange = onNameChange,
                    selectedColorIndex = uiState.selectedColorIndex,
                    onColorSelect = onColorSelect,
                    selectedIconIndex = uiState.selectedIconIndex,
                    onIconSelect = onIconSelect,
                    onAdd = onAddType,
                    onCancel = onCancelAddType,
                )
            } else {
                AddTypeButton(onClick = onShowAddForm)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_label),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.88.sp,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            lineHeight = 30.sp
        )
    }
}

@Composable
private fun LogTypeCard(
    logType: LogType,
    onEditClick: () -> Unit,
    onEfficiencyToggle: () -> Unit,
) {
    val subtitle = if (logType.includeEfficiency) stringResource(R.string.efficiency_included) else stringResource(R.string.efficiency_excluded)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(logType.color.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = logType.icon.toMaterialIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = logType.color
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = logType.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    lineHeight = 22.5.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    lineHeight = 16.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .width(46.dp)
                    .height(31.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                    .clickable(onClick = onEditClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.btn_edit),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    lineHeight = 16.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val effBgColor = if (logType.includeEfficiency) logType.color.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f)
        val effBorderColor = if (logType.includeEfficiency) logType.color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f)
        val effTextColor = if (logType.includeEfficiency) logType.color else TextTertiary
        val effLabel = if (logType.includeEfficiency) stringResource(R.string.efficiency_check_on) else stringResource(R.string.efficiency_check_off)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(effBgColor)
                .border(1.dp, effBorderColor, RoundedCornerShape(8.dp))
                .clickable(onClick = onEfficiencyToggle),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = effLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = effTextColor,
                lineHeight = 16.5.sp
            )
        }
    }
}

@Composable
private fun AddTypeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.02f),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(8.dp.toPx(), 4.dp.toPx())
                        )
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.btn_add_type),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            lineHeight = 19.5.sp
        )
    }
}

@Composable
private fun NewTypeFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    selectedColorIndex: Int,
    onColorSelect: (Int) -> Unit,
    selectedIconIndex: Int,
    onIconSelect: (Int) -> Unit,
    onAdd: () -> Unit,
    onCancel: () -> Unit,
) {
    val selectedColor = colorOptions[selectedColorIndex]
    val canAdd = name.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, WorkGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Text(
            text = stringResource(R.string.new_type_form_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.field_name),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
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
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
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

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.field_color),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            colorOptions.forEachIndexed { index, color ->
                ColorSwatch(
                    color = color,
                    isSelected = index == selectedColorIndex,
                    onClick = { onColorSelect(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.field_icon),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                iconOptions.take(6).forEachIndexed { index, icon ->
                    IconOption(
                        icon = icon,
                        isSelected = index == selectedIconIndex,
                        selectedColor = selectedColor,
                        onClick = { onIconSelect(index) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                iconOptions.drop(6).forEachIndexed { index, icon ->
                    IconOption(
                        icon = icon,
                        isSelected = (index + 6) == selectedIconIndex,
                        selectedColor = selectedColor,
                        onClick = { onIconSelect(index + 6) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(41.dp)
                    .alpha(if (canAdd) 1f else 0.5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WorkGreen.copy(alpha = 0.08f))
                    .border(1.dp, WorkGreen.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .clickable(enabled = canAdd, onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.btn_add),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = WorkGreen,
                    lineHeight = 19.5.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(41.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.btn_cancel),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextTertiary,
                    lineHeight = 19.5.sp
                )
            }
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
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.13f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else Color.White.copy(alpha = 0.06f),
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
private fun IconOption(
    icon: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) selectedColor.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
            .border(
                width = 1.dp,
                color = if (isSelected) selectedColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.06f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon.toMaterialIcon(),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (isSelected) selectedColor else TextTertiary
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewSettingsScreen() {
    SettingsContent(
        uiState = SettingsUiState(
            logTypes = listOf(
                LogType("일하는 중", "play_arrow", Color(0xFF4ADE80), true),
                LogType("쉬는 중", "free_breakfast", Color(0xFFFB923C), false),
            )
        ),
        onShowAddForm = {}, onNameChange = {}, onColorSelect = {},
        onIconSelect = {}, onAddType = {}, onCancelAddType = {}, onEfficiencyToggle = {},
    )
}
