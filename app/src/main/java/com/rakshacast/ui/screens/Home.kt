package com.rakshacast.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rakshacast.model.*
import com.rakshacast.ui.components.*
import com.rakshacast.ui.theme.*
import com.rakshacast.viewmodel.MainViewModel

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToMap: () -> Unit) {
    val location by viewModel.currentLocation.collectAsState()
    val weatherRisk by viewModel.weatherRisk.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        RakshaCastHeader(
            title = "Severe Weather Intelligence",
            showLocation = true,
            locationText = location.name
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            weatherRisk?.let { risk ->
                item {
                    RiskSummarySection(risk, viewModel, onNavigateToMap)
                }
                item {
                    WeatherIndicatorsGrid(risk.indicators)
                }
                item {
                    ForecastTimeline(risk.forecastTimeline)
                }
                item {
                    RiskExplanationSection(risk.explanationFactors)
                }
                
                if (risk.activeAlerts.isNotEmpty()) {
                    item {
                        ActiveWarnings(risk.activeAlerts)
                    }
                }
                
                item {
                    PrototypeTransparencyFooter()
                }
            } ?: item {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).padding(32.dp),
                    color = NavyPrimary
                )
            }
        }
    }
}

@Composable
fun RiskSummarySection(risk: WeatherRisk, viewModel: MainViewModel, onNavigateToMap: () -> Unit) {
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    
    RakshaCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "CURRENT RISK",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralGrey,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(text = risk.overallRisk.name, color = getSeverityColor(risk.overallRisk))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${risk.probability}%",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                    color = getSeverityColor(risk.overallRisk)
                )
                Text(
                    text = "MODEL PROBABILITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeutralGrey
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = risk.primaryHazard,
            style = MaterialTheme.typography.headlineMedium,
            color = DarkCharcoal
        )
        Text(
            text = risk.predictionWindow,
            style = MaterialTheme.typography.titleLarge,
            color = NavyPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = BackgroundLight)
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Risk: ${viewModel.getRiskTrendText()}", style = MaterialTheme.typography.bodyMedium, color = DarkCharcoal)
                Text(viewModel.getLastUpdatedText(), style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
            }
            Column(horizontalAlignment = Alignment.End) {
                val freshnessColor = if (viewModel.getDataFreshness() == "Stale") StatusSevere else NavyPrimary
                Text("Data status: ${viewModel.getDataFreshness()}", style = MaterialTheme.typography.bodyMedium, color = freshnessColor)
                Text("Model: ${risk.modelVersion ?: "XGBoost baseline v1.0"}", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
            }
        }
        
        if (isRefreshing || networkStatus != "ONLINE") {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NavyPrimary, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Updating risk assessment...", style = MaterialTheme.typography.labelSmall, color = NavyPrimary)
                } else if (networkStatus != "ONLINE") {
                    Icon(Icons.Default.WifiOff, contentDescription = null, tint = StatusSevere, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(networkStatus, style = MaterialTheme.typography.labelSmall, color = StatusSevere)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNavigateToMap,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Interactive Map")
        }
    }
}

@Composable
fun WeatherIndicatorsGrid(indicators: WeatherIndicators) {
    RakshaCard {
        Text("METEOROLOGICAL INDICATORS", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            IndicatorItem("RAIN", "${indicators.rainfallMmHr} mm/hr", WeatherRain, Icons.Default.WaterDrop)
            IndicatorItem("TEMP", "${indicators.temperatureC}Â°C", WeatherTemp, Icons.Default.Thermostat)
            IndicatorItem("MOISTURE", indicators.moistureLevel, WeatherMoisture, Icons.Default.Opacity)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            IndicatorItem("INSTABILITY", indicators.instability, WeatherInstability, Icons.Default.Warning)
            IndicatorItem("CLOUD", indicators.cloudCondition, WeatherCloud, Icons.Default.FilterDrama)
        }
    }
}

@Composable
fun IndicatorItem(label: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MutedText)
    }
}

@Composable
fun ForecastTimeline(timeline: List<ForecastPoint>) {
    RakshaCard {
        Text("FORECAST TIMELINE", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            timeline.forEach { point ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(point.timeLabel, style = MaterialTheme.typography.labelSmall, color = MutedText)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = getSeverityColor(point.riskLevel),
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun RiskExplanationSection(factors: List<RiskFactor>) {
    RakshaCard {
        Text("WHY IS THE RISK HIGH?", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
        Spacer(modifier = Modifier.height(12.dp))
        factors.forEach { factor ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.Top) {
                val color = if (factor.status.contains("ELEVATED", true) || factor.status.contains("HIGH", true)) WeatherInstability else WeatherRain
                StatusBadge(text = factor.status, color = color, modifier = Modifier.width(100.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(factor.factor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text(factor.trend.name, style = MaterialTheme.typography.labelSmall, color = NavyPrimary)
                    }
                    Text(factor.explanation, style = MaterialTheme.typography.bodyMedium, color = MutedText, modifier = Modifier.padding(top = 4.dp))
                    Text("Importance: ${factor.importance}", style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(top = 4.dp))
                }
            }
            HorizontalDivider(color = BackgroundLight)
        }
    }
}

@Composable
fun ActiveWarnings(alerts: List<Alert>) {
    Column {
        alerts.forEach { alert ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)), // Very light red
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusSevere, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ACTIVE WARNING", style = MaterialTheme.typography.labelSmall, color = StatusSevere, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(alert.hazard, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Expected: ${alert.expectedTimeframe}", style = MaterialTheme.typography.bodyMedium, color = StatusSevere)
                    Text(alert.message, style = MaterialTheme.typography.bodyMedium, color = DarkCharcoal, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

@Composable
fun PrototypeTransparencyFooter() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = NeutralGrey, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "Prototype assessment based on simulated risk indicators",
            style = MaterialTheme.typography.labelSmall,
            color = NeutralGrey
        )
    }
}


