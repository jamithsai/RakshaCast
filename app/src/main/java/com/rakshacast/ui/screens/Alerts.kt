package com.rakshacast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshacast.model.Alert
import com.rakshacast.model.AlertStatus
import com.rakshacast.ui.theme.getColor
import com.rakshacast.viewmodel.MainViewModel

@Composable
fun AlertsScreen(viewModel: MainViewModel) {
    val alerts by viewModel.alerts.collectAsState()
    val location by viewModel.currentLocation.collectAsState()
    
    var selectedHazardFilter by remember { mutableStateOf("All Hazards") }
    var selectedStatusFilter by remember { mutableStateOf("All Status") }
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }

    val hazardFilters = listOf("All Hazards", "Severe Thunderstorm", "Cloudburst", "Flash Flood")
    val statusFilters = listOf("All Status", "Active", "Upcoming", "Resolved")

    val filteredAlerts = alerts.filter {
        (selectedHazardFilter == "All Hazards" || it.hazard == selectedHazardFilter) &&
        (selectedStatusFilter == "All Status" || it.status.name.equals(selectedStatusFilter, ignoreCase = true))
    }

    val activeAlerts = filteredAlerts.filter { it.status == AlertStatus.ACTIVE }
    val upcomingAlerts = filteredAlerts.filter { it.status == AlertStatus.UPCOMING }
    val historyAlerts = filteredAlerts.filter { it.status == AlertStatus.RESOLVED }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AlertsHeader(location.name, alerts.count { !it.isRead })
            }

            item {
                AlertFilters(
                    hazardFilters, selectedHazardFilter, { selectedHazardFilter = it },
                    statusFilters, selectedStatusFilter, { selectedStatusFilter = it }
                )
            }

            if (activeAlerts.isNotEmpty()) {
                item { SectionHeader("Active Warnings") }
                items(activeAlerts) { alert ->
                    ActiveAlertCardFull(alert) {
                        selectedAlert = it
                        viewModel.markAlertAsRead(it.id)
                    }
                }
            }

            if (upcomingAlerts.isNotEmpty()) {
                item { SectionHeader("Upcoming Risks") }
                items(upcomingAlerts) { alert ->
                    UpcomingAlertCard(alert) {
                        selectedAlert = it
                        viewModel.markAlertAsRead(it.id)
                    }
                }
            }

            if (historyAlerts.isNotEmpty()) {
                item { SectionHeader("Alert History") }
                items(historyAlerts) { alert ->
                    HistoryAlertRow(alert) {
                        selectedAlert = it
                    }
                }
            }
            
            if (filteredAlerts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No alerts match the selected filters.", color = Color.Gray)
                    }
                }
            }
        }

        if (selectedAlert != null) {
            AlertDetailSheet(
                alert = selectedAlert!!,
                onClose = { selectedAlert = null },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun AlertsHeader(locationName: String, unreadCount: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("WEATHER ALERTS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), letterSpacing = 1.sp)
                Text("Severe weather warnings for your area", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = if(unreadCount > 0) Color(0xFFD32F2F) else Color.Gray)
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.background(Color(0xFFD32F2F), CircleShape).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("$unreadCount NEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(locationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = Color.LightGray)
    }
}

@Composable
fun AlertFilters(
    hazards: List<String>, selectedHazard: String, onHazardSelect: (String) -> Unit,
    statuses: List<String>, selectedStatus: String, onStatusSelect: (String) -> Unit
) {
    Column {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(hazards) { hazard ->
                FilterChip(
                    selected = hazard == selectedHazard,
                    onClick = { onHazardSelect(hazard) },
                    label = { Text(hazard, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF1E3A8A), selectedLabelColor = Color.White)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statuses) { status ->
                FilterChip(
                    selected = status == selectedStatus,
                    onClick = { onStatusSelect(status) },
                    label = { Text(status, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF047857), selectedLabelColor = Color.White)
                )
            }
        }
    }
}

@Composable
fun ActiveAlertCardFull(alert: Alert, onClick: (Alert) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (!alert.isRead) 2.dp else 1.dp, if (!alert.isRead) alert.severity.getColor() else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick(alert) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = alert.severity.getColor().copy(alpha = 0.05f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(8.dp).fillMaxHeight().defaultMinSize(minHeight = 140.dp).background(alert.severity.getColor()))
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = alert.severity.getColor(), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(alert.hazard.uppercase(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = alert.severity.getColor())
                    }
                    if (!alert.isRead) {
                        Box(modifier = Modifier.background(alert.severity.getColor(), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("NEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AlertDetailItem("Severity", alert.severity.name, alert.severity.getColor())
                    AlertDetailItem("Probability", "${alert.probability}%", MaterialTheme.colorScheme.onSurface)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Expected: ${alert.expectedTimeframe}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Location: ${alert.location.name}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap for detailed actions and risk explanation", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun UpcomingAlertCard(alert: Alert, onClick: (Alert) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(alert) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(alert.severity.getColor()))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.hazard, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(alert.expectedTimeframe, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text("${alert.probability}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = alert.severity.getColor())
        }
    }
}

@Composable
fun HistoryAlertRow(alert: Alert, onClick: (Alert) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(alert) }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(alert.hazard, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
            Text("${alert.timestamp} • ${alert.location.name}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Box(modifier = Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
            Text(alert.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        }
    }
}

@Composable
fun AlertDetailItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun AlertDetailSheet(alert: Alert, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().fillMaxHeight(0.9f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(modifier = Modifier.fillMaxWidth().background(alert.severity.getColor().copy(alpha = 0.1f)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(alert.hazard.uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = alert.severity.getColor())
                    Text(alert.location.name, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AlertDetailItem("Probability", "${alert.probability}%", MaterialTheme.colorScheme.onSurface)
                        AlertDetailItem("Expected", alert.expectedTimeframe, MaterialTheme.colorScheme.onSurface)
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }
            
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                item {
                    com.rakshacast.ui.components.ExplainableRiskPanel(factors = alert.explanationFactors)
                }
                
                item {
                    SectionHeader("Recommended Actions")
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = alert.severity.getColor().copy(alpha = 0.05f)), border = androidx.compose.foundation.BorderStroke(1.dp, alert.severity.getColor().copy(alpha = 0.2f))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (alert.recommendedActions.isEmpty()) {
                                Text("No specific actions required.", style = MaterialTheme.typography.bodyMedium)
                            } else {
                                alert.recommendedActions.forEach { action ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = alert.severity.getColor(), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(action, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Text("Last updated: ${alert.timestamp}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
