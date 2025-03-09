package hu.dancsomarci.signbuddy.hand_recognition.domain.model

import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkSequenceEntity

data class LandmarkSequence(
    val id: String = "",
    val landmarks: List<List<Float>> = emptyList()
)

fun LandmarkSequenceEntity.asLandmarkSequence(): LandmarkSequence = LandmarkSequence(
    id = id,
    landmarks = landmarks.map { it.values.toList() }
)

fun LandmarkSequence.asLandmarkSequenceEntity(): LandmarkSequenceEntity = LandmarkSequenceEntity(
    id = id,
    landmarks = landmarks.map { it.mapIndexed { index, fl -> index.toString() to fl }.toMap()  }
)