package com.screenlight.app

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity: Displays the Analog Clock and handles user interactions.
 * It uses a Slide-to-Unlock mechanism to minimize itself.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on and hide system bars
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        
        setContentView(R.layout.activity_main)
        
        setupUI()
        startInactivityService()
        checkAccessibilityPermission()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetTimer()
    }

    private fun resetTimer() {
        val intent = Intent("com.screenlight.app.ACTION_RESET_TIMER")
        intent.setPackage(packageName)
        sendBroadcast(intent)
    }

    private fun setupUI() {
        setImmersiveMode()

        val unlockSlider = findViewById<SeekBar>(R.id.unlockSlider)
        unlockSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                resetTimer()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                resetTimer()
                if (seekBar != null) {
                    if (seekBar.progress > 85) {
                        seekBar.progress = 0
                        moveTaskToBack(true)
                    } else {
                        seekBar.progress = 0
                    }
                }
            }
        })

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            AlertDialog.Builder(this)
                .setTitle("Setup Activity Detection")
                .setMessage("To stop the clock while using other apps, please enable 'AlwaysScreen Touch Detector' in Accessibility Settings.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = android.content.ComponentName(this, GlobalTouchService::class.java)
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabledServices.contains(expectedComponentName.flattenToString())
    }

    private fun showSettingsDialog() {
        val options = arrayOf("15 Seconds", "30 Seconds", "1 Minute", "Never")
        val values = arrayOf(15000L, 30000L, 60000L, -1L)
        
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currentTimeout = prefs.getLong("INACTIVITY_TIMEOUT", 15000L)
        
        var checkedItem = values.indexOf(currentTimeout)
        if (checkedItem == -1) checkedItem = 0

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("Inactivity Timeout")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                prefs.edit().putLong("INACTIVITY_TIMEOUT", values[which]).apply()
                startInactivityService()
                dialog.dismiss()
            }
            .show()
    }

    private fun setImmersiveMode() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    private fun startInactivityService() {
        val intent = Intent(this, InactivityService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setImmersiveMode()
            findViewById<SeekBar>(R.id.unlockSlider)?.progress = 0
        }
    }

    override fun onBackPressed() {}
}