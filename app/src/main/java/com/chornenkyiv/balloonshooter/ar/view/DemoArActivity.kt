package com.chornenkyiv.balloonshooter.ar.view

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.chornenkyiv.balloonshooter.R
import com.chornenkyiv.balloonshooter.ar.ARCoreHelper
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import kotlinx.android.synthetic.main.activity_ar.*
import java.util.concurrent.TimeUnit

class DemoArActivity : AppCompatActivity() {

    private lateinit var arCoreHelper: ARCoreHelper

    private lateinit var gestureDetector: GestureDetector
    private var loadingMessageSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        arCoreHelper = ARCoreHelper(this)

        arSceneView.setupSession(arCoreHelper.createArSession())

        // Set up a tap gesture detector.
        gestureDetector = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                @RequiresApi(Build.VERSION_CODES.N)
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    onSingleTap(e)
                    return true
                }

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }
            })

        // Set a touch listener on the Scene to listen for taps.
        arSceneView
            .scene
            .setOnTouchListener { _: HitTestResult, event: MotionEvent ->
                return@setOnTouchListener gestureDetector.onTouchEvent(event)
            }

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
            .scene
            .addOnUpdateListener {
                if (loadingMessageSnackbar == null || arSceneView.arFrame == null) {
                    return@addOnUpdateListener
                }

                val frame = arSceneView.arFrame ?: return@addOnUpdateListener

                if (frame.camera.trackingState !== TrackingState.TRACKING) {
                    return@addOnUpdateListener
                }

                for (plane in frame.getUpdatedTrackables(Plane::class.java)) {
                    if (plane.trackingState === TrackingState.TRACKING) {
                        hideLoadingMessage()
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        arSceneView.resume()
        showLoadingMessage()
    }

    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun onSingleTap(tap: MotionEvent) {
        val frame = arSceneView.arFrame
        if (frame != null) {
            tryPlaceModel(tap, frame)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun tryPlaceModel(tap: MotionEvent?, frame: Frame): Boolean {
        if (tap != null && frame.camera.trackingState === TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    // Create the Anchor.
                    val anchor = hit.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arSceneView.scene)
                    anchorNode.setOnTapListener(object : Node.OnTapListener {
                        override fun onTap(p0: HitTestResult?, p1: MotionEvent?) {
                            p0?.node?.localPosition = Vector3(0f, 10f, 0f)
                        }
                    })
                    val node = createNode()
                    anchorNode.addChild(node)
                    return true
                }
            }
        }

        return false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createNode(): Node {
        val base = Node()

        val modelNode = Node()
        modelNode.setParent(base)
        modelNode.localPosition = Vector3(0f, 0f, 0f)
        modelNode.setOnTapListener { node, _ ->
            // node.node?.setParent(null) // delete node on tap
            animateNode(node.node!!)
        }

        MaterialFactory.makeOpaqueWithColor(
            this, Color(android.graphics.Color.RED)
        )
            .thenAccept { material ->
                val sphere = ShapeFactory.makeSphere(
                    0.1f,
                    Vector3(0.0f, 0.15f, 0.0f),
                    material
                )
                modelNode.renderable = sphere
            }

        return base
    }

    private fun showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar!!.isShownOrQueued) {
            return
        }

        loadingMessageSnackbar = Snackbar.make(
            this.findViewById(android.R.id.content),
            "Looking for the plane",
            Snackbar.LENGTH_INDEFINITE
        )
        loadingMessageSnackbar?.show()
    }

    private fun hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return
        }

        loadingMessageSnackbar?.dismiss()
        loadingMessageSnackbar = null
    }

    private fun animateNode(node: Node) {
        val durationInMilliseconds = TimeUnit.SECONDS.toMillis(10)
        val minimumIntensity = 0.0f
        val maximumIntensity = 1.0f
        val intensityAnimator = ObjectAnimator.ofFloat(minimumIntensity, maximumIntensity)
        intensityAnimator.addUpdateListener {
            shiftNodeYBy(node, it.animatedValue as Float)
        }
        intensityAnimator.duration = durationInMilliseconds
        intensityAnimator.start()
    }

    private fun shiftNodeYBy(node: Node, shift: Float) {
        node.localPosition =
            Vector3(node.localPosition.x, node.localPosition.y + shift, node.localPosition.z)
    }
}
