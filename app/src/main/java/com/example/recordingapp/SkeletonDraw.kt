package com.example.recordingapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.Display
import android.view.View
import androidx.annotation.RequiresApi
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import android.view.WindowManager
import androidx.core.content.res.ResourcesCompat


class SkeletonDraw(context: Context?, var pose: Pose) : View(context) {

    private val boundaryPaint: Paint = Paint()
    private val leftPaint = Paint()
    private val rightPaint = Paint()

    private val screenWidth: Int

    init {
        val wm = this.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        screenWidth = display.width

        val blue = ResourcesCompat.getColor(this.context.resources, R.color.white, null)

        boundaryPaint.color = blue
        boundaryPaint.strokeWidth = 5f
        boundaryPaint.style = Paint.Style.STROKE

        leftPaint.strokeWidth = 5f
        leftPaint.color = blue

        rightPaint.strokeWidth = 5f
        rightPaint.color = blue
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val landmarks = pose.allPoseLandmarks

        for (landmark in landmarks) {
            canvas?.drawCircle(translateX(landmark.position.x),landmark.position.y,8.0f, boundaryPaint)
        }

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)  ?:return
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER) ?:return
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW) ?:return
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW) ?:return
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?:return
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST) ?:return
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP) ?:return
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP) ?:return
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE) ?:return
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE) ?:return
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE) ?:return
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE) ?:return

        val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?:return
        val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY) ?:return
        val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?:return
        val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX) ?:return
        val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB) ?:return
        val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB) ?:return
        val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL) ?:return
        val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL) ?:return
        val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX) ?:return
        val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX) ?:return


        canvas?.drawLine(translateX(leftShoulder.position.x),leftShoulder.position.y,translateX(rightShoulder.position.x),rightShoulder.position.y,boundaryPaint)
        canvas?.drawLine(translateX(leftHip.position.x),leftHip.position.y,translateX(rightHip.position.x),rightHip.position.y,boundaryPaint)

        //Left body
        canvas?.drawLine(translateX(leftShoulder.position.x),leftShoulder.position.y,translateX(leftElbow.position.x),leftElbow.position.y,leftPaint)
        canvas?.drawLine(translateX(leftElbow.position.x),leftElbow.position.y,translateX(leftWrist.position.x),leftWrist.position.y,leftPaint)
        canvas?.drawLine(translateX(leftShoulder.position.x),leftShoulder.position.y,translateX(leftHip.position.x),leftHip.position.y,leftPaint)
        canvas?.drawLine(translateX(leftHip.position.x),leftHip.position.y,translateX(leftKnee.position.x),leftKnee.position.y,leftPaint)
        canvas?.drawLine(translateX(leftKnee.position.x),leftKnee.position.y,translateX(leftAnkle.position.x),leftAnkle.position.y,leftPaint)
        canvas?.drawLine(translateX(leftWrist.position.x),leftWrist.position.y,translateX(leftThumb.position.x),leftThumb.position.y,leftPaint)
        canvas?.drawLine(translateX(leftWrist.position.x),leftWrist.position.y,translateX(leftPinky.position.x),leftPinky.position.y,leftPaint)
        canvas?.drawLine(translateX(leftWrist.position.x),leftWrist.position.y,translateX(leftIndex.position.x),leftIndex.position.y,leftPaint)
        canvas?.drawLine(translateX(leftIndex.position.x),leftIndex.position.y,translateX(leftPinky.position.x),leftPinky.position.y,leftPaint)
        canvas?.drawLine(translateX(leftAnkle.position.x),leftAnkle.position.y,translateX(leftHeel.position.x),leftHeel.position.y,leftPaint)
        canvas?.drawLine(translateX(leftHeel.position.x),leftHeel.position.y,translateX(leftFootIndex.position.x),leftFootIndex.position.y,leftPaint)

        ////Right body
        canvas?.drawLine(translateX(rightShoulder.position.x),rightShoulder.position.y,translateX(rightElbow.position.x),rightElbow.position.y,rightPaint)
        canvas?.drawLine(translateX(rightElbow.position.x),rightElbow.position.y,translateX(rightWrist.position.x),rightWrist.position.y,rightPaint)
        canvas?.drawLine(translateX(rightShoulder.position.x),rightShoulder.position.y,translateX(rightHip.position.x),rightHip.position.y,rightPaint)
        canvas?.drawLine(translateX(rightHip.position.x),rightHip.position.y,translateX(rightKnee.position.x),rightKnee.position.y,rightPaint)
        canvas?.drawLine(translateX(rightKnee.position.x),rightKnee.position.y,translateX(rightAnkle.position.x),rightAnkle.position.y,rightPaint)
        canvas?.drawLine(translateX(rightWrist.position.x),rightWrist.position.y,translateX(rightThumb.position.x),rightThumb.position.y,rightPaint)
        canvas?.drawLine(translateX(rightWrist.position.x),rightWrist.position.y,translateX(rightPinky.position.x),rightPinky.position.y,rightPaint)
        canvas?.drawLine(translateX(rightWrist.position.x),rightWrist.position.y,translateX(rightIndex.position.x),rightIndex.position.y,rightPaint)
        canvas?.drawLine(translateX(rightIndex.position.x),rightIndex.position.y,translateX(rightPinky.position.x),rightPinky.position.y,rightPaint)
        canvas?.drawLine(translateX(rightAnkle.position.x),rightAnkle.position.y,translateX(rightHeel.position.x),rightHeel.position.y,rightPaint)
        canvas?.drawLine(translateX(rightHeel.position.x),rightHeel.position.y,translateX(rightFootIndex.position.x),rightFootIndex.position.y,rightPaint)

    }



    private fun translateX(x: Float): Float {
        // you will need this for the inverted image in case of using front camera
        return screenWidth.minus(x)
    }


}