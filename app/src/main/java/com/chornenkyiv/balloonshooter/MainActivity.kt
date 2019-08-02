package com.chornenkyiv.balloonshooter

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chornenkyiv.balloonshooter.ar.ARCoreAvailabilityCallback
import com.chornenkyiv.balloonshooter.ar.ARCoreHelper
import com.chornenkyiv.balloonshooter.ar.CameraPermissionHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var arCoreHelper: ARCoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arCoreHelper = ARCoreHelper(this)

        arCoreHelper.checkIfArCoreSupported(object : ARCoreAvailabilityCallback {
            override fun handleARCoreAvailability(available: Boolean) {
                // show AR content only in case ARCore is available
                if (available) {
                    arContainer.visibility = View.VISIBLE
                }

                Toast.makeText(this@MainActivity, "AR supported $available", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        btnEnterAr.setOnClickListener {
            // ARCore requires camera permission to operate.
            if (!CameraPermissionHelper.hasCameraPermission(this@MainActivity)) {
                CameraPermissionHelper.requestCameraPermission(this@MainActivity)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
        }
    }
}
