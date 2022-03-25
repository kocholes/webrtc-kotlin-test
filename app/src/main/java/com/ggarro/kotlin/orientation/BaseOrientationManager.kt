package com.ggarro.kotlin.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

abstract class BaseOrientationManager {
    protected val context: Context
    protected val sensorManager: SensorManager
    protected var sensor: Sensor? = null
    protected var sensorListener: SensorEventListener? = null

    constructor(context: Context) {
        this.context = context
        this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    abstract fun getOrientation(event: SensorEvent): String?

    open fun initListener(onOrientationChange: (orientation: String?) -> Unit) {
        stopListener()

        sensorListener = createSensorListener(onOrientationChange)

        sensorManager.registerListener(
            sensorListener,
            sensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    open fun stopListener() {
        sensorManager.unregisterListener(sensorListener)
    }

    private fun createSensorListener(onOrientationChange: (orientation: String?) -> Unit): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val orientation = getOrientation(event)
                onOrientationChange(orientation)
            }

            override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            }
        }
    }
}