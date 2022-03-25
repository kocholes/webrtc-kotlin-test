package com.ggarro.kotlin.orientation

import android.content.Context

class OrientationManagerFactory {
    companion object {
        const val ROTATION_VECTOR = "ROTATION_VECTOR"
        const val OVP_ROTATION = "OVP_ROTATION"
        const val FSENSOR_KALMAN_FILTER = "FSENSOR_KALMAN_FILTER"
        const val FSENSOR_COMPLEMENTARY_FILTER = "FSENSOR_COMPLEMENTARY_FILTER"

        fun getManager(type: String, context: Context): BaseOrientationManager? {
            if (type.equals(ROTATION_VECTOR, true)) {
                return RotationVectorManager(context)
            }
            if (type.equals(OVP_ROTATION, true)) {
                return OvpRotationManager(context)
            }
            if (type.equals(FSENSOR_KALMAN_FILTER, true)) {
                return FsensorKalmanFilterManager(context)
            }
            if (type.equals(FSENSOR_COMPLEMENTARY_FILTER, true)) {
                return FsensorComplementaryFilterManager(context)
            }

            return null;
        }
    }
}