package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DateUtils
import com.example.data.MedicationDoseLogEntity
import com.example.data.MedicationDoseStatus
import com.example.data.MedicationEntity
import com.example.data.SafetyHealthRepository
import com.example.ui.DueMedicationDoseItem
import com.example.ui.MedicationWithStats
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme

@Composable
fun MedicationsScreen(
    medicationsWithStats: List<MedicationWithStats>,
    dueDoses: List<DueMedicationDoseItem>,
    medicationNotificationsEnabled: Boolean,
    onToggleNotifications: (Boolean) -> Unit,
    onSaveMedication: (MedicationEntity) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onMarkTaken: (MedicationDoseLogEntity) -> Unit,
    onSnoozeTenMinutes: (MedicationDoseLogEntity) -> Unit,
    onSkipDose: (MedicationDoseLogEntity) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    var editingMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    var showFormSheet by remember { mutableStateOf(false) }
    var medPendingDelete by remember { mutableStateOf<MedicationEntity?>(null) }

    BackHandler(onBack = onBack)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("meds_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.meds_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.meds_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onToggleTheme(isDark) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = stringResource(R.string.toggle_theme_desc),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Button(
                            onClick = {
                                editingMedication = null
                                showFormSheet = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("meds_add_button")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.meds_add_button))
                        }
                    }
                }
            }

            // Required Verbatim Medication & General Disclaimers
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = accents.greenTint,
                    border = BorderStroke(1.dp, accents.greenPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HealthAndSafety,
                                contentDescription = null,
                                tint = accents.greenPrimary
                            )
                            Text(
                                text = stringResource(R.string.meds_disclaimer),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("meds_verbatim_disclaimer")
                            )
                        }
                        Text(
                            text = stringResource(R.string.safety_disclaimer_general),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("meds_safety_note")
                        )
                    }
                }
            }

            // Medication Notifications Toggle Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = accents.greenPrimary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.meds_notifications_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.meds_notifications_subtitle),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = medicationNotificationsEnabled,
                            onCheckedChange = onToggleNotifications,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = accents.greenPrimary
                            ),
                            modifier = Modifier.testTag("meds_notifications_switch")
                        )
                    }
                }
            }

            // Due Now / Snoozed Actions Section
            if (dueDoses.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.meds_due_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(items = dueDoses, key = { "due_${it.log.id}" }) { dueItem ->
                    DueDoseActionCard(
                        item = dueItem,
                        onTaken = { onMarkTaken(dueItem.log) },
                        onSnooze = { onSnoozeTenMinutes(dueItem.log) },
                        onSkip = { onSkipDose(dueItem.log) }
                    )
                }
            }

            if (medicationsWithStats.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(accents.greenTint),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Medication,
                                    contentDescription = null,
                                    tint = accents.greenPrimary
                                )
                            }
                            Text(
                                text = stringResource(R.string.meds_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = stringResource(R.string.meds_empty_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = {
                                    editingMedication = null
                                    showFormSheet = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("meds_empty_add_button")
                            ) {
                                Text(stringResource(R.string.meds_add_button))
                            }
                        }
                    }
                }
            } else {
                items(items = medicationsWithStats, key = { "med_${it.medication.id}" }) { medItem ->
                    MedicationSummaryCard(
                        item = medItem,
                        onTaken = onMarkTaken,
                        onSnooze = onSnoozeTenMinutes,
                        onSkip = onSkipDose,
                        onEdit = {
                            editingMedication = medItem.medication
                            showFormSheet = true
                        },
                        onDelete = { medPendingDelete = medItem.medication }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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
                        text = stringResource(R.string.privacy_health_data_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showFormSheet) {
        MedicationEditorSheet(
            initial = editingMedication,
            onSave = { med ->
                onSaveMedication(med)
                showFormSheet = false
            },
            onDismiss = { showFormSheet = false }
        )
    }

    medPendingDelete?.let { med ->
        AlertDialog(
            onDismissRequest = { medPendingDelete = null },
            title = { Text("Delete medicine?") },
            text = { Text("Remove ${med.name} and its saved dose history from this device?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMedication(med.id)
                        medPendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_med_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { medPendingDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun DueDoseActionCard(
    item: DueMedicationDoseItem,
    onTaken: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val timeText = if (item.status == MedicationDoseStatus.SNOOZED && item.log.snoozedUntilMs != null) {
        "Snoozed until ${SafetyHealthRepository.formatMillisLabel(item.log.snoozedUntilMs)}"
    } else {
        "Scheduled ${item.log.scheduledTimeLabel}"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("due_dose_card_${item.log.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, accents.greenPrimary),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.medication.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.medication.dose} • $timeText",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accents.greenTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTaken,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("dose_taken_${item.log.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.med_action_taken), maxLines = 1)
                }
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("dose_snooze_${item.log.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accents.greenPrimary)
                ) {
                    Icon(Icons.Rounded.Snooze, contentDescription = null, modifier = Modifier.size(16.dp), tint = accents.greenPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.med_action_snooze),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(48.dp)
                        .testTag("dose_skip_${item.log.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.med_action_skip), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun MedicationSummaryCard(
    item: MedicationWithStats,
    onTaken: (MedicationDoseLogEntity) -> Unit,
    onSnooze: (MedicationDoseLogEntity) -> Unit,
    onSkip: (MedicationDoseLogEntity) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val med = item.medication
    val stats = item.stats
    val dayShortLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val daysSet = med.scheduledDays()
    val daysSummary = if (daysSet.size == 7) "Every day" else {
        daysSet.sorted().mapNotNull { dayShortLabels.getOrNull(it) }.joinToString(", ")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medication_card_${med.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${med.dose} • ${med.timesPerDay}x/day • $daysSummary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dateLine = if (med.endDateIso.isNotBlank()) {
                        "From ${med.startDateIso} to ${med.endDateIso}"
                    } else {
                        "Started ${med.startDateIso}"
                    }
                    Text(
                        text = dateLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.meds_edit_button),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete medicine",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Weekly Adherence Bar & Counts (Taken, Missed, Skipped, %)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Taken: ${stats.takenCount} • Missed: ${stats.missedCount} • Skipped: ${stats.skippedCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.meds_weekly_adherence, stats.weeklyPercentage),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accents.greenPrimary
                    )
                }

                val fraction = (stats.weeklyPercentage / 100f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    if (fraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(50))
                                .background(accents.horizontalGradient)
                        )
                    }
                }
            }

            // Today's slots summary
            if (item.todayLogs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.todayLogs.forEach { slot ->
                        val status = runCatching { MedicationDoseStatus.valueOf(slot.status) }
                            .getOrDefault(MedicationDoseStatus.PENDING)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = slot.scheduledTimeLabel,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (status == MedicationDoseStatus.PENDING || status == MedicationDoseStatus.SNOOZED) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    TextButton(
                                        onClick = { onTaken(slot) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(stringResource(R.string.med_action_taken), color = accents.greenPrimary)
                                    }
                                    TextButton(
                                        onClick = { onSnooze(slot) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Snooze")
                                    }
                                    TextButton(
                                        onClick = { onSkip(slot) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(stringResource(R.string.med_action_skip))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (status == MedicationDoseStatus.TAKEN) accents.greenTint
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MedicationEditorSheet(
    initial: MedicationEntity?,
    onSave: (MedicationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val accents = LocalAccentColors.current
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var dose by remember(initial) { mutableStateOf(initial?.dose ?: "") }
    var timesPerDay by remember(initial) { mutableIntStateOf(initial?.timesPerDay ?: 1) }
    var startDate by remember(initial) { mutableStateOf(initial?.startDateIso ?: DateUtils.todayIso()) }
    var endDate by remember(initial) { mutableStateOf(initial?.endDateIso ?: "") }
    var selectedDays by remember(initial) {
        mutableStateOf(initial?.scheduledDays() ?: setOf(0, 1, 2, 3, 4, 5, 6))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (initial == null) {
                    stringResource(R.string.meds_add_button)
                } else {
                    stringResource(R.string.meds_edit_button)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accents.greenTint,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.meds_disclaimer),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMsg = null
                },
                label = { Text(stringResource(R.string.meds_field_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("med_name_input")
            )

            OutlinedTextField(
                value = dose,
                onValueChange = {
                    dose = it
                    errorMsg = null
                },
                label = { Text(stringResource(R.string.meds_field_dose)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("med_dose_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.meds_field_times),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..4).forEach { count ->
                        FilterChip(
                            selected = timesPerDay == count,
                            onClick = { timesPerDay = count },
                            label = { Text("${count}x/day") },
                            modifier = Modifier.testTag("med_times_$count")
                        )
                    }
                }
                val previewTimes = SafetyHealthRepository.defaultDoseHoursForCount(timesPerDay)
                    .joinToString(" • ") { SafetyHealthRepository.formatHourLabel(it, 0) }
                Text(
                    text = "Schedule: $previewTimes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.meds_field_days),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayLabels.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = idx in selectedDays,
                            onClick = {
                                selectedDays = if (idx in selectedDays && selectedDays.size > 1) {
                                    selectedDays - idx
                                } else {
                                    selectedDays + idx
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text(stringResource(R.string.meds_field_start)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text(stringResource(R.string.meds_field_end)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (errorMsg != null) {
                Text(
                    text = errorMsg ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
                Button(
                    onClick = {
                        if (name.isBlank() || dose.isBlank()) {
                            errorMsg = "Please enter both medicine name and dose."
                            return@Button
                        }
                        onSave(
                            MedicationEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                dose = dose.trim(),
                                timesPerDay = timesPerDay,
                                startDateIso = startDate.trim().ifBlank { DateUtils.todayIso() },
                                endDateIso = endDate.trim(),
                                daysOfWeekCsv = selectedDays.sorted().joinToString(","),
                                createdAt = initial?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("med_save_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
