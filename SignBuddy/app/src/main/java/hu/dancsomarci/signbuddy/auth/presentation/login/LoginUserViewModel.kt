package hu.dancsomarci.signbuddy.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.dancsomarci.signbuddy.auth.domain.usecases.IsEmailValidUseCase
import hu.dancsomarci.signbuddy.auth.data.AuthService
import hu.dancsomarci.signbuddy.ui.model.UiText
import hu.dancsomarci.signbuddy.ui.model.toUiText
import hu.dancsomarci.signbuddy.auth.presentation.util.UiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import hu.dancsomarci.signbuddy.R.string as StringResources

@HiltViewModel
class LoginUserViewModel @Inject constructor(
    private val authService: AuthService,
    private val isEmailValid: IsEmailValidUseCase,
): ViewModel() {

    private val _state = MutableStateFlow(LoginUserState())
    val state = _state.asStateFlow()

    private val email get() = state.value.email

    private val password get() = state.value.password

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        if (authService.hasUser and !authService.getUserProfile().isAnonymous){
            viewModelScope.launch(Dispatchers.IO){
                _uiEvent.send(UiEvent.Success)
            }
        }
    }

    fun onEvent(event: LoginUserEvent) {
        when(event) {
            is LoginUserEvent.EmailChanged -> {
                val newEmail = event.email.trim()
                _state.update { it.copy(email = newEmail) }
            }
            is LoginUserEvent.PasswordChanged -> {
                val newPassword = event.password.trim()
                _state.update { it.copy(password = newPassword) }
            }
            is LoginUserEvent.PasswordVisibilityChanged -> {
                _state.update { it.copy(passwordVisibility = !state.value.passwordVisibility) }
            }
            is LoginUserEvent.SignIn -> {
                onSignIn()
            }
            is LoginUserEvent.SignInAnonymously ->{
                onAnonymousSignIn()
            }
        }
    }

    private fun onAnonymousSignIn() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                authService.authenticateAnonymousAccount()
                _uiEvent.send(UiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.Failure(e.toUiText()))
            }
        }
    }

    private fun onSignIn() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!isEmailValid(email)) {
                    _uiEvent.send(
                        UiEvent.Failure(UiText.StringResource(StringResources.unknown_error))
                    )
                } else {
                    if (password.isBlank()) {
                        _uiEvent.send(
                            UiEvent.Failure(UiText.StringResource(StringResources.unknown_error))
                        )
                    } else {
                        authService.authenticate(email,password)
                        _uiEvent.send(UiEvent.Success)
                    }
                }
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.Failure(e.toUiText()))
            }
        }
    }
}


data class LoginUserState(
    val email: String = "",
    val password: String = "",
    val passwordVisibility: Boolean = false
)

sealed class LoginUserEvent {
    data class EmailChanged(val email: String): LoginUserEvent()
    data class PasswordChanged(val password: String): LoginUserEvent()
    data object PasswordVisibilityChanged: LoginUserEvent()
    data object SignIn: LoginUserEvent()
    data object SignInAnonymously: LoginUserEvent()
}