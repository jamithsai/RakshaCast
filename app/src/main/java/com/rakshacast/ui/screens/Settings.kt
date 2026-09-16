package com.rakshacast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rakshacast.ui.components.RakshaCastHeader
import com.rakshacast.ui.theme.*
import com.rakshacast.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    
    val pushNotificationsEnabled by viewModel.settingsRepository.pushNotificationsEnabled.collectAsState(initial = true)
    val locationServicesEnabled by viewModel.settingsRepository.locationServicesEnabled.collectAsState(initial = true)
    val selectedSeverity by viewModel.settingsRepository.minSeverity.collectAsState(initial = "HIGH / SEVERE")
    val selectedLanguage by viewModel.settingsRepository.language.collectAsState(initial = "English (India)")
    val selectedUnits by viewModel.settingsRepository.units.collectAsState(initial = "Metric (°C, mm, km)")
    val selectedDemoScenario by viewModel.settingsRepository.demoScenario.collectAsState(initial = "Live / Remote")

    var showSeverityDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }
    var showDemoScenarioDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        RakshaCastHeader(title = "Settings")
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsGroup(title = "PREFERENCES") {
                    SettingsToggleRow(
                        icon = Icons.Default.Notifications, 
                        title = "Push Notifications", 
                        subtitle = "Local prototype simulation only",
                        checked = pushNotificationsEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch { viewModel.settingsRepository.setPushNotifications(checked) }
                        }
                    )
                    HorizontalDivider(color = BackgroundLight)
                    SettingsToggleRow(
                        icon = Icons.Default.LocationOn, 
                        title = "Location Services", 
                        subtitle = "Uses precise location if granted",
                        checked = locationServicesEnabled,
                        onCheckedChange = { checked ->
                            coroutineScope.launch { viewModel.settingsRepository.setLocationServices(checked) }
                        }
                    )
                    HorizontalDivider(color = BackgroundLight)
                    SettingsActionRow(
                        icon = Icons.Default.Warning,
                        title = "Minimum Alert Severity",
                        subtitle = selectedSeverity,
                        onClick = { showSeverityDialog = true }
                    )
                    HorizontalDivider(color = BackgroundLight)
                    SettingsActionRow(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = selectedLanguage,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = BackgroundLight)
                    SettingsActionRow(
                        icon = Icons.Default.Speed,
                        title = "Units",
                        subtitle = selectedUnits,
                        onClick = { showUnitsDialog = true }
                    )
                }
            }
            item {
                SettingsGroup(title = "DEMONSTRATION SCENARIOS") {
                    SettingsActionRow(
                        icon = Icons.Default.Science,
                        title = "Demo Scenario",
                        subtitle = selectedDemoScenario,
                        onClick = { showDemoScenarioDialog = true }
                    )
                }
            }
            item {
                SettingsGroup(title = "DATA & PRIVACY") {
                    SettingsActionRow(icon = Icons.Default.Storage, title = "Data Source", subtitle = "Remote API / Prototype Mode", onClick = {})
                }
            }
            item {
                SettingsGroup(title = "ABOUT RAKSHACAST") {
                    SettingsActionRow(icon = Icons.Default.Info, title = "Version", subtitle = "2.1 Prototype (SIH 2026)", onClick = {})
                }
            }
        }
    }

    if (showSeverityDialog) {
        OptionsDialog(
            title = "Minimum Alert Severity",
            options = listOf("ALL", "MODERATE OR HIGHER", "HIGH / SEVERE", "EXTREME ONLY"),
            selectedOption = selectedSeverity,
            onOptionSelected = { 
                coroutineScope.launch { viewModel.settingsRepository.setMinSeverity(it) }
                showSeverityDialog = false 
            },
            onDismiss = { showSeverityDialog = false }
        )
    }

    if (showLanguageDialog) {
        OptionsDialog(
            title = "Language",
            options = listOf("English (India)", "Hindi", "Telugu"),
            selectedOption = selectedLanguage,
            onOptionSelected = { 
                coroutineScope.launch { viewModel.settingsRepository.setLanguage(it) }
                showLanguageDialog = false 
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showUnitsDialog) {
        OptionsDialog(
            title = "Units",
            options = listOf("Metric (°C, mm, km)", "Imperial (°F, in, mi)"),
            selectedOption = selectedUnits,
            onOptionSelected = { 
                coroutineScope.launch { viewModel.settingsRepository.setUnits(it) }
                showUnitsDialog = false 
            },
            onDismiss = { showUnitsDialog = false }
        )
    }
    
    if (showDemoScenarioDialog) {
        OptionsDialog(
            title = "Demo Scenario",
            options = listOf("Live / Remote", "Severe Thunderstorm", "Cloudburst", "Flash Flood", "Moderate Weather", "Low Risk"),
            selectedOption = selectedDemoScenario,
            onOptionSelected = { 
                coroutineScope.launch { viewModel.settingsRepository.setDemoScenario(it) }
                showDemoScenarioDialog = false 
            },
            onDismiss = { showDemoScenarioDialog = false }
        )
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(bottom = 8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NavyPrimary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = NavyPrimary))
    }
}

@Composable
fun SettingsActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = NavyPrimary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
    }
}

@Composable
fun OptionsDialog(title: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onOptionSelected(option) }.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = { onOptionSelected(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = NavyPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = NavyPrimary)
            }
        }
    )
}
