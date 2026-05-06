package com.brahmware.lumi_alpha

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import android.widget.LinearLayout
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.view.animation.AnimationUtils
import com.google.ar.core.CameraConfig
import com.google.ar.core.CameraConfigFilter
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var arSceneView: ARSceneView
    private lateinit var statusText: TextView
    private lateinit var poseOverlay: PoseOverlayView
    private lateinit var poseDetector: PoseDetector
    private lateinit var gownOverlay: GownOverlayView
    private lateinit var gownSelector: GownSelector
    private lateinit var frontCameraPreview: PreviewView

    private var isProcessingFrame = false
    private var frameCount = 0
    // private var gownNode: ModelNode? = null
    // private var isGownLoaded = false
    // private var gownAnchor: Anchor? = null
    private var isSkeletonVisible = true
    private var lastStatusText = ""
    private var isFrontCamera = false
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var frontCameraProvider: ProcessCameraProvider? = null
    private var frontFrameCount = 0
    private var lastDetectedState = false
    private var stableFrameCount = 0
    private val requiredStableFrames = 5 // must be consistent for 5 frames before updating

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
        frontCameraPreview = findViewById(R.id.frontCameraPreview)

        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
        poseDetector = PoseDetection.getClient(options)

        if (hasCameraPermission()) {
            initAR()
        } else {
            requestCameraPermission()
        }

        setupUI()

        // Load product from intent AFTER setupUI so gownOverlay is ready
        loadProductFromIntent()
    }

    private fun loadProductFromIntent() {
        val product = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("product", Product::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("product")
        }

        product?.let {
            // Give the view a moment to be ready before applying
            gownOverlay.post {
                gownOverlay.setGownResource(it.imageRes, it.itemType)
                updateStatus("Previewing: ${it.name}")
            }
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
            updateStatus("Camera permission is required.")
        }
    }

    private fun initAR() {
        updateStatus("Initializing AR...")

        arSceneView.apply {
            onSessionCreated = { session ->
                session.configure(
                    session.config.apply {
                        depthMode = Config.DepthMode.AUTOMATIC
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                    }
                )
                runOnUiThread {
                    updateStatus("Point camera at yourself")
                }
                // loadGownModel()
            }

            onSessionFailed = { exception ->
                runOnUiThread {
                    updateStatus("AR failed: ${exception.message}")
                }
            }

            onSessionUpdated = { _, frame ->
                processArFrame(frame)
            }

        }
    }

    private fun toggleCamera() {
        isFrontCamera = !isFrontCamera

        if (isFrontCamera) {
            // Switch to front camera mode
            arSceneView.visibility = View.GONE
            frontCameraPreview.visibility = View.VISIBLE
            startFrontCamera()
            updateStatus("Front camera active")
        } else {
            // Switch back to rear AR camera
            stopFrontCamera()
            frontCameraPreview.visibility = View.GONE
            arSceneView.visibility = View.VISIBLE
            updateStatus("Rear camera active")
        }

        if (isFrontCamera) {
            arSceneView.visibility = View.GONE
            frontCameraPreview.visibility = View.VISIBLE
            gownOverlay.setFrontCamera(true)   // ← add this
            poseOverlay.setFrontCamera(true)   // ← add this
            startFrontCamera()
            updateStatus("Front camera active")
        } else {
            stopFrontCamera()
            frontCameraPreview.visibility = View.GONE
            arSceneView.visibility = View.VISIBLE
            gownOverlay.setFrontCamera(false)  // ← add this
            poseOverlay.setFrontCamera(false)  // ← add this
            updateStatus("Rear camera active")
        }
    }

    private fun updateDetectionStatus(isDetected: Boolean) {
        if (isDetected == lastDetectedState) {
            stableFrameCount++
        } else {
            stableFrameCount = 0
            lastDetectedState = isDetected
        }

        // Only update UI after state has been stable for required frames
        if (stableFrameCount == requiredStableFrames) {
            if (isDetected) {
                updateStatus("Person detected ✓", isDetected = true)
            } else {
                updateStatus("No person detected")
            }
        }
    }

    private fun startFrontCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            frontCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(frontCameraPreview.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processFrontCameraFrame(imageProxy)
            }

            try {
                frontCameraProvider?.unbindAll()
                frontCameraProvider?.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "Front camera binding failed: ${e.message}")
                isFrontCamera = false
                runOnUiThread {
                    frontCameraPreview.visibility = View.GONE
                    arSceneView.visibility = View.VISIBLE
                    updateStatus("Front camera unavailable")
                }
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun stopFrontCamera() {
        frontCameraProvider?.unbindAll()
        frontCameraProvider = null
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processFrontCameraFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        frontFrameCount++
        if (frontFrameCount % 10 != 0) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            270
        )

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
                            updateDetectionStatus(true)
                            poseOverlay.updatePose(pose, imageProxy.width, imageProxy.height)
                            gownOverlay.updatePose(pose, imageProxy.width, imageProxy.height)
                        }
                    } else {
                        runOnUiThread {
                            updateDetectionStatus(false)
                            gownOverlay.clearPose()
                        }
                    }
                } else {
                    runOnUiThread {
                        updateStatus("No person detected")
                        gownOverlay.clearPose()
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Front camera pose detection failed: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /*
    private fun showDisclaimerDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(
            this, R.style.LumiDialog
        )
            .setTitle("Project Lumi — Alpha")
            .setMessage(
                "This app is a prototype project currently in development.\n\n" +
                        "Features and visuals are subject to change and may not reflect " +
                        "the final intended experience.\n\n" +
                        "Thank you for testing Project Lumi!"
            )
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }
    */

    private fun updateStatus(message: String, isDetected: Boolean = false) {
        if (message == lastStatusText) return // avoid redundant updates
        lastStatusText = message

        runOnUiThread {
            // Fade out
            val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_down)
            statusText.startAnimation(fadeOut)

            fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    statusText.text = message

                    // Change pill color based on state
                    val bgDrawable = statusText.background as? android.graphics.drawable.GradientDrawable
                    if (isDetected) {
                        statusText.setBackgroundResource(R.drawable.status_pill_detected)
                    } else {
                        statusText.setBackgroundResource(R.drawable.status_pill_background)
                    }

                    // Fade in with new text
                    val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in_up)
                    statusText.startAnimation(fadeIn)
                }
            })
        }
    }

    private fun setupUI() {
        // Setup bottom sheet
        val bottomSheet = findViewById<LinearLayout>(R.id.gownBottomSheet)
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // Open selector button
        val openButton = findViewById<MaterialButton>(R.id.openSelectorButton)
        openButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        // Setup gown selector
        // Setup gown selector
        val container = findViewById<LinearLayout>(R.id.gownSelectorLayout)

        val categories = listOf(
            GownSelector.Category(
                title = "Gowns",
                items = listOf(
                    GownSelector.GownItem(R.drawable.lumi_gown_1, "Gown 1", GownSelector.ItemType.GOWN),
                    GownSelector.GownItem(R.drawable.lumi_gown_2, "Gown 2", GownSelector.ItemType.GOWN),
                    GownSelector.GownItem(R.drawable.lumi_gown_3, "Gown 3", GownSelector.ItemType.GOWN)
                )
            ),
            GownSelector.Category(
                title = "Necklaces",
                items = listOf(
                    GownSelector.GownItem(R.drawable.necklace_1, "Necklace 1", GownSelector.ItemType.NECKLACE),
                    GownSelector.GownItem(R.drawable.necklace_2, "Necklace 2", GownSelector.ItemType.NECKLACE),
                    GownSelector.GownItem(R.drawable.necklace_3, "Necklace 3", GownSelector.ItemType.NECKLACE)
                )
            ),
            GownSelector.Category(
                title = "Male Outfits",
                items = listOf(
                    GownSelector.GownItem(R.drawable.male_outfit_1, "Male Outfit 1", GownSelector.ItemType.MALE_OUTFIT),
                    GownSelector.GownItem(R.drawable.male_outfit_2, "Male Outfit 2", GownSelector.ItemType.MALE_OUTFIT),
                    GownSelector.GownItem(R.drawable.male_outfit_3, "Male Outfit 3", GownSelector.ItemType.MALE_OUTFIT)
                )
            )
        )

        gownSelector = GownSelector(
            context = this,
            container = container,
            categories = categories,
            onGownSelected = { item ->
                gownOverlay.setGownResource(item.drawableRes, item.type)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        )

        // FAB toggle skeleton
        val fab = findViewById<FloatingActionButton>(R.id.toggleSkeletonButton)
        fab.setOnClickListener {
            isSkeletonVisible = !isSkeletonVisible
            poseOverlay.visibility = if (isSkeletonVisible) View.VISIBLE else View.GONE
        }

        // Camera toggle
        val cameraToggleFab = findViewById<FloatingActionButton>(R.id.toggleCameraButton)
        cameraToggleFab.setOnClickListener {
            toggleCamera()
        }
    }

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
                                updateDetectionStatus(true)
                                poseOverlay.updatePose(pose, cameraImage.width, cameraImage.height)
                                gownOverlay.updatePose(pose, cameraImage.width, cameraImage.height)
                            }
                        } else {
                            runOnUiThread {
                                updateDetectionStatus(false)
                                gownOverlay.clearPose()
                            }
                        }
                    } else {
                        runOnUiThread {
                            updateStatus("No person detected")
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
        stopFrontCamera()
        cameraExecutor.shutdown()
        poseDetector.close()
    }
}