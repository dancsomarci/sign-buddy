//package hu.dancsomarci.signbuddy.hand_recognition.presentation.landmarking
//
//import android.graphics.Bitmap
//import android.view.ViewGroup
//import android.widget.LinearLayout
//import androidx.camera.core.AspectRatio
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageProxy
//import androidx.camera.core.impl.utils.MatrixExt.postRotate
//import androidx.camera.view.CameraController
//import androidx.camera.view.LifecycleCameraController
//import androidx.camera.view.PreviewView
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Matrix
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import com.google.mediapipe.framework.image.BitmapImageBuilder
//import com.google.mediapipe.framework.image.MPImage
//import com.google.mediapipe.tasks.core.OutputHandler
//import com.google.mediapipe.tasks.vision.core.RunningMode
//import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
//import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
//import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
//
//@Composable
//fun TestCode(){
//    val context = LocalContext.current
//
//    val gestureRecognizer = GestureRecognizer(context) { (leftHandLandMarks, rightHandLandmarks) ->
//        //viewModel.updateHandState(Pair(leftHandLandMarks, rightHandLandmarks))
//    }.apply {
//        setupHandLandmarker()
//    }
//
//    GestureTracker { previewView ->
//        startGestureRecognition(
//            context = context,
//            cameraController = cameraController,
//            lifecycleOwner = lifecycleOwner,
//            previewView = previewView,
//            imageAnalyzer = gestureRecognizer
//        )
//    }
//}
//
//@Composable
//private fun GestureTracker(block: (PreviewView) -> Unit) {
//    AndroidView(
//        modifier = Modifier
//            .fillMaxSize(),
//        factory = { context ->
//            PreviewView(context).apply {
//                layoutParams = LinearLayout.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
//                scaleType = PreviewView.ScaleType.FILL_START
//            }.also { previewView -> block(previewView) }
//        }
//    )
//}
//
//private fun startGestureRecognition(
//    context: Context,
//    cameraController: LifecycleCameraController,
//    lifecycleOwner: LifecycleOwner,
//    previewView: PreviewView,
//    imageAnalyzer: ImageAnalysis.Analyzer
//) {
//    cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_4_3)
//    cameraController.setImageAnalysisAnalyzer(
//        ContextCompat.getMainExecutor(context),
//        imageAnalyzer
//    )
//
//    cameraController.bindToLifecycle(lifecycleOwner)
//    previewView.controller = cameraController
//}
//
//class GestureRecognizer_(
//    private val context: Context,
//    private val handLandmarksResult: (Pair<HandGesture, HandGesture>) -> Unit
//) : ImageAnalysis.Analyzer {
//
//    private var isProcessing = false
//    private var handLandmarker: HandLandmarker? = null
//    private var isOverlayTransformInitialized = false
//    private val overlayTransform = Matrix()
//
//    private val resultListener = OutputHandler.ResultListener<HandLandmarkerResult, MPImage> { result, _ ->
//
//        var leftHand = HandGesture(emptyMap())
//        var rightHand = HandGesture(emptyMap())
//        result.landmarks().forEachIndexed { index, handResult ->
//            val handLandMarks = mutableMapOf<HandLandmarkPoint, HandLandmarkPosition>()
//            handResult.forEachIndexed { index, value ->
//                handLandMarks[HandLandmarkPoint.fromIndex(index)] = HandLandmarkPosition(
//                    xNormalized = value.x(),
//                    yNormalized = value.y()
//                )
//            }
//            when (result.handednesses().getOrNull(index)?.first()?.categoryName()) {
//                "Right" -> rightHand = HandGesture(handLandMarks)
//                "Left" -> leftHand = HandGesture(handLandMarks)
//                else -> Unit
//            }
//        }
//
//        if (result.landmarks().isEmpty()) {
//            handLandmarksResult(Pair(HandGesture(emptyMap()), HandGesture(emptyMap())))
//        } else {
//            handLandmarksResult(Pair(leftHand, rightHand))
//        }
//        isProcessing = false
//    }
//
//    private val errorListener = ErrorListener {
//        Timber.e("MediaPipe Error: $it")
//    }
//    fun setupHandLandmarker(isLiveMode: Boolean = true) {
//        val baseOptionsBuilder = BaseOptions.builder()
//            .setDelegate(Delegate.GPU)
//            .setModelAssetPath("hand_landmarker.task")
//
//        val baseOptions = baseOptionsBuilder.build()
//        val optionsBuilder =
//            HandLandmarker.HandLandmarkerOptions.builder()
//                .setBaseOptions(baseOptions)
//                .setNumHands(2)
//                .setResultListener(resultListener)
//                .setErrorListener(errorListener)
//                .setRunningMode(RunningMode.LIVE_STREAM)
//        val options = optionsBuilder.build()
//        handLandmarker = HandLandmarker.createFromOptions(context, options)
//    }
//    override fun analyze(imageProxy: ImageProxy) {
//        if (isProcessing || handLandmarker == null) {
//            imageProxy.close()
//            return
//        }
//        isProcessing = true
//
//        var bitmapBuffer = imageProxy.toBitmap()
//
//        if (!isOverlayTransformInitialized) {
//            overlayTransform.apply {
//                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
//                postScale(-1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat())
//            }
//            isOverlayTransformInitialized = true
//        }
//        imageProxy.close()
//
//        bitmapBuffer = Bitmap.createBitmap(
//            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
//            overlayTransform, false
//        )
//
//        handLandmarker!!.detectAsync(BitmapImageBuilder(bitmapBuffer).build(), System.currentTimeMillis())
//    }
//}
//
//data class HandGesture(
//    val handLandmarks: Map<HandLandmarkPoint, HandLandmarkPosition>
//)
//
//data class HandLandmarkPosition(
//    val xNormalized: Float,
//    val yNormalized: Float
//    // val zNormalized: Float
//)
//
//enum class HandLandmarkPoint(val index: Int) {
//    WRIST(0),
//    THUMB_CMC(1),
//    THUMB_MCP(2),
//    THUMB_IP(3),
//    THUMB_TIP(4),
//    INDEX_FINGER_MCP(5),
//    INDEX_FINGER_PIP(6),
//    INDEX_FINGER_DIP(7),
//    INDEX_FINGER_TIP(8),
//    MIDDLE_FINGER_MCP(9),
//    MIDDLE_FINGER_PIP(10),
//    MIDDLE_FINGER_DIP(11),
//    MIDDLE_FINGER_TIP(12),
//    RING_FINGER_MCP(13),
//    RING_FINGER_PIP(14),
//    RING_FINGER_DIP(15),
//    RING_FINGER_TIP(16),
//    PINKY_MCP(17),
//    PINKY_PIP(18),
//    PINKY_DIP(19),
//    PINKY_TIP(20),
//    UNDEFINED(-1);
//
//    override fun toString(): String {
//        return "HandLandmarkPoint.$name"
//    }
//    companion object {
//        fun fromIndex(index: Int): HandLandmarkPoint {
//            return when (index) {
//                WRIST.index -> WRIST
//                THUMB_CMC.index -> THUMB_CMC
//                THUMB_MCP.index -> THUMB_MCP
//                THUMB_IP.index -> THUMB_IP
//                THUMB_TIP.index -> THUMB_TIP
//                INDEX_FINGER_MCP.index -> INDEX_FINGER_MCP
//                INDEX_FINGER_PIP.index -> INDEX_FINGER_PIP
//                INDEX_FINGER_DIP.index -> INDEX_FINGER_DIP
//                INDEX_FINGER_TIP.index -> INDEX_FINGER_TIP
//                MIDDLE_FINGER_MCP.index -> MIDDLE_FINGER_MCP
//                MIDDLE_FINGER_PIP.index -> MIDDLE_FINGER_PIP
//                MIDDLE_FINGER_DIP.index -> MIDDLE_FINGER_DIP
//                MIDDLE_FINGER_TIP.index -> MIDDLE_FINGER_TIP
//                RING_FINGER_MCP.index -> RING_FINGER_MCP
//                RING_FINGER_PIP.index -> RING_FINGER_PIP
//                RING_FINGER_DIP.index -> RING_FINGER_DIP
//                RING_FINGER_TIP.index -> RING_FINGER_TIP
//                PINKY_MCP.index -> PINKY_MCP
//                PINKY_PIP.index -> PINKY_PIP
//                PINKY_DIP.index -> PINKY_DIP
//                PINKY_TIP.index -> PINKY_TIP
//                else -> UNDEFINED
//            }
//        }
//
//        @Suppress("MagicNumber")
//        fun getConnectedPoints(): List<Pair<HandLandmarkPoint, HandLandmarkPoint>> {
//            return listOf(
//                0 to 1, 0 to 5, 0 to 17,
//                1 to 2, 2 to 3, 3 to 4,
//                5 to 6, 6 to 7, 7 to 8,
//                9 to 10, 10 to 11, 11 to 12,
//                13 to 14, 14 to 15, 15 to 16,
//                17 to 18, 18 to 19, 19 to 20,
//                5 to 9, 9 to 13, 13 to 17
//            ).map {
//                Pair(fromIndex(it.first), fromIndex(it.second))
//            }
//        }
//    }
//}