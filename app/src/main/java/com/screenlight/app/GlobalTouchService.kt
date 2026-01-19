package com.screenlight.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class GlobalTouchService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Any event (touch, scroll, click) in any app will trigger this
        val intent = Intent("com.screenlight.app.ACTION_RESET_TIMER")
        intent.setPackage(packageName) // Explicit broadcast for security
        sendBroadcast(intent)
    }

    override fun onInterrupt() {}
}