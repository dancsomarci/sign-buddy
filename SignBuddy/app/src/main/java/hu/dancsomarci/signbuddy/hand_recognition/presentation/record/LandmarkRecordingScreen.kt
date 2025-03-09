package hu.dancsomarci.signbuddy.hand_recognition.presentation.record

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.dancsomarci.signbuddy.auth.presentation.util.UiEvent
import hu.dancsomarci.signbuddy.hand_recognition.presentation.common.BottomNavBar
import hu.dancsomarci.signbuddy.hand_recognition.presentation.common.TabBarItem
import hu.dancsomarci.signbuddy.hand_recognition.presentation.landmarking.GestureRecognizerTile
import kotlinx.coroutines.launch
import hu.dancsomarci.signbuddy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkRecordingScreen(
    tabBarItems:  List<TabBarItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int)->Unit,
    viewModel: LandmarkRecordingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { uiEvent ->
            scope.launch {
                hostState.showSnackbar(
                    when (uiEvent) {
                        is UiEvent.Success -> "Successfully saved landmarks!"
                        is UiEvent.Failure -> uiEvent.message.asString(context)
                    }
                )
            }
        }
    }

    CameraPermissionContainer{
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Sign Buddy") },
                )
            },
            bottomBar = {
                BottomNavBar(
                    tabBarItems = tabBarItems,
                    onTabSelected = onTabSelected,
                    selectedTabIndex = selectedTabIndex
                )
            },
            floatingActionButton = {
                DraggableStickyFloatingActionButton(
                    color = if (state.isRecording) Color.Red else Color.Magenta,
                    onClick = { viewModel.onEvent(LandmarkRecordingEvent.ToggleRecording) }
                ){
                    Icon(
                        painter = painterResource(id = R.drawable.record),
                        contentDescription = null // decorative element
                    )
                }
            }
        ) { innerPadding ->
            GestureRecognizerTile(
                modifier = Modifier
                    .padding(innerPadding),
                onResult = {
                viewModel.onEvent(LandmarkRecordingEvent.NewFrameRecognized(it))
            })
        }
    }
}

