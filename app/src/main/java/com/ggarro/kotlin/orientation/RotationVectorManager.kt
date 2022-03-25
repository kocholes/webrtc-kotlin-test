package com.ggarro.kotlin.orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager

class RotationVectorManager : BaseOrientationManager {

    constructor(context: Context) : super(context) {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }

    override fun getOrientation(event: SensorEvent): String? {
        if (event.sensor.type == sensor?.type) {
            val rotationMatrix = FloatArray(16)
            val rotatedRotationMatrix = FloatArray(16)
            val orientation = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
//            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotatedRotationMatrix)
            SensorManager.getOrientation(rotationMatrix, orientation)
//            val yaw = (orientation[0] * 180 / Math.PI).toInt() + 90
//            val pitch = (-orientation[2] * 180 / Math.PI).toInt() - 90
//            val roll = (-orientation[1] * 180 / Math.PI).toInt()
            val yaw = 0//orientation[0]
            val pitch = orientation[1]
            val roll = orientation[2]

            return "${pitch},${roll},${yaw}"
        }
        return null
    }
}