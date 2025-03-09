package hu.dancsomarci.signbuddy.hand_recognition.presentation.model

import hu.dancsomarci.signbuddy.hand_recognition.domain.model.LandmarkSequence

data class LandmarkSequenceUi(
    val id: String = "",
    val landmarks: List<List<Float>> = emptyList()
)

fun LandmarkSequenceUi.asLandmarkSequence() = LandmarkSequence(
    id = id,
    landmarks = landmarks
)

fun LandmarkSequence.asLandmarkSequenceUi() = LandmarkSequenceUi(
    id = id,
    landmarks = landmarks
)