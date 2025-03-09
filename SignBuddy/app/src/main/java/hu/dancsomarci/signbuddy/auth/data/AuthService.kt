package hu.dancsomarci.signbuddy.auth.data

import kotlinx.coroutines.flow.Flow

interface AuthService {
    val currentUserId: String?

    val hasUser: Boolean

    val currentUser: Flow<User?>

    suspend fun signUp(
        email: String, password: String,
    )

    suspend fun authenticate(
        email: String,
        password: String
    )

    fun getUserProfile(): User

    suspend fun updateDisplayName(newDisplayName: String)

    suspend fun authenticateAnonymousAccount()

    suspend fun sendRecoveryEmail(email: String)

    suspend fun deleteAccount()

    suspend fun signOut()
}