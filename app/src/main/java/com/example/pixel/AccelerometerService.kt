package com.example.pixel

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import com.google.gson.Gson
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class AccelerometerService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var heartRate: Sensor
    private lateinit var handlerThread: HandlerThread
    private lateinit var handler: Handler
    private lateinit var mqttClient: MqttClient
    private var lastUpdate: Long = 0
    private var lastHrUpdate: Long = 0

    private val ACCELEROMETER_RATE: Int = SensorManager.SENSOR_DELAY_GAME
    private val GYROSCOPE_RATE: Int = SensorManager.SENSOR_DELAY_GAME
    private val HEART_RATE_RATE: Int = SensorManager.SENSOR_DELAY_NORMAL

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        heartRate = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

        val brokerUrl = "tcp://cpshealthcare.cl:1883"
        val clientId = "pixel-watch-client"
        mqttClient = MqttClient(brokerUrl, clientId, MemoryPersistence())

        val options = MqttConnectOptions()
        mqttClient.connect(options)
        handlerThread = HandlerThread("AccelerometerServiceThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(object : Runnable {
            override fun run() {
                // Register accelerometer sensor listener
                sensorManager.registerListener(this@AccelerometerService, accelerometer, ACCELEROMETER_RATE)

                // Register gyroscope sensor listener
                sensorManager.registerListener(this@AccelerometerService, gyroscope, GYROSCOPE_RATE)

                // Register heart rate sensor listener
                sensorManager.registerListener(this@AccelerometerService, heartRate, HEART_RATE_RATE)

                handler.postDelayed(this, 20) // 50 Hz
            }
        })

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            lastUpdate = System.currentTimeMillis()
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Do something with the accelerometer data (e.g. send to server)
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
            mqttClient.publish(topic, json.toByteArray(), 0, false)
        } else if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE ) {
            lastUpdate = System.currentTimeMillis()
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Do something with the gyroscope data (e.g. send to server)
        } else if (event?.sensor?.type == Sensor.TYPE_HEART_RATE ) {
            lastHrUpdate = System.currentTimeMillis()
            val heartRate = event.values[0]
            // Do something with the heart rate data (e.g. send to server)
        }
    }
}