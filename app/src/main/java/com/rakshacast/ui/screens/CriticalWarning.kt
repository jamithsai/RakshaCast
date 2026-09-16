package com.rakshacast.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakshacast.ui.theme.*
import org.json.JSONArray
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun CriticalWarningScreen(
    hazardType: String,
    probability: Int,
    location: String,
    riskLevel: String,
    explanation: String? = null,
    reliefLocation: String? = null,
    affectedArea: String? = null,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B0000).copy(alpha = alpha))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Icon(imageVector = Icons.Default.Warning, contentDescription = "Critical Warning", tint = Color.White, modifier = Modifier.size(80.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("CRITICAL WEATHER WARNING", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = 1.5.sp), color = Color.White, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${probability}% PROBABILITY", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text(hazardType.replace("_", " "), style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFD700))
                Spacer(modifier = Modifier.height(4.dp))
                Text(riskLevel, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFF4500))
                Spacer(modifier = Modifier.height(8.dp))
                Text(location, style = MaterialTheme.typography.bodyLarge, color = Color.White, textAlign = TextAlign.Center)
                Text("Expected within 2 hours", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // WHY THIS WARNING? (Explanations)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Text("WHY THIS WARNING?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                
                if (explanation.isNullOrEmpty()) {
                    Text("Additional explanation unavailable", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                } else {
                    val parsedFactors = remember(explanation) {
                        val list = mutableListOf<Triple<String, String, String>>()
                        try {
                            val jsonArray = JSONArray(explanation)
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                list.add(Triple(
                                    obj.optString("factor", "Unknown"),
                                    obj.optString("status", "NORMAL"),
                                    obj.optString("explanation", "")
                                ))
                            }
                        } catch (e: Exception) {
                            // Ignored
                        }
                        list
                    }
                    
                    if (parsedFactors.isEmpty()) {
                        Text("Additional explanation unavailable", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    } else {
                        parsedFactors.forEach { (factor, status, expl) ->
                            Text(factor, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Status: $status", color = Color.White, fontSize = 12.sp)
                            Text(expl, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        // NEAREST RELIEF LOCATION
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Text("NEAREST RELIEF LOCATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                val reliefText = reliefLocation ?: "Relief location information unavailable"
                Text(reliefText, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
        
        // AFFECTED AREA
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Text("AFFECTED AREA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                val areaText = affectedArea ?: "Affected area size unavailable"
                Text(areaText, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }

        // SAFETY INSTRUCTIONS
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Text("SAFETY INSTRUCTIONS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                val instructions = listOf(
                    "Seek shelter immediately indoors.",
                    "Stay away from windows and doors.",
                    "Avoid using electrical equipment.",
                    "Do not drive or walk through flood waters."
                )
                instructions.forEach { instruction ->
                    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color.White, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = instruction, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        
        // MODEL INFORMATION
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Text("MODEL INFORMATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("--------------------------------------", color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Model: xgboost-baseline-v1.0", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                Text("Prototype: YES", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
        
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF8B0000)),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("OK, I UNDERSTAND", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
