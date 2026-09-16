package com.rakshacast.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rakshacast.MainActivity
import com.rakshacast.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RakshaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
        sendTokenToBackend(token)
    }
    
    private fun sendTokenToBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = mapOf("token" to token)
                ApiClient.retrofitService.registerFcmToken(request)
                Log.d("FCM", "Token successfully registered with backend.")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to register token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "RakshaCast Alert"
        val body = data["body"] ?: remoteMessage.notification?.body ?: "New weather intelligence available."
        
        // Respecting the notification setting could be done by checking DataStore here if needed
        // but normally if backend pushes it, the backend should respect it if we sent the pref.
        // For prototype, we'll display the notification.
        sendNotification(title, body, data)
    }

    private fun sendNotification(title: String, messageBody: String, data: Map<String, String>) {
        val probabilityStr = data["probability"] ?: "0"
        val probability = probabilityStr.toIntOrNull() ?: 0
        
        val severity = data["riskLevel"] ?: "MODERATE"
        val isCritical = severity == "SEVERE" || severity == "EXTREME"
        
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            data["alertId"]?.let { putExtra("alertId", it) }
            putExtra("isCritical", isCritical)
            putExtra("hazardType", data["hazardType"] ?: "SEVERE WEATHER")
            putExtra("probability", probability)
            putExtra("location", data["location"] ?: "Affected Area")
            putExtra("riskLevel", severity)
            
            // New Rich Payload fields
            data["explanations"]?.let { putExtra("explanations", it) }
            data["timestamp"]?.let { putExtra("timestamp", it) }
            data["trend"]?.let { putExtra("trend", it) }
            data["model_version"]?.let { putExtra("model_version", it) }
            
            // Shelter & Affected Area
            data["shelterName"]?.let { putExtra("shelterName", it) }
            data["shelterDistanceKm"]?.let { putExtra("shelterDistanceKm", it) }
            data["shelterVerification"]?.let { putExtra("shelterVerification", it) }
            data["shelterAvailability"]?.let { putExtra("shelterAvailability", it) }
            data["radiusKm"]?.let { putExtra("radiusKm", it) }
            data["affectedAreaKm2"]?.let { putExtra("affectedAreaKm2", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = if (isCritical) "rakshacast_critical_channel" else "rakshacast_alerts_channel"
        

        val importance = if (isCritical) NotificationManager.IMPORTANCE_HIGH else when(severity) {
            "HIGH", "SEVERE", "EXTREME" -> NotificationManager.IMPORTANCE_HIGH
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(if (isCritical) "CRITICAL WEATHER WARNING" else title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_MAX else if (importance == NotificationManager.IMPORTANCE_HIGH) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)

        if (isCritical) {
            notificationBuilder.setFullScreenIntent(pendingIntent, true)
            notificationBuilder.setCategory(NotificationCompat.CATEGORY_ALARM)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = if (isCritical) "RakshaCast Critical Alerts" else "RakshaCast Alerts"
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
