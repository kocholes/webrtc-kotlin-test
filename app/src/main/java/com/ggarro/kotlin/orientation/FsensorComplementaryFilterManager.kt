package com.ggarro.kotlin.orientation

import android.content.Context
import android.hardware.SensorEvent
import com.kircherelectronics.fsensor.observer.SensorSubject.SensorObserver
import com.kircherelectronics.fsensor.sensor.FSensor
import com.kircherelectronics.fsensor.sensor.gyroscope.ComplementaryGyroscopeSensor

class FsensorComplementaryFilterManager: BaseOrientationManager {
    private val fSensor: FSensor
    private var sensorObserver: SensorObserver? = null

    constructor(context: Context) : super(context) {
        fSensor = ComplementaryGyroscopeSensor(context)
    }

    override fun getOrientation(event: SensorEvent): String? {
        return null
    }

    override fun initListener(onOrientationChange: (orientation: String?) -> Unit) {
        stopListener()
        sensorObserver = createSensorObserver(onOrientationChange)
        fSensor.register(sensorObserver)
        fSensor.start()
    }

    private fun createSensorObserver(onOrientationChange: (orientation: String?) -> Unit): SensorObserver {
        return SensorObserver { values ->
            val yaw = 0//values[0]
            val pitch = values[1]
            val roll = -values[2]
            onOrientationChange("${pitch},${roll},${yaw}")
        }
    }

    override fun stopListener() {
        fSensor.unregister(sensorObserver);
        fSensor.stop();
    }
}