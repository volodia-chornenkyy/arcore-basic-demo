package com.chornenkyiv.balloonshooter.ar

import android.app.Activity
import com.google.ar.core.ArCoreApk


class ARCoreHelper(private val activity: Activity) {

    /**
     * Official solution with isTransient() is not working as always returns "false".
     *
     * Looks like it only checks if package can be installed of the Play Market.
     * https://github.com/google-ar/arcore-android-sdk/issues/162#issuecomment-427602424
     */
    fun checkIfArCoreAvailable(availabilityCallback: ARCoreAvailabilityCallback) {
        if (ArCoreApk.getInstance().checkAvailability(activity).isSupported.not()) {
            availabilityCallback.handleARCoreAvailability(ARCoreAvailabilityStatus.NEED_INSTALL)
        } else {
            availabilityCallback.handleARCoreAvailability(ARCoreAvailabilityStatus.AVAILABLE)
        }
    }

    fun askToInstallArCore() {
        // if pass "false" next invocation of requestInstall() will either return
        // INSTALLED or throw an UnavailableUserDeclinedInstallationException.
        val alwaysAskToInstall = true
        ArCoreApk.getInstance().requestInstall(activity, alwaysAskToInstall)
    }
}