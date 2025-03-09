package hu.dancsomarci.signbuddy.hand_recognition.data.firebase

import com.google.firebase.firestore.DocumentId
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkSequenceEntity

data class FirebaseLandmarkSequence(
    @DocumentId val id: String = "",
    val landmarks: List<Map<String, Float>> = emptyList()
)

fun FirebaseLandmarkSequence.asLandmarkSequenceEntity() = LandmarkSequenceEntity(
    id = id,
    landmarks = landmarks
)

fun LandmarkSequenceEntity.asFirebaseLandmarkSequence() = FirebaseLandmarkSequence(
    id = id,
    landmarks = landmarks
)