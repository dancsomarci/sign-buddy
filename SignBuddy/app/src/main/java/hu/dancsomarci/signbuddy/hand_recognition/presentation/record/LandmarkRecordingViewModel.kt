package hu.dancsomarci.signbuddy.hand_recognition.presentation.record

import android.content.Context
import android.util.Log
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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import javax.inject.Inject
import hu.dancsomarci.signbuddy.ml.TestModel
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

@HiltViewModel
class LandmarkRecordingViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val landmarkUseCases: LandmarkSequenceUseCases
): ViewModel() {
    private val _state = MutableStateFlow(LandmarkRecordingState())
    val state = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

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

    private val tfModel = TestModel.newInstance(context)
    private var interpreter: Interpreter? = null

    enum class Delegate {
        CPU, NNAPI
    }
    val compatList = CompatibilityList()

    init {
        val litertBuffer = FileUtil.loadMappedFile(context, "torch_mlp.tflite")
        val options = Interpreter.Options().apply {
            if(compatList.isDelegateSupportedOnThisDevice){
                Log.d("GPU", "Running on GPU:)")
                // if the device has a supported GPU, add the GPU delegate
                val delegateOptions = compatList.bestOptionsForThisDevice
                this.addDelegate(GpuDelegate(delegateOptions))
            } else {
                //Log.d("GPU", "Not on GPU:(")
                // if the GPU is not supported, run on 4 threads
                numThreads = 4
                useNNAPI = false //Delegate.NNAPI
                //useXNNPACK
            }

        }
        interpreter = Interpreter(litertBuffer, options)
    }

    private fun recognizeGesture(lm: Landmark){
        // Convert the filtered list to ByteBuffer
        val filteredLm = lm.landmarks.filterIndexed { index, _ -> (index + 1) % 3 != 0 }
        val byteBuffer = ByteBuffer.allocateDirect(4 * filteredLm.size).order(ByteOrder.nativeOrder())
        filteredLm.forEach { byteBuffer.putFloat(it) }

        // Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 42), DataType.FLOAT32)
        // inputFeature0.loadBuffer(byteBuffer)

//        val outputs = tfModel.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//        val outputArray = outputFeature0.floatArray

        //val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 23), DataType.FLOAT32)

        val inputBuffer = FloatBuffer.allocate(42) // TODO Only output needs to be FloatBuffer apparently
        val outputBuffer = FloatBuffer.allocate(23)
        Log.d("MyViewModel", interpreter!!.getInputTensor(0).dataType().name)
        Log.d("MyViewModel", interpreter!!.getInputTensor(0).shape().joinToString { it.toString() })

        //outputBuffer.rewind() TODO figure out this rewind thing
        interpreter?.run(inputBuffer, outputBuffer)
        Log.d("MyViewModel", "Hello")
        val outputArray = outputBuffer.array()

        Log.d("MyViewModel", outputArray.joinToString { it.toString() })

        // Releases model resources if no longer used.
        // model.close()
    }
}

data class LandmarkRecordingState(
    val isRecording: Boolean = false,
    val recordedLandmarks: List<Landmark> = emptyList()
)

sealed class LandmarkRecordingEvent {
    data object ToggleRecording: LandmarkRecordingEvent()
    data class NewFrameRecognized(val landmark: Landmark) : LandmarkRecordingEvent()
}
