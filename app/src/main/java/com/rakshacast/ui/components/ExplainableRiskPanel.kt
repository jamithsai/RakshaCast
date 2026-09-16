package com.rakshacast.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rakshacast.model.RiskFactor
import com.rakshacast.ui.theme.*

@Composable
fun ExplainableRiskPanel(factors: List<RiskFactor>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("RISK FACTORS", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
        Spacer(modifier = Modifier.height(12.dp))
        factors.forEach { factor ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                val color = if (factor.status.contains("ELEVATED", true) || factor.status.contains("HIGH", true)) WeatherInstability else WeatherRain
                StatusBadge(text = factor.status, color = color, modifier = Modifier.width(90.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(factor.factor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Text(factor.trend.name, style = MaterialTheme.typography.labelSmall, color = NavyPrimary)
                    }
                    Text(factor.explanation, style = MaterialTheme.typography.bodyMedium, color = MutedText, modifier = Modifier.padding(top = 4.dp))
                    Text("Importance: ${factor.importance}", style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

