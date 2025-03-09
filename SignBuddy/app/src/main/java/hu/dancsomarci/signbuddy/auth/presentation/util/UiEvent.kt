package hu.dancsomarci.signbuddy.auth.presentation.util

import hu.dancsomarci.signbuddy.ui.model.UiText

sealed class UiEvent {
    data object Success: UiEvent()
    data class Failure(val message: UiText): UiEvent()
}