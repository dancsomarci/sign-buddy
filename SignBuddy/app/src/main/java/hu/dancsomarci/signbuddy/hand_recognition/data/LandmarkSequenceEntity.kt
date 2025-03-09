package hu.dancsomarci.signbuddy.hand_recognition.data

data class LandmarkSequenceEntity(
    val id: String = "",
    val landmarks: List<Map<String, Float>> = emptyList()
)