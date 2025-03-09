package hu.dancsomarci.signbuddy.hand_recognition.domain.usecases

import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.LandmarkSequence
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.asLandmarkSequence
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

class LoadLandmarkSequencesUseCase @Inject constructor(
    private val landmarkService: LandmarkService
) {
    suspend operator fun invoke(): Result<List<LandmarkSequence>> {
        return try {
            val todos = landmarkService.recordings.first()
            Result.success(todos.map { it.asLandmarkSequence() })
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}
