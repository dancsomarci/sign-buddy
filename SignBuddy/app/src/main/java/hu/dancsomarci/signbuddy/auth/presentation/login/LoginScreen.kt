package hu.dancsomarci.signbuddy.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.dancsomarci.signbuddy.auth.presentation.common.EmailTextField
import hu.dancsomarci.signbuddy.auth.presentation.common.PasswordTextField
import hu.dancsomarci.signbuddy.auth.presentation.util.UiEvent
import hu.dancsomarci.signbuddy.ui.common.VideoPlayer
import kotlinx.coroutines.launch
import hu.dancsomarci.signbuddy.R.string as StringResources
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginUserViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is UiEvent.Success -> {
                    onSuccess()
                }
                is UiEvent.Failure -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message.asString(context)
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VideoPlayer(
                videoUri = "android.resource://hu.dancsomarci.signbuddy/raw/example_video".toUri()
            )
            EmailTextField(
                value = state.email, 
                label = stringResource(id = StringResources.email),
                onValueChange = { viewModel.onEvent(LoginUserEvent.EmailChanged(it)) },
                onDone = {},
                imeAction = ImeAction.Next,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            PasswordTextField(
                value = state.password,
                label = stringResource(id = StringResources.password),
                onValueChange = { viewModel.onEvent(LoginUserEvent.PasswordChanged(it)) },
                onDone = {},
                modifier = Modifier.padding(bottom = 10.dp),
                isVisible = state.passwordVisibility,
                onVisibilityChanged = { viewModel.onEvent(LoginUserEvent.PasswordVisibilityChanged) }
            )
            Button(
                onClick = { viewModel.onEvent(LoginUserEvent.SignIn) },
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(text = stringResource(id = StringResources.button_text_sign_in))
            }
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(text = stringResource(id = StringResources.button_text_no_account))
            }
            Button(
                onClick = { viewModel.onEvent(LoginUserEvent.SignInAnonymously) },
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(text = stringResource(id = StringResources.button_text_anonymous_sign_in))
            }
        }
    }
}