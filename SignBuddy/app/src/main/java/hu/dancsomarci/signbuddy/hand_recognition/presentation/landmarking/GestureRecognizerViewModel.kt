package hu.dancsomarci.signbuddy.hand_recognition.presentation.landmarking

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.vision.core.RunningMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.dancsomarci.signbuddy.ui.model.UiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestureRecognizerViewModel @Inject constructor(
    @ApplicationContext context: Context
): ViewModel() {

    private val _uiEvent = Channel<GestureRecognizerUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // TODO what happens when this is slow?
    private val handLandmarkerHelper = HandLandmarkerHelper(
        context = context,
        runningMode = RunningMode.LIVE_STREAM,
        minHandDetectionConfidence = HandLandmarkerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE,
        minHandTrackingConfidence = HandLandmarkerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE,
        minHandPresenceConfidence = HandLandmarkerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE,
        currentDelegate = HandLandmarkerHelper.DELEGATE_CPU,
        onError = { errorMsg, _ ->
            viewModelScope.launch {
                _uiEvent.send(
                    GestureRecognizerUiEvent.Failure(
                        UiText.DynamicString(errorMsg)
                    )
                )
            }
        },
        onResults = { resultBundle ->
            viewModelScope.launch {
                _uiEvent.send(
                    GestureRecognizerUiEvent.Success(
                        resultBundle
                    )
                )
            }
        }
    )

    fun onEvent(event: GestureRecognizerEvent){
        when(event){
            is GestureRecognizerEvent.OnRecognize -> {
                viewModelScope.launch {

                }
            }
            is GestureRecognizerEvent.OnResume -> onResume()
            is GestureRecognizerEvent.OnPause -> onPause()
        }
    }

    private fun onResume() {
        viewModelScope.launch {
            if (handLandmarkerHelper.isClose()) {
                handLandmarkerHelper.setupHandLandmarker()
            }
        }
    }

    private fun onPause() {
        viewModelScope.launch {
            handLandmarkerHelper.clearHandLandmarker()
        }
    }
}

sealed class GestureRecognizerUiEvent {
    data class Success(val result: HandLandmarkerHelper.ResultBundle): GestureRecognizerUiEvent()
    data class Failure(val message: UiText): GestureRecognizerUiEvent()
}

sealed class GestureRecognizerEvent {
    data object OnResume: GestureRecognizerEvent()
    data object OnPause: GestureRecognizerEvent()
    data class OnRecognize(val imageProxy: ImageProxy, val isFrontCamera: Boolean): GestureRecognizerEvent()
}
