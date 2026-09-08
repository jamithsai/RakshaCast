package com.rakshacast.ui.theme

import androidx.compose.ui.graphics.Color
import com.rakshacast.model.RiskLevel

fun RiskLevel.getColor(): Color {
    return when(this) {
        RiskLevel.LOW -> Color(0xFF4CAF50)
        RiskLevel.MODERATE -> Color(0xFFFF9800)
        RiskLevel.HIGH -> Color(0xFFFF5722)
        RiskLevel.SEVERE -> Color(0xFFD32F2F)
        RiskLevel.EXTREME -> Color(0xFFB71C1C)
    }
}
