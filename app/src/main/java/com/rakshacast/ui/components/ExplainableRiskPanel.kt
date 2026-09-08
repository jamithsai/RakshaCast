package com.rakshacast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshacast.model.RiskFactor
import com.rakshacast.model.RiskTrend

@Composable
fun ExplainableRiskPanel(factors: List<RiskFactor>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, contentDescription = "AI Risk", tint = Color(0xFF1E3A8A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WHY IS THE RISK HIGH?",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B5563),
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            factors.forEachIndexed { index, factor ->
                RiskFactorExplanationRow(factor)
                if (index < factors.size - 1) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.background(Color(0xFFF3F4F6), RoundedCornerShape(4.dp)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Prototype assessment based on simulated risk indicators.", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun RiskFactorExplanationRow(factor: RiskFactor) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(factor.factor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(factor.status, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = when(factor.trend) {
                        RiskTrend.RISING -> Icons.Default.TrendingUp
                        RiskTrend.FALLING -> Icons.Default.TrendingDown
                        RiskTrend.STABLE -> Icons.Default.TrendingFlat
                    },
                    contentDescription = factor.trend.name,
                    tint = Color(0xFF1E3A8A),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Importance visualization bar
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE5E7EB))) {
            Box(modifier = Modifier.fillMaxWidth(fraction = factor.importance).fillMaxHeight().background(Color(0xFF1E3A8A)))
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Text(factor.explanation, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}
