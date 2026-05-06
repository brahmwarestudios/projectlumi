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
    private var itemBitmap: Bitmap? = null
    private var currentItemType: GownSelector.ItemType = GownSelector.ItemType.GOWN
    private var isFrontCamera = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        itemBitmap = BitmapFactory.decodeResource(resources, R.drawable.lumi_gown_1)
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

    fun setGownResource(drawableRes: Int, itemType: GownSelector.ItemType = GownSelector.ItemType.GOWN) {
        itemBitmap = BitmapFactory.decodeResource(resources, drawableRes)
        currentItemType = itemType
        invalidate()
    }

    fun setFrontCamera(isFront: Boolean) {
        isFrontCamera = isFront
        invalidate()
    }

    private fun mirrorX(x: Float, scaleX: Float): Float {
        return if (isFrontCamera) {
            width.toFloat() - (x * scaleX)
        } else {
            x * scaleX
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentPose = pose ?: return
        val bitmap = itemBitmap ?: return

        when (currentItemType) {
            GownSelector.ItemType.NECKLACE -> drawNecklace(canvas, currentPose, bitmap)
            GownSelector.ItemType.GOWN -> drawGown(canvas, currentPose, bitmap)
            GownSelector.ItemType.MALE_OUTFIT -> drawMaleOutfit(canvas, currentPose, bitmap)
        }
    }

    private fun drawGown(canvas: Canvas, pose: Pose, bitmap: Bitmap) {
        val scaleX = width.toFloat() / imageHeight.toFloat()
        val scaleY = height.toFloat() / imageWidth.toFloat()

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null) return
        if (leftShoulder.inFrameLikelihood < 0.5f ||
            rightShoulder.inFrameLikelihood < 0.5f) return

        val leftShoulderX = mirrorX(leftShoulder.position.x, scaleX)
        val leftShoulderY = leftShoulder.position.y * scaleY
        val rightShoulderX = mirrorX(rightShoulder.position.x, scaleX)
        val rightShoulderY = rightShoulder.position.y * scaleY
        val leftHipY = leftHip.position.y * scaleY
        val rightHipY = rightHip.position.y * scaleY

        val shoulderWidth = Math.abs(rightShoulderX - leftShoulderX) * 1.8f
        val torsoHeight = Math.abs(
            ((leftHipY + rightHipY) / 2f) - ((leftShoulderY + rightShoulderY) / 2f)
        ) * 3.0f

        val gownTop = (leftShoulderY + rightShoulderY) / 2f - shoulderWidth * 0.5f
        val gownLeft = (leftShoulderX + rightShoulderX) / 2f - shoulderWidth / 2f

        val matrix = Matrix()
        val bitmapScaleX = shoulderWidth / bitmap.width.toFloat()
        val bitmapScaleY = torsoHeight / bitmap.height.toFloat()
        matrix.setScale(bitmapScaleX, bitmapScaleY)
        matrix.postTranslate(gownLeft, gownTop)

        canvas.drawBitmap(bitmap, matrix, paint)
    }

    private fun drawNecklace(canvas: Canvas, pose: Pose, bitmap: Bitmap) {
        val scaleX = width.toFloat() / imageHeight.toFloat()
        val scaleY = height.toFloat() / imageWidth.toFloat()

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

        if (leftShoulder == null || rightShoulder == null || nose == null) return
        if (leftShoulder.inFrameLikelihood < 0.5f ||
            rightShoulder.inFrameLikelihood < 0.5f) return

        val leftShoulderX = mirrorX(leftShoulder.position.x, scaleX)
        val leftShoulderY = leftShoulder.position.y * scaleY
        val rightShoulderX = mirrorX(rightShoulder.position.x, scaleX)
        val rightShoulderY = rightShoulder.position.y * scaleY
        val noseY = nose.position.y * scaleY

        val necklaceWidth = Math.abs(rightShoulderX - leftShoulderX) * 1.1f
        val shoulderMidY = (leftShoulderY + rightShoulderY) / 2f
        val necklaceHeight = (shoulderMidY - noseY) * 0.8f
        val necklaceTop = noseY + (shoulderMidY - noseY) * 0.3f
        val necklaceLeft = (leftShoulderX + rightShoulderX) / 2f - necklaceWidth / 2f

        val matrix = Matrix()
        val bitmapScaleX = necklaceWidth / bitmap.width.toFloat()
        val bitmapScaleY = necklaceHeight / bitmap.height.toFloat()
        matrix.setScale(bitmapScaleX, bitmapScaleY)
        matrix.postTranslate(necklaceLeft, necklaceTop)

        canvas.drawBitmap(bitmap, matrix, paint)
    }

    private fun drawMaleOutfit(canvas: Canvas, pose: Pose, bitmap: Bitmap) {
        val scaleX = width.toFloat() / imageHeight.toFloat()
        val scaleY = height.toFloat() / imageWidth.toFloat()

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)

        if (leftShoulder == null || rightShoulder == null ||
            leftHip == null || rightHip == null) return
        if (leftShoulder.inFrameLikelihood < 0.5f ||
            rightShoulder.inFrameLikelihood < 0.5f) return

        val leftShoulderX = mirrorX(leftShoulder.position.x, scaleX)
        val leftShoulderY = leftShoulder.position.y * scaleY
        val rightShoulderX = mirrorX(rightShoulder.position.x, scaleX)
        val rightShoulderY = rightShoulder.position.y * scaleY
        val leftHipY = leftHip.position.y * scaleY
        val rightHipY = rightHip.position.y * scaleY

        val outfitWidth = Math.abs(rightShoulderX - leftShoulderX) * 1.8f
        val shoulderMidY = (leftShoulderY + rightShoulderY) / 2f
        val hipMidY = (leftHipY + rightHipY) / 2f
        val outfitHeight = (hipMidY - shoulderMidY) * 1.2f
        val outfitTop = shoulderMidY - outfitWidth * 0.15f
        val outfitLeft = (leftShoulderX + rightShoulderX) / 2f - outfitWidth / 2f

        val matrix = Matrix()
        val bitmapScaleX = outfitWidth / bitmap.width.toFloat()
        val bitmapScaleY = outfitHeight / bitmap.height.toFloat()
        matrix.setScale(bitmapScaleX, bitmapScaleY)
        matrix.postTranslate(outfitLeft, outfitTop)

        canvas.drawBitmap(bitmap, matrix, paint)
    }
}