package hu.dancsomarci.signbuddy.hand_recognition.domain.usecases

import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.LandmarkSequence
import hu.dancsomarci.signbuddy.hand_recognition.domain.model.asLandmarkSequenceEntity
import javax.inject.Inject

class SaveLandmarkSequenceUseCase @Inject constructor(
    private val landmarkService: LandmarkService
) {
    suspend operator fun invoke(landmarkSequence: LandmarkSequence) {
        landmarkService.saveSequence(landmarkSequence.asLandmarkSequenceEntity())
    }
}