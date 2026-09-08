package com.rakshacast.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshacast.model.*
import com.rakshacast.ui.theme.getColor
import com.rakshacast.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToMap: () -> Unit) {
    val location by viewModel.currentLocation.collectAsState()
    val weatherRisk by viewModel.weatherRisk.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            LocationHeader(location)
        }

        weatherRisk?.let { risk ->
            item {
                RiskSummarySection(risk)
            }
            item {
                ForecastTimeline(risk.forecastTimeline)
            }
            item {
                RiskExplanationSection(risk.explanationFactors)
            }
            item {
                CurrentConditionsSection(risk.indicators)
            }
            if (risk.activeAlerts.isNotEmpty()) {
                item {
                    ActiveAlertCard(risk.activeAlerts.first())
                }
            }
            item {
                Button(
                    onClick = onNavigateToMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E3A8A), // Deep institutional blue
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("View Risk Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        } ?: item {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun LocationHeader(location: Location) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("RAKSHACAST", style = MaterialTheme.typography.titleSmall, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text("Hyper-Local Severe Weather Early Warning System", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(location.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Divider(modifier = Modifier.padding(top = 16.dp), color = Color.LightGray)
    }
}

@Composable
fun RiskSummarySection(risk: WeatherRisk) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("OVERALL RISK", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(risk.overallRisk.name, style = MaterialTheme.typography.headlineMedium, color = risk.overallRisk.getColor(), fontWeight = FontWeight.ExtraBold)
                }
                Box(
                    modifier = Modifier
                        .background(risk.overallRisk.getColor().copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = risk.overallRisk.getColor(), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RiskDetailItem(label = "Primary Hazard", value = risk.primaryHazard)
                RiskDetailItem(label = "Probability", value = "${risk.probability}%", alignment = Alignment.End)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            RiskDetailItem(label = "Forecast Window", value = risk.predictionWindow)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "Conditions indicate an elevated probability of ${risk.primaryHazard.lowercase()} development.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun RiskDetailItem(label: String, value: String, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ForecastTimeline(timeline: List<ForecastPoint>) {
    SectionHeader("Forecast Timeline (2-6 Hours)")
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timeline.forEach { point ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(point.timeLabel, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(point.riskLevel.getColor())
                    )
                }
            }
        }
    }
}

@Composable
fun RiskExplanationSection(factors: List<RiskFactor>) {
    com.rakshacast.ui.components.ExplainableRiskPanel(factors = factors)
}

@Composable
fun CurrentConditionsSection(indicators: WeatherIndicators) {
    SectionHeader("Current Conditions")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ConditionIndicator("Rainfall", "${indicators.rainfallMmHr} mm/hr", Icons.Default.WaterDrop, Modifier.weight(1f))
            ConditionIndicator("Temperature", "${indicators.temperatureC}°C", Icons.Default.Thermostat, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ConditionIndicator("Moisture", indicators.moistureLevel, Icons.Default.Cloud, Modifier.weight(1f))
            ConditionIndicator("Instability", indicators.instability, Icons.Default.Air, Modifier.weight(1f))
        }
    }
}

@Composable
fun ConditionIndicator(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun ActiveAlertCard(alert: Alert) {
    SectionHeader("Active Warning")
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, alert.severity.getColor(), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = alert.severity.getColor().copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(6.dp).height(120.dp).background(alert.severity.getColor()))
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = alert.severity.getColor(), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SEVERE WEATHER ALERT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = alert.severity.getColor(), letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Expected: ${alert.expectedTimeframe}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Action: Avoid unnecessary travel and exposed areas.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4B5563),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
    )
}
