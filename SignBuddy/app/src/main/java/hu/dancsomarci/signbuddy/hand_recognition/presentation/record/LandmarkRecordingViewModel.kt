package hu.dancsomarci.signbuddy.hand_recognition.presentation.record

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import hu.dancsomarci.signbuddy.auth.presentation.util.UiEvent
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.Landmark
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.asLandmarkSequence
import hu.dancsomarci.signbuddy.hand_recognition.domain.usecases.LandmarkSequenceUseCases
import hu.dancsomarci.signbuddy.ui.model.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LandmarkRecordingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val landmarkUseCases: LandmarkSequenceUseCases
): ViewModel() {
    private val _state = MutableStateFlow(LandmarkRecordingState(recognizedCharacter="d"))
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val model: SignRecognitionHelper = SignRecognitionHelper(context)

    fun onEvent(event: LandmarkRecordingEvent){
        when(event){
            is LandmarkRecordingEvent.NewFrameRecognized -> {
                if (state.value.isRecording){
                    _state.update{it.copy(
                        recordedLandmarks = it.recordedLandmarks + event.landmark
                    )}
                }
                recognizeGesture(event.landmark)
            }
            is LandmarkRecordingEvent.ToggleRecording -> {
                if (state.value.isRecording){
                    onSave()
                    _state.update{it.copy(
                        isRecording = false,
                    )}
                } else {
                    _state.update{it.copy(
                        isRecording = true,
                        recordedLandmarks = emptyList()
                    )}
                }
            }
        }
    }

    private fun onSave() {
        viewModelScope.launch {
            try {
                landmarkUseCases.saveSequence(state.value.recordedLandmarks.asLandmarkSequence())
                _uiEvent.send(UiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.Failure(e.toUiText()))
            }
        }
    }

    private fun recognizeGesture(lm: Landmark){
        viewModelScope.launch {
            model.recognizeGesture()
        }
    }
}

data class LandmarkRecordingState(
    val isRecording: Boolean = false,
    val recognizedCharacter: String? = null,
    val confidence: Float = 0F,
    val recordedLandmarks: List<Landmark> = emptyList()
)

sealed class LandmarkRecordingEvent {
    data object ToggleRecording: LandmarkRecordingEvent()
    data class NewFrameRecognized(val landmark: Landmark) : LandmarkRecordingEvent()
}
