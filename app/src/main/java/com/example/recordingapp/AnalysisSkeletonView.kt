package com.example.recordingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Size
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class AnalysisSkeletonView@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dotPaint: Paint = Paint()
    private val linePaint: Paint = Paint()

    private val isLandscape: Boolean

    // [MlKit id : Coordinates]
    private var landmarks: HashMap<Int, Coordinates> = hashMapOf()
        set(value) {
            field = value
            invalidate()
        }

    init {
        val wm = this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        isLandscape = display.width > display.height

        val blue = ResourcesCompat.getColor(this.context.resources, R.color.blue, null)

        dotPaint.color = blue
        dotPaint.strokeWidth = 100f // convert to px
        dotPaint.style = Paint.Style.FILL

        linePaint.color = blue
        linePaint.strokeWidth = 20f // convert to px
        linePaint.style = Paint.Style.FILL
    }

    fun setLandmarks(pose: Pose, frameSize: Size) {
        val hashMap = hashMapOf<Int, Coordinates>()

        landmarksSelection.map { id ->
            pose.getPoseLandmark(id)?.let { l ->
                if (l.inFrameLikelihood > MIN_FRAME_SCORE) {
                    val coordinate = Coordinates(l.position.x, l.position.y)
                    hashMap[id] = adaptCoordinateForDisplay(coordinate, frameSize)
                }
            }
        }

        landmarks = hashMap
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]
        val leftElbow = landmarks[PoseLandmark.LEFT_ELBOW]
        val rightElbow = landmarks[PoseLandmark.RIGHT_ELBOW]
        val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]
        val rightWrist = landmarks[PoseLandmark.RIGHT_WRIST]
        val leftHip = landmarks[PoseLandmark.LEFT_HIP]
        val rightHip = landmarks[PoseLandmark.RIGHT_HIP]
        val leftKnee = landmarks[PoseLandmark.LEFT_KNEE]
        val rightKnee = landmarks[PoseLandmark.RIGHT_KNEE]
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]
        val rightAnkle = landmarks[PoseLandmark.RIGHT_ANKLE]

        // POINTS
        landmarks.forEach { l ->
            canvas?.drawCircle(l.value.x, l.value.y, 45f, dotPaint) //todo convert to px
        }

        // TORSO LINES
        if (leftShoulder != null && rightShoulder != null)
            canvas?.drawLine(leftShoulder.x, leftShoulder.y, rightShoulder.x, rightShoulder.y, linePaint)
        if (leftShoulder != null && leftHip != null)
            canvas?.drawLine(leftShoulder.x, leftShoulder.y, leftHip.x, leftHip.y, linePaint)
        if (rightShoulder != null && rightHip != null)
                canvas?.drawLine(rightShoulder.x, rightShoulder.y, rightHip.x, rightHip.y, linePaint)
        if (rightShoulder != null && rightHip != null)
            canvas?.drawLine(rightShoulder.x, rightShoulder.y, rightHip.x, rightHip.y, linePaint)
        if (leftHip != null && rightHip != null)
            canvas?.drawLine(rightHip.x, rightHip.y, leftHip.x, leftHip.y, linePaint)

        // ARMS LINES
        if (leftShoulder != null && leftElbow != null)
            canvas?.drawLine(leftShoulder.x, leftShoulder.y, leftElbow.x, leftElbow.y, linePaint)
        if (leftWrist != null && leftElbow != null)
            canvas?.drawLine(leftWrist.x, leftWrist.y, leftElbow.x, leftElbow.y, linePaint)
        if (rightShoulder != null && rightElbow != null)
            canvas?.drawLine(rightShoulder.x, rightShoulder.y, rightElbow.x, rightElbow.y, linePaint)
        if (rightWrist != null && rightElbow != null)
            canvas?.drawLine(rightWrist.x, rightWrist.y, rightElbow.x, rightElbow.y, linePaint)

        // LEGS LINES
        if (leftHip != null && leftKnee != null)
            canvas?.drawLine(leftHip.x, leftHip.y, leftKnee.x, leftKnee.y, linePaint)
        if (rightHip != null && rightKnee != null)
            canvas?.drawLine(rightHip.x, rightHip.y, rightKnee.x, rightKnee.y, linePaint)
        if (leftKnee != null && leftAnkle != null)
            canvas?.drawLine(leftKnee.x, leftKnee.y, leftAnkle.x, leftAnkle.y, linePaint)
        if (rightKnee != null && rightAnkle != null)
            canvas?.drawLine(rightKnee.x, rightKnee.y, rightAnkle.x, rightAnkle.y, linePaint)
    }

    // Adapt coordinates so that they match with view displayed on screen
    private fun adaptCoordinateForDisplay(coordinate: Coordinates, analyzedImageSize: Size): Coordinates {

        val x = if (isLandscape) coordinate.x * (this.width / analyzedImageSize.width.toFloat())
        else coordinate.x * (this.width / analyzedImageSize.height.toFloat())

        val y = if (isLandscape) coordinate.y * (this.height / analyzedImageSize.height.toFloat())
        else coordinate.y * (this.height / analyzedImageSize.width.toFloat())

        return Coordinates(translateX(x), y)
    }

    // Needed for the inverted image when using front camera
    private fun translateX(x: Float): Float {
        return this.width.minus(x)
    }

    private val landmarksSelection = listOf(
        PoseLandmark.NOSE,
        PoseLandmark.LEFT_SHOULDER,
        PoseLandmark.RIGHT_SHOULDER,
        PoseLandmark.LEFT_ELBOW,
        PoseLandmark.RIGHT_ELBOW,
        PoseLandmark.LEFT_WRIST,
        PoseLandmark.RIGHT_WRIST,
        PoseLandmark.LEFT_HIP,
        PoseLandmark.RIGHT_HIP,
        PoseLandmark.LEFT_KNEE,
        PoseLandmark.RIGHT_KNEE,
        PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_ANKLE,
        PoseLandmark.LEFT_ANKLE,
        PoseLandmark.RIGHT_ANKLE,
    )

    companion object {
        private const val MIN_FRAME_SCORE = 0.97
        private const val TAG = "skeleton"
    }
    
    class Coordinates(val x: Float, val y: Float)
}