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
import com.rakshacast.model.EmergencyContact
import com.rakshacast.model.SafetyGuidance
import com.rakshacast.model.Shelter
import com.rakshacast.model.ShelterStatus
import com.rakshacast.ui.components.*
import com.rakshacast.ui.theme.*
import com.rakshacast.viewmodel.MainViewModel

@Composable
fun EmergencyScreen(viewModel: MainViewModel) {
    val contacts by viewModel.emergencyContacts.collectAsState()
    val shelters by viewModel.shelters.collectAsState()
    val safetyGuidance by viewModel.safetyGuidance.collectAsState()
    
    var showSosDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        RakshaCastHeader(title = "Emergency & Response")
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(
                    onClick = { showSosDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusExtreme),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("INITIATE SOS (DEMO)", style = MaterialTheme.typography.titleLarge)
                }
            }
            
            item { Text("EMERGENCY NUMBERS — VERIFY LOCALLY BEFORE DEPLOYMENT", style = MaterialTheme.typography.labelSmall, color = NeutralGrey) }
            items(contacts) { contact ->
                ContactCard(contact)
            }
            
            item { Text("NEARBY RELIEF CENTRES", style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(top = 8.dp)) }
            items(shelters) { shelter ->
                ShelterCard(shelter)
            }
            
            item { Text("SAFETY GUIDANCE", style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(top = 8.dp)) }
            items(safetyGuidance) { guidance ->
                GuidanceCard(guidance)
            }
        }
    }

    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = { Text("Prototype SOS Activated") },
            text = { Text("This is a safe prototype demonstration. No actual emergency services will be contacted.") },
            confirmButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("ACKNOWLEDGE", color = NavyPrimary)
                }
            }
        )
    }
}

@Composable
fun ContactCard(contact: EmergencyContact) {
    RakshaCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(contact.name, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
                Text(contact.number, style = MaterialTheme.typography.headlineMedium, color = NavyPrimary)
                Text("Prototype contact data", style = MaterialTheme.typography.labelSmall, color = NeutralGrey, modifier = Modifier.padding(top = 4.dp))
            }
            Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary)
        }
    }
}

@Composable
fun ShelterCard(shelter: Shelter) {
    RakshaCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(shelter.name, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
                    Text("${shelter.distanceKm} km away", style = MaterialTheme.typography.bodyMedium, color = NavyPrimary, fontWeight = FontWeight.SemiBold)
                }
                val isVerified = shelter.name.contains("GHMC") || shelter.name.contains("School") || shelter.name.contains("Community")
                if (isVerified) {
                    StatusBadge(text = "OFFICIAL RELIEF LOCATION", color = NavyPrimary)
                } else {
                    StatusBadge(text = "PROTOTYPE", color = NeutralGrey)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("STATUS:", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
            Text("Availability not verified (Live shelter status unavailable)", style = MaterialTheme.typography.bodyMedium, color = StatusSevere)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("SOURCE:", style = MaterialTheme.typography.labelSmall, color = NeutralGrey)
            val isVerifiedSource = shelter.name.contains("GHMC") || shelter.name.contains("School") || shelter.name.contains("Community")
            Text(if (isVerifiedSource) "GHMC / DDMA Disaster Management" else "Demo Location Data", style = MaterialTheme.typography.bodyMedium, color = MutedText)
        }
    }
}

@Composable
fun GuidanceCard(guidance: SafetyGuidance) {
    RakshaCard {
        Text(guidance.title, style = MaterialTheme.typography.titleLarge, color = DarkCharcoal)
        Spacer(modifier = Modifier.height(12.dp))
        guidance.instructions.forEach { step ->
            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Info, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(step, style = MaterialTheme.typography.bodyMedium, color = DarkCharcoal)
            }
        }
    }
}


