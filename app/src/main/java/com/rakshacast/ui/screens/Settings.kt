package com.rakshacast.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rakshacast.viewmodel.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        
        item {
            SettingSwitchRow("Enable Notifications", notificationsEnabled) { notificationsEnabled = it }
            SettingSwitchRow("Enable Location Tracking", locationEnabled) { locationEnabled = it }
        }
        
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Alert Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            SettingRow("Minimum Alert Severity", "Moderate")
            SettingRow("Language", "English")
            SettingRow("Units", "Metric (mm, °C)")
        }
        
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("RakshaCast v1.0.0", style = MaterialTheme.typography.bodyMedium)
            Text("AI-Powered Hyper-Local Severe Weather Early Warning System.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
