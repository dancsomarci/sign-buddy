package hu.dancsomarci.signbuddy.auth.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthService @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthService {

    override val currentUserId: String? get() = firebaseAuth.currentUser?.uid
    override val hasUser: Boolean get() = firebaseAuth.currentUser != null
    override val currentUser: Flow<User?>
        get() = callbackFlow {
        this.trySend(currentUserId?.let { User(it) })
        val listener =
            FirebaseAuth.AuthStateListener { auth ->
                this.trySend(auth.currentUser?.toSignBuddyUser())
            }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun getUserProfile(): User {
        return firebaseAuth.currentUser.toSignBuddyUser()
    }

    override suspend fun updateDisplayName(newDisplayName: String) {
        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(newDisplayName)
            .build()
        firebaseAuth.currentUser?.updateProfile(profileChangeRequest)?.await()
    }

    override suspend fun signUp(email: String, password: String) {
        if (hasUser and getUserProfile().isAnonymous){
            // link accounts
            val credential = EmailAuthProvider.getCredential(email, password)
            firebaseAuth.currentUser!!.linkWithCredential(credential).await()
        } else{
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener {  result ->
                    val user = result.user
                    val profileChangeRequest = UserProfileChangeRequest.Builder()
                        .setDisplayName(user?.email?.substringBefore('@'))
                        .build()
                    user?.updateProfile(profileChangeRequest)
                }.await()
        }
    }

    override suspend fun authenticate(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun authenticateAnonymousAccount() {
        firebaseAuth.signInAnonymously().await()
    }

    override suspend fun sendRecoveryEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    override suspend fun deleteAccount() {
        firebaseAuth.currentUser!!.delete().await()
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser?.toSignBuddyUser(): User {
        return if (this == null) User() else User(
            id = this.uid,
            email = this.email ?: "",
            provider = this.providerId,
            displayName = this.displayName ?: "",
            isAnonymous = this.isAnonymous
        )
    }
}