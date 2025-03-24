package hu.dancsomarci.signbuddy.hand_recognition.presentation.landmarking

import android.content.Context
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.Landmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.lifecycle.Lifecycle

@Composable
fun GestureRecognizerTile(
    modifier: Modifier = Modifier,
    onResult: (Landmark) -> Unit = {}
){
    GestureRecognizerBuilder(
        onResult = onResult
    ).Build(modifier=modifier)
}

class GestureRecognizerBuilder(
    private val onResult: (Landmark) -> Unit = { }
) {
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper

    @Composable
    fun Build(modifier: Modifier = Modifier) {
        val lensFacing = CameraSelector.LENS_FACING_FRONT
        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        val context = LocalContext.current
        val previewView = remember { PreviewView(context) }
        val overlayView = remember { OverlayView(context, null) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(lensFacing) {
            handLandmarkerHelper = HandLandmarkerHelper(
                context = context,
                runningMode = RunningMode.LIVE_STREAM,
                minHandDetectionConfidence = HandLandmarkerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE,
                minHandTrackingConfidence = HandLandmarkerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE,
                minHandPresenceConfidence = HandLandmarkerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE,
                currentDelegate = HandLandmarkerHelper.DELEGATE_CPU,
                onError = {msg, code ->
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "$msg ErrorCode: $code", Toast.LENGTH_SHORT).show()
                    }
                },
                onResults = { resultBundle ->
                    val detectionResult = resultBundle.results[0]
                    val detectedLandmarks = detectionResult.landmarks()
                    if (detectedLandmarks.size != 0){
                        val xCords = detectionResult.landmarks()[0].map { it.x() }
                        val yCords = detectionResult.landmarks()[0].map { it.y() }
                        onResult(Landmark(landmarks = xCords + yCords))
                    }

                    scope.launch(Dispatchers.Main) {
                        overlayView.setResults(
                            detectionResult,
                            resultBundle.inputImageHeight,
                            resultBundle.inputImageWidth
                        )

                        // Force a redraw
                        overlayView.invalidate()
                    }
                }
            )

            // Preview. Only using the 4:3 ratio because this is the closest to our models
            val preview =
                Preview.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder().setAspectRatioStrategy(
                            AspectRatioStrategy(
                                AspectRatio.RATIO_16_9,
                                AspectRatioStrategy.FALLBACK_RULE_AUTO
                            )
                        ).build()
                    )
                    .setTargetRotation(previewView.display.rotation)
                    .build()

            // ImageAnalysis. Using RGBA 8888 to match how our models work
            val imageAnalyzer =
                ImageAnalysis.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder().setAspectRatioStrategy(
                            AspectRatioStrategy(
                                AspectRatio.RATIO_16_9,
                                AspectRatioStrategy.FALLBACK_RULE_AUTO
                            )
                        ).build()
                    )
                    .setTargetRotation(previewView.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    // The analyzer can then be assigned to the instance
                    .also {
                        it.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            handLandmarkerHelper::detectLiveStream
                        )
                    }

            val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalyzer)

            preview.surfaceProvider = previewView.surfaceProvider

            previewView.apply {
                scaleType = PreviewView.ScaleType.FIT_START
            }
            previewView.scaleX = 1.2f
            previewView.scaleY = 1.2f
            overlayView.scaleX = 1.2f
            overlayView.scaleY = 1.2f
        }

        Box(contentAlignment = Alignment.Center) {
            AndroidView(factory = { previewView })
            AndroidView(factory = { overlayView })
        }
    }

    private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
        suspendCoroutine { continuation ->
            ProcessCameraProvider.getInstance(this).also { cameraProvider ->
                cameraProvider.addListener({
                    continuation.resume(cameraProvider.run { get() })
                }, ContextCompat.getMainExecutor(this))
            }
        }
}