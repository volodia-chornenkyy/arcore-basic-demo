package com.chornenkyiv.balloonshooter

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chornenkyiv.balloonshooter.ar.ARCoreAvailabilityCallback
import com.chornenkyiv.balloonshooter.ar.ARCoreAvailabilityStatus
import com.chornenkyiv.balloonshooter.ar.ARCoreHelper
import com.chornenkyiv.balloonshooter.ar.CameraPermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var arCoreHelper: ARCoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arCoreHelper = ARCoreHelper(this)

        btnOpen2d.setOnClickListener {
            showShortToast("Not implemented")
        }

        btnOpenAr.setOnClickListener {
            arCoreHelper.checkIfArCoreAvailable(object : ARCoreAvailabilityCallback {
                override fun handleARCoreAvailability(status: ARCoreAvailabilityStatus) {

                    if (status == ARCoreAvailabilityStatus.AVAILABLE) {
                        // ARCore requires camera permission to operate.
                        if (!CameraPermissionHelper.hasCameraPermission(this@MainActivity)) {
                            CameraPermissionHelper.requestCameraPermission(this@MainActivity)
                        } else {
                            showArContent()
                        }
                    } else if (status == ARCoreAvailabilityStatus.NEED_INSTALL) {
                        arCoreHelper.askToInstallArCore()
                    }
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            showShortToast("Camera permission is needed to run this application")

            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
        } else {
            showArContent()
        }
    }

    private fun showArContent() {
        arContainer.visibility = View.VISIBLE
    }

    private fun showShortToast(msg: String) {
        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
    }
}
