package com.chornenkyiv.balloonshooter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chornenkyiv.balloonshooter.ar.ARCoreAvailabilityCallback
import com.chornenkyiv.balloonshooter.ar.ARCoreAvailabilityStatus
import com.chornenkyiv.balloonshooter.ar.ARCoreHelper
import com.chornenkyiv.balloonshooter.ar.CameraPermissionHelper
import com.chornenkyiv.balloonshooter.ar.view.DemoArActivity
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

        // Android 7.0 is min supported version for ARCore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            btnOpenAr.visibility = VISIBLE
        } else {
            btnOpenAr.visibility = GONE
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

        btnEnterAr.setOnClickListener {
            startActivity(Intent(this@MainActivity, DemoArActivity::class.java))
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
