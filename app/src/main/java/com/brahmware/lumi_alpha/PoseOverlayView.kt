package com.brahmware.lumi_alpha

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PoseOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var pose: Pose? = null
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var isFrontCamera = false

    private val dotPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
        strokeWidth = 8f
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    fun updatePose(pose: Pose, imageWidth: Int, imageHeight: Int) {
        this.pose = pose
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate() // triggers redraw
    }

    fun setFrontCamera(isFront: Boolean) {
        isFrontCamera = isFront
        invalidate()
    }

    private fun mirrorX(x: Float, scaleX: Float): Float {
        return if (isFrontCamera) width.toFloat() - (x * scaleX) else x * scaleX
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentPose = pose ?: return

        val scaleX = width.toFloat() / imageHeight
        val scaleY = height.toFloat() / imageWidth

        // Draw connections (skeleton lines)
        val connections = listOf(
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
            Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
        )

        for ((startType, endType) in connections) {
            val start = currentPose.getPoseLandmark(startType)
            val end = currentPose.getPoseLandmark(endType)
            if (start != null && end != null &&
                start.inFrameLikelihood > 0.5f &&
                end.inFrameLikelihood > 0.5f) {
                canvas.drawLine(
                    start.position.x * scaleX,
                    start.position.y * scaleY,
                    end.position.x * scaleX,
                    end.position.y * scaleY,
                    linePaint
                )
            }
        }

        // Draw landmark dots
        for (landmark in currentPose.allPoseLandmarks) {
            if (landmark.inFrameLikelihood > 0.5f) {
                canvas.drawCircle(
                    mirrorX(landmark.position.x, scaleX),
                    landmark.position.y * scaleY,
                    10f,
                    dotPaint
                )
            }
        }
    }

}