package com.example.pixel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var accelerometer: Sensor
    private val frequency = 50 // Set the frequency in Hz

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize sensor and power managers
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        // Acquire a wake lock to prevent the device from sleeping
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AccelerometerWakeLock")
        wakeLock.acquire()

        // Get the accelerometer sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Register the sensor listener
        sensorManager.registerListener(this, accelerometer, (1000000 / frequency).toInt())
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release the wake lock
        wakeLock.release()

        // Unregister the sensor listener
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        // Get the accelerometer values
        val x = event!!.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Update the UI with the accelerometer data
        val accelerometerDataTextView = findViewById<TextView>(R.id.accelerometer_data)
        accelerometerDataTextView.text = "X: $x\nY: $y\nZ: $z"
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}
