package com.ggarro.kotlin.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.view.Surface
import com.kircherelectronics.fsensor.filter.averaging.LowPassFilter
import kotlin.math.atan2
import kotlin.math.sqrt

class OvpRotationManager : BaseOrientationManager {
    private var filterTimeConstant = 0.18f
    private var lowPassFilter = LowPassFilter().also { it.setTimeConstant(filterTimeConstant) }
    private var rotation = Surface.ROTATION_90

    constructor(context: Context) : super(context) {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        rotation =
//            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
//        Log.i("OvpRotationManager", rotation.toString())
    }

    override fun getOrientation(event: SensorEvent): String? {
        try {
            val gravityAccel = lowPassFilter.filter(event.values)

            val x = gravityAccel[0]
            val y = gravityAccel[1]
            val z = gravityAccel[2]

            var roll = atan2(y, sqrt(x * x + z * z)).toDouble()
            var pitch = atan2(x, z).toDouble()

//            if (rotation > Surface.ROTATION_90) {
//                if (pitch <= -restingPosition.pitch) {
//                    pitch = -PI - pitch
//                } else {
//                    pitch = PI - pitch
//                }
//                if (roll <= -restingPosition.roll) {
//                    roll = -PI - roll
//                } else {
//                    roll = PI - roll
//                }
//            }
            return "${-roll},${-pitch},0"
        } catch (e: Exception) {
        }

        return null
    }
}