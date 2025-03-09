package hu.dancsomarci.signbuddy.ui.model

import android.content.Context
import androidx.annotation.StringRes
import hu.dancsomarci.signbuddy.R.string as StringResources

sealed class UiText {
    data class DynamicString(val value: String): UiText()
    data class StringResource(@StringRes val id: Int): UiText()

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> this.value
            is StringResource -> context.getString(this.id)
        }
    }
}

fun Throwable.toUiText(): UiText {
    val message = this.message.orEmpty()
    return if (message.isBlank()) {
        UiText.StringResource(StringResources.unknown_error)
    } else {
        UiText.DynamicString(message)
    }
}