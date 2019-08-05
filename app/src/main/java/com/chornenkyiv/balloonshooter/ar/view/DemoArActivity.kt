package com.chornenkyiv.balloonshooter.ar.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.chornenkyiv.balloonshooter.R
import com.chornenkyiv.balloonshooter.ar.ARCoreHelper
import kotlinx.android.synthetic.main.activity_ar.*

class DemoArActivity : AppCompatActivity() {

    private lateinit var arCoreHelper: ARCoreHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        arCoreHelper = ARCoreHelper(this)

        arSceneView.setupSession(arCoreHelper.createArSession())
    }

    override fun onResume() {
        super.onResume()
        arSceneView.resume()
    }

    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }
}
