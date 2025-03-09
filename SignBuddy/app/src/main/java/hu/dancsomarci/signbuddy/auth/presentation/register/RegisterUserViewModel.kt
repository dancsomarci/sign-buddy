package hu.dancsomarci.signbuddy.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.dancsomarci.signbuddy.auth.domain.usecases.IsEmailValidUseCase
import hu.dancsomarci.signbuddy.auth.domain.usecases.PasswordsMatchUseCase
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
class RegisterUserViewModel @Inject constructor(
    private val authService: AuthService,
    private val isEmailValid: IsEmailValidUseCase,
    private val passwordsMatch: PasswordsMatchUseCase
): ViewModel() {
    
    private val _state = MutableStateFlow(RegisterUserState())
    val state = _state.asStateFlow()

    private val email get() = state.value.email

    private val password get() = state.value.password

    private val confirmPassword get() = state.value.confirmPassword

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun onEvent(event: RegisterUserEvent) {
        when(event) {
            is RegisterUserEvent.EmailChanged -> {
                val newEmail = event.email.trim()
                _state.update { it.copy(email = newEmail) }
            }
            is RegisterUserEvent.PasswordChanged -> {
                val newPassword = event.password.trim()
                _state.update { it.copy(password = newPassword) }
            }
            is RegisterUserEvent.ConfirmPasswordChanged -> {
                val newConfirmPassword = event.password.trim()
                _state.update { it.copy(confirmPassword = newConfirmPassword) }
            }
            RegisterUserEvent.PasswordVisibilityChanged -> {
                _state.update { it.copy(passwordVisibility = !state.value.passwordVisibility) }
            }
            RegisterUserEvent.ConfirmPasswordVisibilityChanged -> {
                _state.update { it.copy(confirmPasswordVisibility = !state.value.confirmPasswordVisibility) }
            }
            RegisterUserEvent.SignUp -> {
                onSignUp()
            }
        }
    }

    private fun onSignUp() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!isEmailValid(email)) {
                    _uiEvent.send(
                        UiEvent.Failure(UiText.StringResource(StringResources.unknown_error))
                    )
                } else {
                    if (!passwordsMatch(password,confirmPassword) && password.isNotBlank()) {
                        _uiEvent.send(
                            UiEvent.Failure(UiText.StringResource(StringResources.unknown_error))
                        )
                    } else {
                        authService.signUp(email, password)
                        _uiEvent.send(UiEvent.Success)
                    }
                }
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.Failure(e.toUiText()))
            }
        }
    }
}


data class RegisterUserState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisibility: Boolean = false,
    val confirmPasswordVisibility: Boolean = false
)

sealed class RegisterUserEvent {
    data class EmailChanged(val email: String): RegisterUserEvent()
    data class PasswordChanged(val password: String): RegisterUserEvent()
    data class ConfirmPasswordChanged(val password: String): RegisterUserEvent()
    data object PasswordVisibilityChanged: RegisterUserEvent()
    data object ConfirmPasswordVisibilityChanged: RegisterUserEvent()
    data object SignUp: RegisterUserEvent()
}