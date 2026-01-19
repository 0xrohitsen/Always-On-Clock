package com.screenlight.app

import android.app.*
import android.content.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo

class InactivityService : Service() {

    private val CHANNEL_ID = "InactivityServiceChannel"
    private val handler = Handler(Looper.getMainLooper())
    private var lastInteractionTime = System.currentTimeMillis()

    private val interactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.screenlight.app.ACTION_RESET_TIMER") {
                lastInteractionTime = System.currentTimeMillis()
            }
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> lastInteractionTime = System.currentTimeMillis()
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> lastInteractionTime = System.currentTimeMillis()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        setupForeground()
        
        // Register for manual reset events from Activities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(interactionReceiver, IntentFilter("com.screenlight.app.ACTION_RESET_TIMER"), Context.RECEIVER_NOT_EXPORTED)
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(interactionReceiver, IntentFilter("com.screenlight.app.ACTION_RESET_TIMER"))
            
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(screenReceiver, filter)
        }
        
        startInactivityTimer()
    }

    private fun setupForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "AlwaysScreen Monitor", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AlwaysScreen is Active")
            .setContentText("Monitoring for idle time...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(101, notification)
            }
        } catch (e: Exception) {
            Log.e("InactivityService", "Failed to start foreground service", e)
        }
    }

    private fun startInactivityTimer() {
        handler.removeCallbacksAndMessages(null)
        val runnable = object : Runnable {
            override fun run() {
                try {
                    val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                    val timeout = prefs.getLong("INACTIVITY_TIMEOUT", 15000L)
                    
                    if (timeout != -1L) {
                        val now = System.currentTimeMillis()
                        if (now - lastInteractionTime >= timeout && !isDeviceLocked() && !isAppInForeground()) {
                            openClockApp()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InactivityService", "Error in timer loop", e)
                }
                
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(runnable)
    }

    private fun isDeviceLocked(): Boolean {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            keyguardManager.isDeviceLocked
        } else {
            keyguardManager.isKeyguardLocked
        }
    }

    private fun isAppInForeground(): Boolean {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = am.runningAppProcesses ?: return false
        return processes.any { 
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && 
            it.processName == packageName 
        }
    }

    private fun openClockApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("InactivityService", "Failed to start activity", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            unregisterReceiver(interactionReceiver)
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {}
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}