package hu.dancsomarci.signbuddy.hand_recognition.data

import kotlinx.coroutines.flow.Flow

interface LandmarkService {
    val recordings: Flow<List<LandmarkSequenceEntity>>

    suspend fun getSequence(id: String): LandmarkSequenceEntity?

    suspend fun saveSequence(landmarks: LandmarkSequenceEntity)

    suspend fun deleteSequence(id: String)
}
