package com.brahmware.lumi_alpha

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class GownOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var pose: Pose? = null
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var gownBitmap: Bitmap? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        gownBitmap = BitmapFactory.decodeResource(resources, R.drawable.lumi_gown_1)
    }

    fun updatePose(pose: Pose, imageWidth: Int, imageHeight: Int) {
        this.pose = pose
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate()
    }

    fun clearPose() {
        this.pose = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentPose = pose ?: return
        val bitmap = gownBitmap ?: return

        val scaleX = width.toFloat() / imageHeight
        val scaleY = height.toFloat() / imageWidth

        val leftShoulder = currentPose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = currentPose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = currentPose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = currentPose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null) return

        if (leftShoulder.inFrameLikelihood < 0.5f ||
            rightShoulder.inFrameLikelihood < 0.5f) return

        // Calculate screen positions
        val leftShoulderX = leftShoulder.position.x * scaleX
        val leftShoulderY = leftShoulder.position.y * scaleY
        val rightShoulderX = rightShoulder.position.x * scaleX
        val rightShoulderY = rightShoulder.position.y * scaleY
        val leftHipX = leftHip.position.x * scaleX
        val leftHipY = leftHip.position.y * scaleY
        val rightHipX = rightHip.position.x * scaleX
        val rightHipY = rightHip.position.y * scaleY

        // Gown width = shoulder width with padding
        val shoulderWidth = Math.abs(rightShoulderX - leftShoulderX) * 1.4f

        // Gown height = shoulder to hip distance * 2.5 to cover full gown
        val torsoHeight = Math.abs(
            ((leftHipY + rightHipY) / 2f) - ((leftShoulderY + rightShoulderY) / 2f)
        ) * 2.5f

        // Top of gown starts at shoulder level
        val gownTop = (leftShoulderY + rightShoulderY) / 2f - (shoulderWidth * 0.1f)
        val gownLeft = (leftShoulderX + rightShoulderX) / 2f - (shoulderWidth / 2f)

        // Scale bitmap to fit calculated gown dimensions
        val matrix = Matrix()
        val bitmapScaleX = shoulderWidth / bitmap.width
        val bitmapScaleY = torsoHeight / bitmap.height
        matrix.setScale(bitmapScaleX, bitmapScaleY)
        matrix.postTranslate(gownLeft, gownTop)

        canvas.drawBitmap(bitmap, matrix, paint)
    }
}