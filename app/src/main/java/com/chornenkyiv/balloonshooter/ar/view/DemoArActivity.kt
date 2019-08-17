package com.chornenkyiv.balloonshooter.ar.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.PlaneRenderer.MATERIAL_TEXTURE
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import kotlinx.android.synthetic.main.activity_ar.*
import java.util.concurrent.TimeUnit

class DemoArActivity : AppCompatActivity() {

    private lateinit var arCoreHelper: ARCoreHelper

    private lateinit var gestureDetector: GestureDetector
    private var loadingMessageSnackbar: Snackbar? = null

    @RequiresApi(Build.VERSION_CODES.N)
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

        setPlaneTexture("texture.png")
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

                    generateNodes(anchorNode, 10, 1000)
                    return true
                }
            }
        }

        return false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun generateNodes(anchorNode: AnchorNode, amount: Int, delayMillis: Long) {
        val handler = Handler()
        for (i in 0 until amount) {
            handler.postDelayed({ generateRandomlyPlacedNode(anchorNode) }, delayMillis * i)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun generateRandomlyPlacedNode(anchorNode: AnchorNode) {
        // Math.random() generates double in 0..1 range which corresponds to real 1 meter in ARCore
        val x = Math.random().toFloat() / 2
        val y = Math.random().toFloat() / 2
        anchorNode.addChild(createNode(x, y))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun createNode(x: Float, y: Float): Node {
        val base = Node()

        val modelNode = Node()
        modelNode.setParent(base)
        modelNode.localPosition = Vector3(x, y, 0f)
        modelNode.setOnTapListener { hitTestResult, _ ->
            deleteNode(hitTestResult.node!!) // delete hitTestResult on tap
        }
        modelNode.addLifecycleListener(object : Node.LifecycleListener {
            override fun onActivated(node: Node?) {
                // Start animation once node will be created
                animateNode(node!!, object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(p0: Animator?) {
                        //no-op
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        //no-op
                    }

                    override fun onAnimationEnd(p0: Animator?) {
                        deleteNode(node)
                    }

                    override fun onAnimationCancel(p0: Animator?) {
                        //no-op
                    }
                })
            }

            override fun onDeactivated(p0: Node?) {
                //no-op
            }

            override fun onUpdated(p0: Node?, p1: FrameTime?) {
                //no-op
            }
        })

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

    private fun animateNode(node: Node, animatorListener: Animator.AnimatorListener) {
        val durationInMilliseconds = TimeUnit.SECONDS.toMillis(10)
        val minimumIntensity = 0.0f
        val maximumIntensity = 2.0f
        val intensityAnimator = ObjectAnimator.ofFloat(minimumIntensity, maximumIntensity)
        intensityAnimator.addUpdateListener {
            updateNodeY(node, it.animatedValue as Float)
        }
        intensityAnimator.addListener(animatorListener)
        intensityAnimator.duration = durationInMilliseconds
        intensityAnimator.start()
    }

    private fun updateNodeY(node: Node, y: Float) {
        node.localPosition =
            Vector3(node.localPosition.x, y, node.localPosition.z)
    }

    private fun deleteNode(node: Node) {
        node.setParent(null)
    }

    /**
     * Sets the plane renderer texture.
     * @param texturePath - Path to texture to use in the assets directory.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setPlaneTexture(texturePath: String) {

        val sampler = Texture.Sampler.builder()
            .setMinFilter(Texture.Sampler.MinFilter.LINEAR_MIPMAP_LINEAR)
            .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
            .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
            .build()

        Texture.builder()
            .setSource { assets.open(texturePath) }
            .setSampler(sampler)
            .build()
            .thenAccept(fun(texture: Texture) {
                arSceneView.planeRenderer.material
                    .thenAccept {
                        it.setTexture(MATERIAL_TEXTURE, texture)
                    }
            })
    }
}
