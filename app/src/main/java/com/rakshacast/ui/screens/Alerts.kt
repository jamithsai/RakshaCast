package com.rakshacast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rakshacast.model.Alert
import com.rakshacast.model.AlertStatus
import com.rakshacast.ui.components.*
import com.rakshacast.ui.theme.*
import com.rakshacast.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(viewModel: MainViewModel) {
    val alerts by viewModel.alerts.collectAsState()
    var selectedAlert by remember { mutableStateOf<Alert?>(null) }
    
    val activeAlerts = alerts.filter { it.status == AlertStatus.ACTIVE }
    val upcomingAlerts = alerts.filter { it.status == AlertStatus.UPCOMING }
    
    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        RakshaCastHeader(title = "Alert Center")
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (activeAlerts.isNotEmpty()) {
                item {
                    Text("ACTIVE WARNINGS", style = MaterialTheme.typography.labelSmall, color = StatusSevere)
                }
                items(activeAlerts) { alert ->
                    AlertCard(alert = alert, isHero = true, onClick = { selectedAlert = alert })
                }
            }
            
            if (upcomingAlerts.isNotEmpty()) {
                item {
                    Text("UPCOMING ADVISORIES", style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(top = 8.dp))
                }
                items(upcomingAlerts) { alert ->
                    AlertCard(alert = alert, isHero = false, onClick = { selectedAlert = alert })
                }
            }
        }
    }
    
    if (selectedAlert != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedAlert = null },
            containerColor = SurfaceWhite
        ) {
            AlertDetailSheet(alert = selectedAlert!!)
        }
    }
}

@Composable
fun AlertCard(alert: Alert, isHero: Boolean, onClick: () -> Unit) {
    val containerColor = if (isHero) Color(0xFFFFF5F5) else SurfaceWhite
    val borderColor = if (isHero) StatusExtreme.copy(alpha = 0.2f) else BackgroundLight
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatusBadge(
                    text = alert.severity.name, 
                    color = getSeverityColor(alert.severity)
                )
                Text(alert.timestamp, style = MaterialTheme.typography.labelSmall, color = MutedText)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(alert.hazard, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
            Spacer(modifier = Modifier.height(4.dp))
            Text(alert.expectedTimeframe, style = MaterialTheme.typography.bodyMedium, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = DarkCharcoal)
        }
    }
}

@Composable
fun AlertDetailSheet(alert: Alert) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            StatusBadge(text = alert.severity.name, color = getSeverityColor(alert.severity))
            Spacer(modifier = Modifier.height(4.dp))
            Text("${alert.probability}% Probability", style = MaterialTheme.typography.titleLarge, color = NavyPrimary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(alert.hazard, style = MaterialTheme.typography.headlineMedium, color = DarkCharcoal)
        Text(alert.location.name, style = MaterialTheme.typography.bodyMedium, color = MutedText)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("RECOMMENDED ACTIONS", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
        Spacer(modifier = Modifier.height(8.dp))
        alert.recommendedActions.forEach { action ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSafe, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(action, style = MaterialTheme.typography.bodyMedium, color = DarkCharcoal)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

