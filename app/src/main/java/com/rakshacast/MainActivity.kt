package com.rakshacast

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.rakshacast.navigation.RakshaCastApp
import com.rakshacast.navigation.Screen
import com.rakshacast.network.ApiClient
import com.rakshacast.ui.theme.RakshaCastTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
            fetchFcmToken()
        } else {
            Log.d("MainActivity", "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val alertId = intent.getStringExtra("alertId")
        val isCritical = intent.getBooleanExtra("isCritical", false)
        
        var startDestination = Screen.Home.route
        if (isCritical) {
            startDestination = Screen.Critical.route
        } else if (alertId != null) {
            startDestination = Screen.Alerts.route
        }
        
        val criticalHazard = intent.getStringExtra("hazardType") ?: "SEVERE WEATHER"
        val criticalProb = intent.getIntExtra("probability", 80)
        val criticalLoc = intent.getStringExtra("location") ?: "Affected Area"
        val criticalRiskLevel = intent.getStringExtra("riskLevel") ?: "SEVERE"
        val criticalExplanation = intent.getStringExtra("explanations")
        
        val sName = intent.getStringExtra("shelterName")
        val sDist = intent.getStringExtra("shelterDistanceKm")
        val sVer = intent.getStringExtra("shelterVerification")
        val sAvail = intent.getStringExtra("shelterAvailability")
        val criticalRelief = if (sName != null) {
            "$sName\nDistance: $sDist km\n$sVer\n$sAvail"
        } else null
        
        val rKm = intent.getStringExtra("radiusKm")
        val aKm = intent.getStringExtra("affectedAreaKm2")
        val criticalArea = if (rKm != null && aKm != null) {
            "Radius: $rKm km\nArea: $aKm km²"
        } else null

        setContent {
            RakshaCastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RakshaCastApp(
                        startDestination = startDestination,
                        criticalHazard = criticalHazard,
                        criticalProb = criticalProb,
                        criticalLoc = criticalLoc,
                        criticalRiskLevel = criticalRiskLevel,
                        criticalExplanation = criticalExplanation,
                        criticalRelief = criticalRelief,
                        criticalArea = criticalArea
                    )
                }
            }
        }
        
        askNotificationPermission()
    }

    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                fetchFcmToken()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // UI could explain why we need it
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            fetchFcmToken()
        }
    }
    
    private fun fetchFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = mapOf("token" to token)
                    ApiClient.retrofitService.registerFcmToken(request)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to register FCM token: ${e.message}")
                }
            }
        }
    }
}
