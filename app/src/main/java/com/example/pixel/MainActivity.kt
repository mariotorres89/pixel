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
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var heartRate: Sensor
    private lateinit var mqttClient: MqttClient
    private val frequency = 50 // Set the frequency in Hz

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize sensor and power managers
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // Acquire a wake lock to prevent the device from sleeping
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AccelerometerWakeLock")
        wakeLock.acquire()

        // Get the accelerometer sensor
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val brokerUrl = "tcp://cpshealthcare.cl:1883"
        val clientId = "pixel-watch-client"
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

        val options = MqttConnectOptions()
        mqttClient.connect(options)
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
        val topic = "stream-ts-epoch"
        val timestamp = System.currentTimeMillis()
        val list = arrayListOf(x, y, z)
        val data = mapOf(
            "timestamp" to timestamp,
            "sensor" to "test_ecg",
            "client_id" to 0,
            "measures" to list,

        )
        val json = Gson().toJson(data)
        val message = "X: $x\nY: $y\nZ: $z"

        mqttClient.publish(topic, json.toByteArray(), 0, false)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}



