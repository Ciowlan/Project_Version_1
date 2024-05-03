package com.example.recordingapp

import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.roundToInt

class PoseImageAnalyser(
    private val poseDetector: PoseDetector,
    private val poseListener: PoseListener,

) : ImageAnalysis.Analyzer {

    private var frameCount = SCORE_DELIVERY_FRAME_RATE
    // We store n scores then analyse body placement given average value
    private val scores = arrayListOf<Double>()

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image

        //Log.d(MainActivity.TAG, "proxy: ${mediaImage?.width} - ${mediaImage?.height}")

        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            image ?: return

            poseDetector.process(image)
                .addOnSuccessListener { pose ->

                    val selectedLandmarks = landmarksIds.map { id ->
                        pose.getPoseLandmark(id)
                    }

                    // Return early if no landmarks detected
                    if (selectedLandmarks.isEmpty()) {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }

                    // SKELETON
                    poseListener.onPoseAnalysed(pose, Size(image.width, image.height))

                    // FRAME DETECTION
                    var score = 0.0
                    selectedLandmarks.map { mark ->
                        score += (mark?.inFrameLikelihood ?: 0f)
                    }

                    scores.add(score)

                    frameCount--
                    if (frameCount < 0) {
                        frameCount = SCORE_DELIVERY_FRAME_RATE
                        poseListener.fullBodyInFrame(scores.average() > (selectedLandmarks.size - SCORE_DELTA))
                        scores.clear()
                    }

                    imageProxy.close()


                }
                .addOnFailureListener {
                    Log.d("debuglog", "ANALYSIS FAILURE")
                    imageProxy.close()
                }
            
            }
    }

    private val landmarksIds = listOf(
        PoseLandmark.LEFT_EYE,
        PoseLandmark.RIGHT_EYE,
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
        PoseLandmark.LEFT_FOOT_INDEX,
        PoseLandmark.RIGHT_FOOT_INDEX,
        PoseLandmark.LEFT_MOUTH,
        PoseLandmark.RIGHT_MOUTH,
        PoseLandmark.NOSE
    )

    companion object {
        private const val SCORE_DELTA = 0.18 //Permissiveness constant (arbitrary)
        private const val SCORE_DELIVERY_FRAME_RATE = 10 //Trigger score every x video frame
    }

    interface PoseListener {
        fun onPoseAnalysed(pose: Pose, frameSize: Size)
        fun fullBodyInFrame(inFrame: Boolean)
    }

}