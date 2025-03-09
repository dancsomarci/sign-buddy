package hu.dancsomarci.signbuddy.hand_recognition.presentation.landmarking

import android.content.Context
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.Landmark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun GestureRecognizerTile_v2(
    modifier: Modifier = Modifier,
    onResult: (Landmark) -> Unit = {},
    viewModel: GestureRecognizerViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val lensFacing = CameraSelector.LENS_FACING_FRONT
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val previewView = remember {
        PreviewView(context)
//            .apply {
//            controller = cameraController
//            cameraController.bindToLifecycle(lifecycleOwner)
//        }
    }

    OnLifecycleEvent { owner, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> owner.lifecycleScope.launch {
                viewModel.onEvent(GestureRecognizerEvent.OnResume)
            }
            Lifecycle.Event.ON_PAUSE -> owner.lifecycleScope.launch {
                viewModel.onEvent(GestureRecognizerEvent.OnPause)
            }
            else -> {}
        }
    }

    LaunchedEffect(lifecycleOwner) { //TODO maybe depend on previewView instead (rotation??)
        // Preview. Only using the 4:3 ratio because this is the closest to our models
        val preview =
            Preview.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder().setAspectRatioStrategy(
                        AspectRatioStrategy(
                            AspectRatio.RATIO_4_3,
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
                            AspectRatio.RATIO_4_3,
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
                        ContextCompat.getMainExecutor(context)
                    ) { imgProxy ->
                        Log.d("test", "Got to ImageAnalyser")
                        viewModel.onEvent(
                            GestureRecognizerEvent.OnRecognize(
                                imgProxy,
                                lensFacing == CameraSelector.LENS_FACING_FRONT
                            )
                        )
                    }
                }

        val cameraxSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraxSelector, preview, imageAnalyzer)

        preview.setSurfaceProvider(previewView.surfaceProvider)
        previewView.scaleType = PreviewView.ScaleType.FIT_START
    }

//    previewView = previewView.apply {
//        scaleType = PreviewView.ScaleType.FIT_START
//    }

    val snackbarHostState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    val overlayView = remember {
        OverlayView(context, null)
    }
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is GestureRecognizerUiEvent.Failure -> scope.launch {
                    snackbarHostState.showSnackbar(
                        message = event.message.asString(context)
                    )
                }
                is GestureRecognizerUiEvent.Success -> {
                    val resultBundle = event.result

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
            }
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(factory = { previewView }, modifier = modifier.fillMaxSize())
        AndroidView(factory = { overlayView }, modifier = modifier.fillMaxSize())
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    //TODO https://stackoverflow.com/questions/66546962/jetpack-compose-how-do-i-refresh-a-screen-when-app-returns-to-foreground
    // TODO test: https://stackoverflow.com/questions/75287804/how-to-stop-mediaplayer-when-the-app-is-backgrounded-in-jetpack-compose
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}
