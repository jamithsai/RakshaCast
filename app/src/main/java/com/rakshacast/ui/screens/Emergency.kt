package com.rakshacast.ui.screens

import androidx.compose.foundation.background
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
import com.rakshacast.model.*
import com.rakshacast.viewmodel.MainViewModel

@Composable
fun EmergencyScreen(viewModel: MainViewModel) {
    val location by viewModel.currentLocation.collectAsState()
    val shelters by viewModel.shelters.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val safetyGuidance by viewModel.safetyGuidance.collectAsState()
    
    var showSOSDialog by remember { mutableStateOf(false) }
    var selectedHazardGuidance by remember { mutableStateOf("Thunderstorm") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                EmergencyHeader(location.name)
            }

            item {
                SOSCard(onSOSClick = { showSOSDialog = true })
            }

            item {
                SectionTitle("Emergency Contacts")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        emergencyContacts.forEachIndexed { index, contact ->
                            EmergencyContactRow(contact)
                            if (index < emergencyContacts.size - 1) {
                                HorizontalDivider(color = Color(0xFFF3F4F6))
                            }
                        }
                    }
                }
            }

            item {
                SectionTitle("Nearby Shelters & Safe Zones")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(shelters) { shelter ->
                        ShelterCard(shelter)
                    }
                }
            }

            item {
                SectionTitle("Hazard Safety Guidance")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ScrollableHazardSelection(
                            hazards = safetyGuidance.map { it.hazardType },
                            selected = selectedHazardGuidance,
                            onSelect = { selectedHazardGuidance = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val currentGuidance = safetyGuidance.find { it.hazardType == selectedHazardGuidance }
                        currentGuidance?.instructions?.forEach { instruction ->
                            SafetyInstructionItem(instruction)
                        }
                    }
                }
            }

            item {
                SectionTitle("General Preparedness")
                PreparednessCard()
            }
        }

        if (showSOSDialog) {
            SOSPrototypeDialog(onDismiss = { showSOSDialog = false })
        }
    }
}

@Composable
fun EmergencyHeader(locationName: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("EMERGENCY & RESPONSE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F), letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Current Location: $locationName", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = Color.LightGray)
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF4B5563),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun SOSCard(onSOSClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onSOSClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(64.dp).background(Color(0xFFDC2626), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("SOS", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Emergency Assistance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                Text("Tap to request emergency rescue and share your location.", style = MaterialTheme.typography.bodySmall, color = Color(0xFFB91C1C))
            }
        }
    }
}

@Composable
fun SOSPrototypeDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Emergency Assistance", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
            }
        },
        text = {
            Column {
                Text("This is a prototype feature.", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("In the final system, this will instantly connect to emergency services and share your precise location coordinates. No actual emergency call is being placed right now.")
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Understood (Demo)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
fun EmergencyContactRow(contact: EmergencyContact) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFEFF6FF), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(contact.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(contact.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
        Text(contact.number, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
    }
}

@Composable
fun ShelterCard(shelter: Shelter) {
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(24.dp))
                Text("${shelter.distanceKm} km", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(shelter.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Capacity: ${shelter.capacity}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                ShelterStatusIndicator(shelter.status, shelter.availabilityText)
            }
        }
    }
}

@Composable
fun ShelterStatusIndicator(status: ShelterStatus, text: String) {
    val color = when(status) {
        ShelterStatus.AVAILABLE -> Color(0xFF059669)
        ShelterStatus.LIMITED -> Color(0xFFD97706)
        ShelterStatus.FULL, ShelterStatus.CLOSED -> Color(0xFFDC2626)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollableHazardSelection(hazards: List<String>, selected: String, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(hazards) { hazard ->
            FilterChip(
                selected = hazard == selected,
                onClick = { onSelect(hazard) },
                label = { Text(hazard, style = MaterialTheme.typography.bodySmall) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF1E3A8A),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun SafetyInstructionItem(instruction: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(instruction, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PreparednessCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SafetyInstructionItem("Keep mobile devices charged and emergency contacts saved.")
            SafetyInstructionItem("Prepare a basic disaster kit (water, medication, flashlight).")
            SafetyInstructionItem("Familiarize yourself with local evacuation routes.")
            SafetyInstructionItem("Monitor RakshaCast alerts continuously during active weather.")
        }
    }
}
