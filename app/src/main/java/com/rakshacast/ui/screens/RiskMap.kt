package com.rakshacast.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.rakshacast.model.RiskLevel
import com.rakshacast.model.RiskZone
import com.rakshacast.ui.theme.getColor
import com.rakshacast.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.CopyrightOverlay

@SuppressLint("MissingPermission")
@Composable
fun RiskMapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            locationPermissionGranted = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = GeoPoint(it.latitude, it.longitude)
                }
            }
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    val defaultDemoLocation = GeoPoint(17.3850, 78.4867)
    var mapCenter by remember { mutableStateOf(defaultDemoLocation) }
    var zoomLevel by remember { mutableStateOf(10.0) }
    
    // We need a ref to the map view to animate camera programmatically
    var mapRef by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            mapCenter = it
            mapRef?.controller?.animateTo(it, 10.0, 1000L)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val allRiskZones by viewModel.riskZones.collectAsState()
    var selectedHazard by remember { mutableStateOf("All Hazards") }
    var selectedForecastHour by remember { mutableStateOf(0) }
    var selectedZone by remember { mutableStateOf<RiskZone?>(null) }

    val hazards = listOf("All Hazards", "Thunderstorm", "Cloudburst", "Flash Flood")
    val forecastLabels = mapOf(0 to "NOW", 1 to "+1H", 2 to "+2H", 3 to "+3H", 4 to "+4H", 5 to "+5H", 6 to "+6H")

    val visibleZones = allRiskZones.filter { zone ->
        (selectedHazard == "All Hazards" || zone.hazard == selectedHazard) &&
                zone.forecastHour == selectedForecastHour
    }

    LaunchedEffect(selectedHazard, selectedForecastHour) {
        if (!visibleZones.contains(selectedZone)) {
            selectedZone = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(zoomLevel)
                    controller.setCenter(mapCenter)
                    
                    val copyrightOverlay = CopyrightOverlay(ctx)
                    overlays.add(copyrightOverlay)
                    
                    mapRef = this
                }
            },
            update = { mapView ->
                // Clear all except copyright overlay
                val copyright = mapView.overlays.find { it is CopyrightOverlay }
                mapView.overlays.clear()
                if (copyright != null) mapView.overlays.add(copyright)

                // Add User Marker
                userLocation?.let { loc ->
                    val userMarker = Marker(mapView)
                    userMarker.position = loc
                    userMarker.title = "Current Location"
                    userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    mapView.overlays.add(userMarker)
                }

                // Add Risk Zones
                visibleZones.forEach { zone ->
                    val center = GeoPoint(zone.centerLat, zone.centerLng)
                    val isSelected = zone == selectedZone
                    
                    val polygon = Polygon()
                    polygon.points = Polygon.pointsAsCircle(center, zone.radiusKm * 1000.0)
                    
                    val composeColor = zone.riskLevel.getColor()
                    val androidColor = android.graphics.Color.argb(
                        255,
                        (composeColor.red * 255).toInt(),
                        (composeColor.green * 255).toInt(),
                        (composeColor.blue * 255).toInt()
                    )
                    
                    polygon.fillPaint.color = androidColor
                    polygon.fillPaint.alpha = if (isSelected) 128 else 76
                    polygon.outlinePaint.color = androidColor
                    polygon.outlinePaint.strokeWidth = if (isSelected) 8f else 4f
                    
                    polygon.setOnClickListener { p, mv, eventInfo ->
                        selectedZone = zone
                        mv.controller.animateTo(center)
                        true
                    }
                    mapView.overlays.add(polygon)
                }
                
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Controls
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            MapControlsCard(
                hazards = hazards,
                selectedHazard = selectedHazard,
                onHazardSelected = { selectedHazard = it },
                forecastLabels = forecastLabels,
                selectedForecastHour = selectedForecastHour,
                onForecastSelected = { selectedForecastHour = it }
            )
        }

        // Floating Recenter Button
        FloatingMapTools(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            onRecenter = {
                val target = userLocation ?: defaultDemoLocation
                mapRef?.controller?.animateTo(target, 10.0, 500L)
            }
        )

        // Bottom Legend
        MapLegend(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = if (selectedZone == null) 80.dp else 0.dp)
        )

        // Bottom Selected Zone Panel
        if (selectedZone != null) {
            SelectedZonePanel(
                zone = selectedZone!!,
                userLocation = userLocation,
                onClose = { selectedZone = null },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    }
}

@Composable
fun MapControlsCard(
    hazards: List<String>,
    selectedHazard: String,
    onHazardSelected: (String) -> Unit,
    forecastLabels: Map<Int, String>,
    selectedForecastHour: Int,
    onForecastSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Hazard Filter", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(hazards) { hazard ->
                    FilterChip(
                        selected = hazard == selectedHazard,
                        onClick = { onHazardSelected(hazard) },
                        label = { Text(hazard, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1E3A8A),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Forecast Horizon", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(forecastLabels.keys.toList()) { hour ->
                    FilterChip(
                        selected = hour == selectedForecastHour,
                        onClick = { onForecastSelected(hour) },
                        label = { Text(forecastLabels[hour]!!, style = MaterialTheme.typography.bodySmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF047857),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingMapTools(modifier: Modifier = Modifier, onRecenter: () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        IconButton(onClick = onRecenter) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Recenter",
                tint = Color(0xFF4B5563)
            )
        }
    }
}

@Composable
fun MapLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(bottom = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("RISK LEVEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LegendItemRow("Low", RiskLevel.LOW.getColor())
            LegendItemRow("Moderate", RiskLevel.MODERATE.getColor())
            LegendItemRow("High", RiskLevel.HIGH.getColor())
            LegendItemRow("Severe", RiskLevel.SEVERE.getColor())
            LegendItemRow("Extreme", RiskLevel.EXTREME.getColor())
            Spacer(modifier = Modifier.height(8.dp))
            Text("PROTOTYPE DEMO DATA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            Text("© OpenStreetMap contributors", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontSize = 9.sp)
        }
    }
}

@Composable
fun LegendItemRow(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SelectedZonePanel(zone: RiskZone, userLocation: GeoPoint?, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val affectedAreaKm2 = Math.PI * zone.radiusKm * zone.radiusKm
    val formattedArea = String.format("%.1f", affectedAreaKm2)
    
    var distanceText = "Unknown"
    if (userLocation != null) {
        val results = FloatArray(1)
        Location.distanceBetween(userLocation.latitude, userLocation.longitude, zone.centerLat, zone.centerLng, results)
        val distanceKm = results[0] / 1000f
        distanceText = String.format("%.1f km", distanceKm)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(zone.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(zone.riskLevel.getColor()))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${zone.hazard} - ${zone.riskLevel.name}", style = MaterialTheme.typography.labelMedium, color = zone.riskLevel.getColor(), fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Probability", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${zone.probability}%", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expected Window", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(zone.expectedWindow, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Radius", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${zone.radiusKm} km", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Approx. affected area", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("$formattedArea km²", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Distance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(distanceText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            com.rakshacast.ui.components.ExplainableRiskPanel(factors = zone.explanationFactors)
            
            Spacer(modifier = Modifier.height(8.dp))
            Text("PROTOTYPE ZONE - Not an official warning", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
