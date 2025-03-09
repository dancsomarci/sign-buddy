package hu.dancsomarci.signbuddy.hand_recognition.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import hu.dancsomarci.signbuddy.auth.data.AuthService
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkSequenceEntity
import hu.dancsomarci.signbuddy.hand_recognition.data.LandmarkService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseLandmarkService @Inject constructor(
    private val authService: AuthService,
    private val firestore: FirebaseFirestore,
): LandmarkService {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val recordings: Flow<List<LandmarkSequenceEntity>>
        get() = authService.currentUser.flatMapLatest { user ->
            if (user == null) flow { emit(emptyList()) }
            else currentCollection(user.id)
                .snapshots()
                .map { snapshot ->
                    snapshot
                        .toObjects(FirebaseLandmarkSequence::class.java)
                        .map {
                            it.asLandmarkSequenceEntity()
                        }
                }
        }

    override suspend fun getSequence(id: String): LandmarkSequenceEntity?{
        return authService.currentUserId?.let {
            currentCollection(it).document(id).get().await()
                .toObject(FirebaseLandmarkSequence::class.java)?.asLandmarkSequenceEntity()
        }
    }

    override suspend fun saveSequence(landmarks: LandmarkSequenceEntity) {
        authService.currentUserId?.let {
            currentCollection(it).add(landmarks.asFirebaseLandmarkSequence()).await()
        }
    }

    override suspend fun deleteSequence(id: String) {
        authService.currentUserId?.let {
            currentCollection(it).document(id).delete().await()
        }
    }

    private fun currentCollection(userId: String) =
        firestore.collection(USER_COLLECTION).document(userId).collection(TODO_COLLECTION)

    companion object {
        private const val USER_COLLECTION = "users"
        private const val TODO_COLLECTION = "recordings"
    }
}