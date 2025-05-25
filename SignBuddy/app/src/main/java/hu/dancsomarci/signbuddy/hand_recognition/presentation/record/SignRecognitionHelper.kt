package hu.dancsomarci.signbuddy.hand_recognition.presentation.record

import android.content.Context
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.FloatBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate

class SignRecognitionHelper(private val context: Context) {
    companion object {
        private const val TAG = "SignRecognitionHelper"
        private const val MODEL_PATH = "gat_test2.tflite"
    }

    private var interpreter: Interpreter? = null

    private val _result = MutableSharedFlow<Result>(
        extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Result>
        get() = _result

    init {
        initModel()
    }

    fun initModel(delegate: Delegate = Delegate.GPU) {
        val litertBuffer = FileUtil.loadMappedFile(context, MODEL_PATH)
        Log.i(TAG, "Done creating TFLite buffer from $MODEL_PATH model")

        val compatList = CompatibilityList()
        val options = Interpreter.Options().apply {
            if(compatList.isDelegateSupportedOnThisDevice){
                addDelegate(GpuDelegate(compatList.bestOptionsForThisDevice))
                Log.d(TAG, "Selected GPU")
            } else {
                Log.d(TAG, "Selected CPU")
                numThreads = 4
                useNNAPI = delegate == Delegate.NNAPI
            }
        }

        interpreter = Interpreter(litertBuffer, options)
    }

    suspend fun recognizeGesture() {
        if (interpreter == null) return

        withContext(Dispatchers.IO) {
            val startTime = SystemClock.uptimeMillis()

            // TODO preprocess
            // val filteredLm = lm.landmarks.filterIndexed { index, _ -> (index + 1) % 3 != 0 }
            // val buffer = FloatBuffer.allocate(filteredLm.size)
            // filteredLm.forEach { buffer.put(it) }

            val outputBitmap = inference()
            val inferenceTime = SystemClock.uptimeMillis() - startTime

            // TODO post process

            _result.emit(Result(gesture = "?", inferenceTime = inferenceTime))
        }
    }

    private fun inference() {
        val (_, nodes, dim) = interpreter!!.getInputTensor(0).shape()
        val (_, numGestures) = interpreter!!.getOutputTensor(0).shape()

        val inputBuffer = FloatBuffer.allocate(nodes * dim)
        val outputBuffer = FloatBuffer.allocate(numGestures)

        interpreter?.run(inputBuffer, outputBuffer)
        // TODO Test
        // outputBuffer.rewind()
        val outputArray = outputBuffer.array()
        Log.d(TAG, "${outputArray[0]} ${outputArray[1]} ${outputArray[2]}")
    }

    data class Result(
        val gesture: String? = null,
        val inferenceTime: Long = 0L
    )

    enum class Delegate {
        CPU, NNAPI, GPU
    }
}