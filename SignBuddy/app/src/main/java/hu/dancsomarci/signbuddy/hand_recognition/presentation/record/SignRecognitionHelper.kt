package hu.dancsomarci.signbuddy.hand_recognition.presentation.record

import android.content.Context
import android.os.SystemClock
import android.util.Log
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.Landmark
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
import kotlin.math.abs
import kotlin.math.exp

class SignRecognitionHelper(private val context: Context) {
    companion object {
        private const val TAG = "SignRecognitionHelper"
        private const val MODEL_PATH = "tf_mobile_gnn.tflite"
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

    private fun preprocessLandmarks(lm: Landmark): FloatBuffer {
        require(lm.landmarks.size == 42) { "Expected 42 values (21 landmarks × 2 coords), got ${lm.landmarks.size}" }

        val (_, nodes, dim) = interpreter!!.getInputTensor(0).shape() // should be (1, 21, 2)

        // Reshape into (21, 2)
        val xy = Array(21) { i ->
            floatArrayOf(lm.landmarks[i * 2], lm.landmarks[i * 2 + 1])
        }

        // Compute centroid
        val centroidX = xy.map { it[0] }.average().toFloat()
        val centroidY = xy.map { it[1] }.average().toFloat()

        // Center
        val centeredXY = Array(21) { i ->
            floatArrayOf(xy[i][0] - centroidX, xy[i][1] - centroidY)
        }

        // Max distances
        val maxX = centeredXY.maxOf { abs(it[0]) }
        val maxY = centeredXY.maxOf { abs(it[1]) }

        val scaleX = if (maxX != 0f) 1f / maxX else 1f
        val scaleY = if (maxY != 0f) 1f / maxY else 1f

        // Allocate buffer with correct shape (1, 21, 2)
        val inputBuffer = FloatBuffer.allocate(1 * nodes * dim)

        // Fill buffer with scaled values
        for (i in 0 until 21) {
            inputBuffer.put(centeredXY[i][0] * scaleX) // x
            inputBuffer.put(centeredXY[i][1] * scaleY) // y
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { exp((it - maxLogit).toDouble()) }  // subtract max for numerical stability
        val sumExps = exps.sum()
        return exps.map { (it / sumExps).toFloat() }.toFloatArray()
    }

    private val alphabet = ('A'..'Z')
        .filterNot { it in listOf('J', 'Z', 'R') }
        .toList()

    private fun postProcess(output: FloatArray): Pair<Char, Float> {
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        val pred = if (maxIndex in alphabet.indices) alphabet[maxIndex] else '?'
        val probs = softmax(output)
        val confidence = if (maxIndex in probs.indices) probs[maxIndex] else 0f
        return pred to confidence
    }

    suspend fun recognizeGesture(lm: Landmark) {
        if (interpreter == null) return

        withContext(Dispatchers.IO) {
            val inputBuffer = preprocessLandmarks(lm)

            val startTime = SystemClock.uptimeMillis()
            val outputBuffer = inference(inputBuffer)
            val inferenceTime = SystemClock.uptimeMillis() - startTime

            val (predictedChar, confidence) = postProcess(outputBuffer)

            Log.d(TAG, "Output: $predictedChar (took ${inferenceTime}ms)")
            _result.emit(
                Result(
                    gesture = predictedChar.toString(),
                    inferenceTime = inferenceTime,
                    confidence = confidence
                )
            )
        }
    }

    private fun inference(inputBuffer: FloatBuffer): FloatArray {
        val (_, numGestures) = interpreter!!.getOutputTensor(0).shape()

        val outputBuffer = FloatBuffer.allocate(numGestures)
        interpreter?.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        return outputBuffer.array()
    }

    data class Result(
        val gesture: String? = null,
        val inferenceTime: Long = 0L,
        val confidence: Float = 1F
    )

    enum class Delegate {
        CPU, NNAPI, GPU
    }
}