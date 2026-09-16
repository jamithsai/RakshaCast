package com.rakshacast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rakshacast.model.RiskLevel
import com.rakshacast.ui.theme.*

// 1. App Brand Header
@Composable
fun RakshaCastHeader(
    title: String,
    subtitle: String? = null,
    showLocation: Boolean = false,
    locationText: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Logo",
                tint = SurfaceWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RAKSHA CAST",
                style = MaterialTheme.typography.displayLarge,
                color = SurfaceWhite
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = SurfaceWhite
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = SurfaceWhite.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (showLocation && locationText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = WeatherMoisture,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WeatherMoisture,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// 2. Standard Card
@Composable
fun RakshaCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

// 3. Status Badge
@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// 4. Utility to map RiskLevel to Color
fun getSeverityColor(level: RiskLevel): Color {
    return when (level) {
        RiskLevel.LOW -> StatusLow
        RiskLevel.MODERATE -> StatusModerate
        RiskLevel.HIGH -> StatusHigh
        RiskLevel.SEVERE -> StatusSevere
        RiskLevel.EXTREME -> StatusExtreme
        else -> StatusElevated
    }
}
