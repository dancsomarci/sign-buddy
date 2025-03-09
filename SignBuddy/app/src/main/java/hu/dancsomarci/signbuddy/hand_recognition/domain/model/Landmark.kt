package hu.dancsomarci.signbuddy.hand_recognition.domain.model

data class Landmark(val landmarks: List<Float>)

fun List<Landmark>.asLandmarkSequence(): LandmarkSequence = LandmarkSequence(
    landmarks = map { it -> it.landmarks }
)