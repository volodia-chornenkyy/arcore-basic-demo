package com.chornenkyiv.balloonshooter.ar

import android.content.Context
import android.os.Handler
import com.google.ar.core.ArCoreApk


class ARCoreHelper(context: Context) {

    private val arAvailability = ArCoreApk.getInstance().checkAvailability(context)

    fun checkIfArCoreSupported(availabilityCallback: ARCoreAvailabilityCallback) {
        if (arAvailability.isTransient) {
            // May need to query network resources to determine whether the device supports ARCore.
            Handler().postDelayed({ checkIfArCoreSupported(availabilityCallback) }, 200)
        }

        availabilityCallback.handleARCoreAvailability(arAvailability.isSupported)
    }
}