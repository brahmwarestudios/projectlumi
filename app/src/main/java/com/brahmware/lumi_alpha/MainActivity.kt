package com.brahmware.lumi_alpha

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Config
import com.google.ar.core.Frame
// import com.google.ar.core.Pose
// import com.google.ar.core.Anchor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import io.github.sceneview.ar.ARSceneView
// import io.github.sceneview.node.ModelNode
// import io.github.sceneview.math.Position

class MainActivity : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView
    private lateinit var statusText: TextView
    private lateinit var poseOverlay: PoseOverlayView
    private lateinit var poseDetector: PoseDetector
    private lateinit var gownOverlay: GownOverlayView

    private var isProcessingFrame = false
    private var frameCount = 0
    // private var gownNode: ModelNode? = null
    // private var isGownLoaded = false
    // private var gownAnchor: Anchor? = null

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val TAG = "ProjectLumi"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arSceneView = findViewById(R.id.arSceneView)
        statusText = findViewById(R.id.statusText)
        poseOverlay = findViewById(R.id.poseOverlay)
        gownOverlay = findViewById(R.id.gownOverlay)

        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)

        if (hasCameraPermission()) {
            initAR()
        } else {
            requestCameraPermission()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initAR()
        } else {
            statusText.text = "Camera permission is required for AR."
        }
    }

    private fun initAR() {
        statusText.text = "Initializing AR..."

        arSceneView.apply {
            onSessionCreated = { session ->
                session.configure(
                    session.config.apply {
                        depthMode = Config.DepthMode.AUTOMATIC
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR // ← add here
                    }
                )
                runOnUiThread {
                    statusText.text = "Point camera at yourself"
                }
                // loadGownModel()
            }

            onSessionFailed = { exception ->
                runOnUiThread {
                    statusText.text = "AR failed: ${exception.message}"
                }
            }

            onSessionUpdated = { _, frame ->
                processArFrame(frame)
            }

        }
    }

    /* private fun loadGownModel() {
        Log.d(TAG, "Attempting to load gown model...")

        val loader = arSceneView.modelLoader
        if (loader == null) {
            Log.e(TAG, "Model loader is NULL — cannot load model")
            runOnUiThread { statusText.text = "Model loader unavailable" }
            return
        }

        Log.d(TAG, "Model loader available, loading file...")

        loader.loadModelAsync(
            fileLocation = "lumi_gown_1.glb",
            onResult = { modelInstance ->
                Log.d(TAG, "Load result received. Instance is null: ${modelInstance == null}")
                if (modelInstance != null) {
                    Log.d(TAG, "Creating ModelNode...")
                    gownNode = ModelNode(
                        modelInstance = modelInstance.instance,
                        scaleToUnits = 1.8f  // roughly human height in meters
                    ).also { node ->
                        node.worldPosition = Position(0f, -0.5f, -1.5f)
                        // -0.5f on Y moves it slightly downward so it's centered on body
                        // -1.5f on Z keeps it 1.5 meters in front of camera
                        node.worldScale = io.github.sceneview.math.Scale(1.8f, 1.8f, 1.8f)
                        arSceneView.addChildNode(node)
                        Log.d(TAG, "Node added to scene at position: ${node.worldPosition}")
                        isGownLoaded = true
                        runOnUiThread {
                            statusText.text = "Gown loaded ✓"
                        }
                    }
                } else {
                    Log.e(TAG, "Model instance is null — file may not be found")
                    runOnUiThread { statusText.text = "Model instance null" }
                }
            }
        )
    } */

    private fun processArFrame(frame: Frame) {
        if (isProcessingFrame) return
        frameCount++
        if (frameCount % 10 != 0) return
        isProcessingFrame = true

        try {
            if (frame.timestamp == 0L) {
                isProcessingFrame = false
                return
            }

            val cameraImage = try {
                frame.acquireCameraImage()
            } catch (e: Exception) {
                isProcessingFrame = false
                return
            }

            val inputImage = try {
                InputImage.fromMediaImage(cameraImage, 90)
            } catch (e: Exception) {
                cameraImage.close()
                isProcessingFrame = false
                return
            }

            // Get camera pose for 3D placement
            val cameraPose = frame.camera.pose

            poseDetector.process(inputImage)
                .addOnSuccessListener { pose ->
                    if (pose.allPoseLandmarks.isNotEmpty()) {
                        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

                        if (leftShoulder != null && rightShoulder != null &&
                            leftShoulder.inFrameLikelihood > 0.5f &&
                            rightShoulder.inFrameLikelihood > 0.5f
                        ) {
                            runOnUiThread {
                                statusText.text = "Person detected ✓"
                                poseOverlay.updatePose(pose, cameraImage.width, cameraImage.height)
                                gownOverlay.updatePose(pose, cameraImage.width, cameraImage.height)
                            }
                        } else {
                            runOnUiThread {
                                statusText.text = "No person detected"
                                gownOverlay.clearPose()
                            }
                        }
                    } else {
                        runOnUiThread {
                            statusText.text = "No person detected"
                            gownOverlay.clearPose()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Pose detection failed: ${e.message}")
                }
                .addOnCompleteListener {
                    try { cameraImage.close() } catch (e: Exception) { }
                    isProcessingFrame = false
                }

        } catch (e: Exception) {
            Log.e(TAG, "Frame processing error: ${e.message}")
            isProcessingFrame = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // gownAnchor?.detach()
        poseDetector.close()
    }
}