package com.example.pixel

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.app.Activity

class MainActivity : Activity() {

    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Acquire a wakelock to keep the device awake
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AccelerometerService::Wakelock")
        wakeLock.acquire()

        // Start AccelerometerService
        startService(Intent(this, AccelerometerService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release the wakelock
        if (wakeLock.isHeld) {
            wakeLock.release()
        }

        // Stop AccelerometerService
        stopService(Intent(this, AccelerometerService::class.java))
    }
}