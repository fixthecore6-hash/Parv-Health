package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.HeightUnit
import com.example.data.ProfileSex
import com.example.data.UserProfile
import com.example.data.WeightUnit
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onSaveProfile: (UserProfile) -> Unit,
    onClearProfile: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    var nameInput by remember(userProfile) { mutableStateOf(userProfile.name) }
    var ageInput by remember(userProfile) { mutableStateOf(userProfile.age?.toString() ?: "") }
    var sexInput by remember(userProfile) { mutableStateOf(userProfile.sex) }
    var weightInput by remember(userProfile) {
        mutableStateOf(userProfile.weightValue?.let { formatInputNumber(it) } ?: "")
    }
    var weightUnit by remember(userProfile) { mutableStateOf(userProfile.weightUnit) }
    var heightInput by remember(userProfile) {
        mutableStateOf(userProfile.heightValue?.let { formatInputNumber(it) } ?: "")
    }
    var heightUnit by remember(userProfile) { mutableStateOf(userProfile.heightUnit) }
    var emergencyInput by remember(userProfile) { mutableStateOf(userProfile.emergencyContact) }

    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isErrorFeedback by remember { mutableStateOf(false) }

    val savedMessage = stringResource(R.string.profile_saved_message)
    val clearedMessage = stringResource(R.string.profile_cleared_message)
    val invalidNumbersMessage = stringResource(R.string.profile_invalid_numbers)

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            kotlinx.coroutines.delay(3000)
            feedbackMessage = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.profile_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { onToggleTheme(isDark) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("profile_theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(R.string.toggle_theme_desc),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Form Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = if (isDark) 2.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Name
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(stringResource(R.string.profile_field_name)) },
                        placeholder = { Text(stringResource(R.string.profile_field_name_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )

                    // Age
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { input ->
                            if (input.length <= 3 && input.all { it.isDigit() }) {
                                ageInput = input
                            }
                        },
                        label = { Text(stringResource(R.string.profile_field_age)) },
                        placeholder = { Text(stringResource(R.string.profile_field_age_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_age_input")
                    )

                    // Optional Sex (used for calorie target formula)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.profile_field_sex),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val sexOptions = listOf(
                                ProfileSex.UNSPECIFIED to stringResource(R.string.sex_unspecified),
                                ProfileSex.FEMALE to stringResource(R.string.sex_female),
                                ProfileSex.MALE to stringResource(R.string.sex_male)
                            )
                            sexOptions.forEach { (sexOption, label) ->
                                FilterChip(
                                    selected = sexInput == sexOption,
                                    onClick = { sexInput = sexOption },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = accents.greenTint,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.testTag("profile_sex_${sexOption.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Weight + kg/lb toggle
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.profile_field_weight),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            UnitTogglePill(
                                options = listOf(
                                    WeightUnit.KG to stringResource(R.string.unit_kg),
                                    WeightUnit.LB to stringResource(R.string.unit_lb)
                                ),
                                selected = weightUnit,
                                onSelect = { newUnit ->
                                    if (newUnit != weightUnit) {
                                        val current = weightInput.toFloatOrNull()
                                        if (current != null && current > 0f) {
                                            val converted = if (newUnit == WeightUnit.LB) {
                                                current / 0.45359237f
                                            } else {
                                                current * 0.45359237f
                                            }
                                            weightInput = formatInputNumber(converted)
                                        }
                                        weightUnit = newUnit
                                    }
                                },
                                tagPrefix = "weight_unit"
                            )
                        }
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { input ->
                                if (isValidDecimalInput(input)) {
                                    weightInput = input
                                }
                            },
                            suffix = {
                                Text(
                                    if (weightUnit == WeightUnit.KG) {
                                        stringResource(R.string.unit_kg)
                                    } else {
                                        stringResource(R.string.unit_lb)
                                    }
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_weight_input")
                        )
                    }

                    // Height + cm/ft toggle
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.profile_field_height),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            UnitTogglePill(
                                options = listOf(
                                    HeightUnit.CM to stringResource(R.string.unit_cm),
                                    HeightUnit.FT to stringResource(R.string.unit_ft)
                                ),
                                selected = heightUnit,
                                onSelect = { newUnit ->
                                    if (newUnit != heightUnit) {
                                        val current = heightInput.toFloatOrNull()
                                        if (current != null && current > 0f) {
                                            val converted = if (newUnit == HeightUnit.FT) {
                                                current / 30.48f
                                            } else {
                                                current * 30.48f
                                            }
                                            heightInput = formatInputNumber(converted)
                                        }
                                        heightUnit = newUnit
                                    }
                                },
                                tagPrefix = "height_unit"
                            )
                        }
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { input ->
                                if (isValidDecimalInput(input)) {
                                    heightInput = input
                                }
                            },
                            suffix = {
                                Text(
                                    if (heightUnit == HeightUnit.CM) {
                                        stringResource(R.string.unit_cm)
                                    } else {
                                        stringResource(R.string.unit_ft)
                                    }
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_height_input")
                        )
                    }

                    // Emergency Contact Number
                    OutlinedTextField(
                        value = emergencyInput,
                        onValueChange = { input ->
                            if (input.length <= 24) {
                                emergencyInput = input
                            }
                        },
                        label = { Text(stringResource(R.string.profile_field_emergency)) },
                        placeholder = { Text(stringResource(R.string.profile_field_emergency_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_emergency_input")
                    )

                    AnimatedVisibility(visible = feedbackMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isErrorFeedback) {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    } else {
                                        accents.greenTint
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = if (isErrorFeedback) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = feedbackMessage ?: "",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = if (isErrorFeedback) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }

                    // Save Button (50dp tall)
                    Button(
                        onClick = {
                            val parsedAge = ageInput.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
                            val parsedWeight = weightInput.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()
                            val parsedHeight = heightInput.trim().takeIf { it.isNotEmpty() }?.toFloatOrNull()

                            val weightInvalid = weightInput.isNotBlank() && (parsedWeight == null || parsedWeight <= 0f)
                            val heightInvalid = heightInput.isNotBlank() && (parsedHeight == null || parsedHeight <= 0f)

                            if (weightInvalid || heightInvalid) {
                                isErrorFeedback = true
                                feedbackMessage = invalidNumbersMessage
                            } else {
                                onSaveProfile(
                                    UserProfile(
                                        name = nameInput.trim(),
                                        age = parsedAge,
                                        sex = sexInput,
                                        weightValue = parsedWeight,
                                        weightUnit = weightUnit,
                                        heightValue = parsedHeight,
                                        heightUnit = heightUnit,
                                        emergencyContact = emergencyInput.trim()
                                    )
                                )
                                isErrorFeedback = false
                                feedbackMessage = savedMessage
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("profile_save_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.profile_save_button),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    // Clear Profile Button (48dp tall)
                    OutlinedButton(
                        onClick = {
                            nameInput = ""
                            ageInput = ""
                            sexInput = ProfileSex.UNSPECIFIED
                            weightInput = ""
                            heightInput = ""
                            emergencyInput = ""
                            onClearProfile()
                            isErrorFeedback = false
                            feedbackMessage = clearedMessage
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("profile_clear_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.profile_clear_button),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Local privacy note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.profile_privacy_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun <T> UnitTogglePill(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    tagPrefix: String
) {
    val accents = LocalAccentColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { (option, label) ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) accents.greenPrimary else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("${tagPrefix}_${label.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

private fun isValidDecimalInput(input: String): Boolean {
    if (input.isEmpty()) return true
    if (input.length > 6) return false
    var dotCount = 0
    for (ch in input) {
        if (ch == '.') {
            dotCount++
            if (dotCount > 1) return false
        } else if (!ch.isDigit()) {
            return false
        }
    }
    return true
}

private fun formatInputNumber(value: Float): String {
    val formatted = String.format(Locale.US, "%.1f", value)
    return if (formatted.endsWith(".0")) formatted.dropLast(2) else formatted
}
