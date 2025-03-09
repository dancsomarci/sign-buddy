package hu.dancsomarci.signbuddy.auth.presentation.account_details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.dancsomarci.signbuddy.auth.data.AuthService
import hu.dancsomarci.signbuddy.auth.data.User
import hu.dancsomarci.signbuddy.auth.presentation.util.UiEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountCenterViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel() {
    // Backing property to avoid state updates from other classes
    private val _user = MutableStateFlow(User())
    val user: StateFlow<User> = _user.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        launchCatching {
            _user.value = authService.getUserProfile()
        }
    }

    fun onEvent(event: AccountCenterEvent){
        launchCatching {
            when(event){
                AccountCenterEvent.DeleteAccount -> deleteAccount()
                AccountCenterEvent.SignOut -> signOut()
                is AccountCenterEvent.UpdateDisplayName -> updateDisplayName(event.newDisplayName)
            }
        }
    }

    private suspend fun updateDisplayName(newDisplayName: String) {
        authService.updateDisplayName(newDisplayName)
        _user.value = authService.getUserProfile()
    }

    private suspend fun signOut() {
        authService.signOut()
        _uiEvent.send(UiEvent.Success)
    }

    private suspend fun deleteAccount() {
        authService.deleteAccount()
        _uiEvent.send(UiEvent.Success)
    }

    //TODO make all viewmodels work with this pattern
    private fun launchCatching(block: suspend CoroutineScope.() -> Unit) =
        viewModelScope.launch(
            CoroutineExceptionHandler { _, throwable ->
                Log.d(ERROR_TAG, throwable.message.orEmpty())
            },
            block = block
        )

    companion object {
        const val ERROR_TAG = "SIGN BUDDY APP ERROR"
    }
}

sealed class AccountCenterEvent {
    data object DeleteAccount: AccountCenterEvent()
    data object SignOut: AccountCenterEvent()
    data class UpdateDisplayName(val newDisplayName: String) : AccountCenterEvent()
}
