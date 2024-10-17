package com.example.recordingapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.recordingapp.databinding.ActivityMainBinding
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.widget.Chronometer
import com.google.mlkit.vision.pose.PoseLandmark
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import org.tensorflow.lite.flex.FlexDelegate;
import org.tensorflow.lite.gpu.GpuDelegate




class MainActivity : AppCompatActivity(), PoseImageAnalyser.PoseListener {
    private lateinit var viewBinding: ActivityMainBinding

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var recordingState = RecordingState.STOPPED
    private lateinit var cameraExecutor: ExecutorService

    private var analysisActivated = true

    private lateinit var analysisSkeletonView: AnalysisSkeletonView

    private lateinit var dateTextView: TextView

    private lateinit var chronometer: Chronometer
    private var isRunning = false
    private var lastPause: Long = 0
    lateinit var end_time: String
    lateinit var interpreter: Interpreter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        analysisSkeletonView = viewBinding.analysisSkeletonView

        // 初始化 TextView
        dateTextView = findViewById(R.id.date)

        // 设置当天日期
        getCurrentDate()

        chronometer = findViewById(R.id.timer)
        val startButton: Button = findViewById(R.id.stop_button)

        val resetButton: Button = findViewById(R.id.end_button)

        startButton.setOnClickListener {
            if (!isRunning) {
                chronometer.base = SystemClock.elapsedRealtime() - lastPause
                chronometer.start()
                isRunning = true
                startButton.text = "STOP"
            }
            else{
                lastPause = SystemClock.elapsedRealtime() - chronometer.base
                chronometer.stop()
                isRunning = false
                startButton.text = "START"
            }
        }

        // Reset Chronometer
        resetButton.setOnClickListener {
            end_time = chronometer.text as String
            chronometer.base = SystemClock.elapsedRealtime()
            Log.d("Chronometer", ""+end_time)
            lastPause = 0
            chronometer.stop()
            isRunning = false
            startButton.text = "START"

            setRecordingFrame(FrameState.HIDDEN)
            val intent = Intent(this, End::class.java)
            startActivity(intent)

        }

        chronometer.setOnChronometerTickListener { chrono ->
            val time = SystemClock.elapsedRealtime() - chrono.base
            val h = (time / 3600000).toInt()
            val m = (time % 3600000 / 60000).toInt()
            val s = (time % 60000 / 1000).toInt()
            val hh = if (h < 10) "0$h" else h.toString()
            val mm = if (m < 10) "0$m" else m.toString()
            val ss = if (s < 10) "0$s" else s.toString()
            chrono.text = "$hh:$mm:$ss"
        }

        // 初始化 FlexDelegate
        val flexDelegate = FlexDelegate()

        // 初始化 GpuDelegate
        val gpuDelegate = GpuDelegate()

        // 创建 Interpreter 选项，并将 FlexDelegate 和 GpuDelegate 添加进去
        val options = Interpreter.Options()
        options.addDelegate(flexDelegate) // 如果模型需要 TensorFlow ops 支持
        options.addDelegate(gpuDelegate)  // 如果你想要 GPU 加速

        try {

            // 創建 Interpreter.Options 並添加 FlexDelegate
            val options = Interpreter.Options().apply {
                addDelegate(FlexDelegate())
            }
            // 接收数据
            val data = intent.getStringExtra("KEY_DATA")
            if(data=="squat"){

                // 初始化 interpreter 並傳入 options
                interpreter = Interpreter(loadModelFile(this, "action_model_2.tflite"), options)
                Log.e("check01", "是深蹲")

            }else if(data=="sit"){
                // 初始化 interpreter 並傳入 options
                interpreter = Interpreter(loadModelFile(this, "action_model_2.tflite"), options)
                Log.e("check01", "是仰臥起坐")
            }else{
                // 初始化 interpreter 並傳入 options
                interpreter = Interpreter(loadModelFile(this, "action_model_2.tflite"), options)
                Log.e("check01", "是伏地挺身")
            }



        } catch (e: Exception) {
            Log.e("bug01", "Failed to initialize interpreter", e)
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()

        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for video capture button
//        viewBinding.videoCaptureButton.setOnClickListener {
//            if (recording != null) stopVideoCapture()
//            else countDownStart(COUNT_DOWN_MANUAL_RECORDING).start()
//        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        setRecordingFrame(FrameState.DEFAULT)

        val buttonGoToStartActivity: Button = findViewById(R.id.return_page)
        buttonGoToStartActivity.setOnClickListener {

            setRecordingFrame(FrameState.HIDDEN)
            val intentReturn = Intent(this, Choose::class.java)
            startActivity(intentReturn)
        }

    }

    private fun stopVideoCapture() {
        recording?.stop()
        recording = null
    }

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        //viewBinding.videoCaptureButton.isEnabled = false

        if (recording != null) {
            stopVideoCapture()
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        recordingState = RecordingState.RECORDING
                        filmingDurationCountDown.start()
                        viewBinding.recordingIndicator.visibility = View.VISIBLE
//                        viewBinding.videoCaptureButton.apply {
//                            text = getString(R.string.stop_capture)
//                            isEnabled = true
//                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg =
                                "Video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " + "${recordEvent.error}")
                        }
                        viewBinding.recordingIndicator.visibility = View.GONE
//                        viewBinding.videoCaptureButton.apply {
//                            text = getString(R.string.start_capture)
//                            isEnabled = true
//                        }
                        recordingState = RecordingState.STOPPED
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
//            val previewForvideo = Preview.Builder()
//                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
//                .build()
//                .also {
//                    it.setSurfaceProvider(viewBinding.viewFinderForvideo.surfaceProvider)
//                }
            //viewBinding.viewFinder.scaleType = PreviewView.ScaleType.FIT_CENTER

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)


            // Select front camera as a default
            //val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalysis = getImageAnalysis()

            //val viewPort = viewBinding.viewFinder.viewPort!!
            val useCaseGroup = UseCaseGroup.Builder()
                //.setViewPort(viewBinding.viewFinder.viewPort!!)
                .addUseCase(imageAnalysis)
                .addUseCase(preview)
                .build()


            /*
            Log.d(TAG, "startCamera: preview aspect ratio = ${viewBinding.viewFinder.viewPort?.aspectRatio}")
            Log.d(TAG, "startCamera: preview aspect ratio = ${viewBinding.viewFinder.viewPort?.aspectRatio?.toDouble()}")

            val width = viewBinding.viewFinder.height * viewBinding.viewFinder.viewPort?.aspectRatio?.toDouble()!!
            Log.d(TAG, "startCamera: preview width = ${width}")
             */

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                //cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, videoCapture)
                //cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis,videoCapture)
                cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup)
                // Bind use cases to camera
                //cameraProvider.bindToLifecycle(this, cameraSelector, useCaseGroup)


            } catch (exc: IllegalArgumentException) {
                Log.e(TAG, "Use case binding failed", exc)
                // Some devices won't support analysis while capturing video
                // e,g: CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED
                try {
                    analysisActivated = false
                    //viewBinding.videoCaptureButton.visibility = View.VISIBLE
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding without analysis failed", exc)
                    //todo error message
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                //todo error message
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    //----------------------------------- D E T E C T O R ----------------------------------------//

    private fun getImageAnalysis(): ImageAnalysis {
        // Detector
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        val poseDetector = PoseDetection.getClient(options)

        val wm = this.getSystemService(WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay

        Log.d(TAG, "screen width: ${display.width} : ${display.height}")
        Log.d(TAG, "screen aspectRatio: ${display.height / display.width}")

        return ImageAnalysis.Builder()
            //.setTargetResolution(Size(display.width, display.height))
            //.setTargetResolution(Size(513, 513))
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    PoseImageAnalyser(poseDetector, this)
                )
            }
    }

    override fun onPoseAnalysed(pose: Pose, frameSize: Size) {
        //analysisSkeletonView.setLandmarks(pose, frameSize)


        // 获取屏幕宽高
        val screenWidth = frameSize.width.toFloat()
        val screenHeight = frameSize.height.toFloat()

        // 获取关键点
        val landmarks = HashMap<Int, PoseLandmark>()
        pose.allPoseLandmarks.forEach { landmark ->
            landmarks[landmark.landmarkType] = landmark
        }

        // 继续显示骨架数据
        analysisSkeletonView.setLandmarks(pose, frameSize)

        // 使用自定义模型分析
        analyzePoseWithCustomModel(landmarks, screenWidth, screenHeight)

    }

    override fun fullBodyInFrame(inFrame: Boolean) {
        if (inFrame) {
            setRecordingFrame(FrameState.GOOD)
            /*
            if (recordingState == RecordingState.STOPPED) {
                // Launch automatic recording
                countDownStart(COUNT_DOWN_POSTURE_DETECTION).start()
            }
             */

        } else setRecordingFrame(FrameState.WRONG)
    }

    //------------------------------------------ U I ---------------------------------------------//

    private var frameState: FrameState = FrameState.DEFAULT

    private fun setRecordingFrame(state: FrameState) {
        if (state == this.frameState) return


        this.frameState = state
        val frameDrawable = (viewBinding.frame.background as? GradientDrawable) ?: return
        frameDrawable.setStroke(state.widthPx, state.color)


    }

    private enum class FrameState(val widthPx: Int, val color: Int) {
        DEFAULT(20, Color.WHITE),
        HIDDEN(0, Color.TRANSPARENT),
        GOOD(20, Color.GREEN),
        WRONG(20, Color.RED)
    }

    private enum class RecordingState() {
        STOPPED, COUNT_DOWN, RECORDING
    }

//    private fun countDownStart(time: Long): CountDownTimer {
//        viewBinding.textCountdown.visibility = View.VISIBLE
////        viewBinding.videoCaptureButton.text = ""
////        viewBinding.videoCaptureButton.isEnabled = false
//        recordingState = RecordingState.COUNT_DOWN
//
//        return object : CountDownTimer(time + 1_000, 1_000L) {
//            override fun onTick(p0: Long) {
//                if (p0 < 1_000) {
//                    viewBinding.textCountdown.text = "GO_"
//                } else {
//                    viewBinding.textCountdown.text = (p0 / 1_000L).toString()
//                }
//            }
//
//            override fun onFinish() {
//                viewBinding.textCountdown.visibility = View.GONE
//                captureVideo()
//            }
//        }
//    }

    private val filmingDurationCountDown: CountDownTimer =

        object : CountDownTimer(FILMING_DURATION, 1_000L) {
            override fun onTick(p0: Long) {}

            override fun onFinish() {
                stopVideoCapture()
            }
        }

    //--------------------------------- P E R M I S S I O N S ------------------------------------//

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    //--------------------------------------- U T I L S ------------------------------------------//

    private fun analysisAvailable(): Boolean {
        val manager = (getSystemService(CAMERA_SERVICE) as CameraManager)
        val frontCameraId = getFrontFacingCameraId(manager)
        frontCameraId ?: return false
        val characteristics = manager.getCameraCharacteristics(frontCameraId)

        return (characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED)
    }

    private fun getFrontFacingCameraId(cManager: CameraManager): String? {
        for (cameraId in cManager.cameraIdList) {
            val characteristics = cManager.getCameraCharacteristics(cameraId)
            val cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) return cameraId
        }
        return null
    }

    //----------------------------------- C O M P A N I O N --------------------------------------//

    companion object {
        const val TAG = "debuglog"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private const val COUNT_DOWN_POSTURE_DETECTION = 3_000L
        private const val COUNT_DOWN_MANUAL_RECORDING = 5_000L
        private const val FILMING_DURATION = 5_000L

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(Manifest.permission.CAMERA).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
    //----------------------------------- B U T T O N --------------------------------------//
    private fun getCurrentDate() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)
        dateTextView.text = currentDate
    }

    private fun loadModelFile(context: Context, modelFile: String): ByteBuffer {
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelFile)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // 假设你的 landmarks 是一个 HashMap，键是关键点 ID，值是 PoseLandmark 对象
    fun prepareInputData(
        landmarks: HashMap<Int, PoseLandmark>,
        screenWidth: Float,
        screenHeight: Float
    ): FloatArray {
        val keypoints = listOf(
            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
            PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE,
            PoseLandmark.LEFT_HEEL, PoseLandmark.RIGHT_HEEL,
            PoseLandmark.LEFT_FOOT_INDEX, PoseLandmark.RIGHT_FOOT_INDEX
        )



        val inputData = FloatArray(30 * keypoints.size * 4) // 24个特征可能需要额外的填充



        for (frame in 0 until 30) {
            for ((i, keypointId) in keypoints.withIndex()) {
                val landmark = landmarks[keypointId]
                if (landmark != null) {

                    // 归一化坐标
                    val normalizedX = landmark.position.x / screenWidth
                    val normalizedY = landmark.position.y / screenHeight

                    inputData[frame * keypoints.size * 4 + i * 4] = landmark.position.x / screenWidth  // 归一化 x 坐标
                    inputData[frame * keypoints.size * 4 + i * 4 + 1] = landmark.position.y / screenHeight  // 归一化 y 坐标
                    // 这里可以添加其他特征，例如：
                    inputData[frame * keypoints.size * 4 + i * 4 + 2] = 0f // 额外特征
                    inputData[frame * keypoints.size * 4 + i * 4 + 3] = 0f // 额外特征
                    // 打印每个关节点的XY坐标
                    Log.d("PoseLandmarkData", "Frame: $frame, Keypoint: $keypointId, X: $normalizedX, Y: $normalizedY")
                } else {
                    inputData[frame * keypoints.size * 4 + i * 4] = 0f
                    inputData[frame * keypoints.size * 4 + i * 4 + 1] = 0f
                    inputData[frame * keypoints.size * 4 + i * 4 + 2] = 0f
                    inputData[frame * keypoints.size * 4 + i * 4 + 3] = 0f


                    // 打印出使用上一帧数据的情况
                    Log.d("PoseLandmarkData", "Frame: $frame, Keypoint: $keypointId, No Detection - Using Previous Data")
                }
            }
        }
        return inputData
    }




    // 运行模型分析
    fun analyzePoseWithCustomModel(
        landmarks: HashMap<Int, PoseLandmark>,
        screenWidth: Float,
        screenHeight: Float
    ) {

        // 准备输入数据
        val input = prepareInputData(landmarks, screenWidth, screenHeight)
        //Log.d("PoseAnalysis01", "X:${input[0]},Y:${input[1]},${input[2]},${input[3]}")

        // 检查是否有任何检测到的关键点
        if (input.all { it == 0f }) {
            Log.d("ModelOutput", "未检测到关节点")
            return // 如果没有检测到关键点，直接返回
        }

        // 创建一个输入缓冲区
        val inputBuffer = ByteBuffer.allocateDirect(input.size * 4).order(ByteOrder.nativeOrder())
        input.forEach { inputBuffer.putFloat(it) }

        // 进行推理，假设模型输出为[1, 3]的形状
        val output = Array(1) { FloatArray(3) } // 创建一个二维数组以匹配模型输出形状
        interpreter.run(inputBuffer, output)

        // 处理模型输出
        handleModelOutput(output[0]) // 传递一维数组
    }

    // 假设类别索引为 0, 1, 2，这里预期的是第二类 (类别索引 1)
    val EXPECTED_CLASS_INDEX: Int = 1 // 根据您预期的类别索引进行修改

    private fun handleModelOutput(output: FloatArray) {
        // 找到最大概率值的索引
        val maxScoreIndex = output.indices.maxByOrNull { output[it] } ?: -1

        // 检查最大概率的索引是否等于预期类别
        if (maxScoreIndex == EXPECTED_CLASS_INDEX) {
            // 动作正确
            Log.d("ModelOutput", "姿势正确")
        } else if(maxScoreIndex == 0){
            // 动作不正确
            Log.d("ModelOutput", "飛身蹲，实际类别: $maxScoreIndex, 概率: ${output[maxScoreIndex]}")
        }else{
            Log.d("ModelOutput", "錯誤身蹲，实际类别: $maxScoreIndex, 概率: ${output[maxScoreIndex]}")
        }

        // 记录每个类别的概率
        Log.d("ModelOutput", "各类别概率: ${output.joinToString(", ") { String.format("%.2f", it) }}")
    }





}