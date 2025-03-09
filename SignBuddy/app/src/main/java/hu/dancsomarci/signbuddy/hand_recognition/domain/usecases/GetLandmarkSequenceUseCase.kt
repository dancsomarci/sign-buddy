package hu.dancsomarci.signbuddy.hand_recognition.domain.usecases

import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.asLandmarkSequence
import javax.inject.Inject

class GetLandmarkSequenceUseCase @Inject constructor(
    private val landmarkService: LandmarkService
) {
    suspend operator fun invoke(id: String) {
        landmarkService.getSequence(id)?.asLandmarkSequence()
    }
}
