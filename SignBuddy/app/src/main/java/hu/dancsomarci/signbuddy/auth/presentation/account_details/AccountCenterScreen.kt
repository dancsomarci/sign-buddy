package hu.dancsomarci.signbuddy.auth.presentation.account_details

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import hu.dancsomarci.signbuddy.R
import hu.dancsomarci.signbuddy.auth.data.User
import hu.dancsomarci.signbuddy.auth.presentation.common.TodoAppBar
import hu.dancsomarci.signbuddy.auth.presentation.util.UiEvent
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun AccountCenterScreen(
    afterLogout: () -> Unit = {},
    onSignIn: ()->Unit = {},
    onSignUp: ()->Unit = {},
    onNavigateBack: ()->Unit = {},
    viewModel: AccountCenterViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState(initial = User())
    val provider = user.provider.replaceFirstChar { it.titlecase(Locale.getDefault()) }

    val snackbarHostState = SnackbarHostState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is UiEvent.Success -> {
                    afterLogout()
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

    Scaffold (
        topBar = {
            TodoAppBar(
                title = stringResource(R.string.account_center),
                actions = {  },
                onNavigateBack = onNavigateBack
            )
        },
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            DisplayNameCard(user.displayName) {
                viewModel.onEvent(AccountCenterEvent.UpdateDisplayName(it))
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            Card(modifier = Modifier.card()) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
                    if (!user.isAnonymous) {
                        Text(
                            text = String.format(stringResource(R.string.profile_email), user.email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    Text(
                        text = String.format(stringResource(R.string.profile_uid), user.id),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )

                    Text(
                        text = String.format(stringResource(R.string.profile_provider), provider),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))

            if (user.isAnonymous) {
                AccountCenterCard(stringResource(R.string.sign_in), Icons.Filled.Face, Modifier.card()) {
                    onSignIn()
                }

                AccountCenterCard(stringResource(R.string.sign_up), Icons.Filled.AccountCircle, Modifier.card()) {
                    onSignUp()
                }
            } else {
                ExitAppCard { viewModel.onEvent(AccountCenterEvent.SignOut) }
                RemoveAccountCard { viewModel.onEvent(AccountCenterEvent.DeleteAccount) }
            }
        }
    }
}