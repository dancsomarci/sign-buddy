package hu.dancsomarci.signbuddy.hand_recognition.domain.usecases

import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import javax.inject.Inject

class DeleteLandmarkSequenceUseCase @Inject constructor(
    private val landmarkService: LandmarkService
) {
    suspend operator fun invoke(id: String) {
        landmarkService.deleteSequence(id)
    }
}
