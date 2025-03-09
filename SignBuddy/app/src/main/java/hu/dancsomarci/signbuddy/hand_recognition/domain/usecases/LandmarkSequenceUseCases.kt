package hu.dancsomarci.signbuddy.hand_recognition.domain.usecases

class LandmarkSequenceUseCases(
    val saveSequence: SaveLandmarkSequenceUseCase,
    val loadSequence: GetLandmarkSequenceUseCase,
    val loadSequences: LoadLandmarkSequencesUseCase,
    val deleteSequence: DeleteLandmarkSequenceUseCase
)