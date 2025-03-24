package hu.dancsomarci.signbuddy.hand_recognition.presentation.list_recordings

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.dancsomarci.signbuddy.R
import hu.dancsomarci.signbuddy.hand_recognition.presentation.common.BottomNavBar
import hu.dancsomarci.signbuddy.hand_recognition.presentation.common.TabBarItem
import hu.dancsomarci.signbuddy.ui.common.SwipeToDismissListItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ResourceType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingsScreen(
    tabBarItems:  List<TabBarItem>,
    selectedTabIndex: Int,
    onTabSelected: (Int)->Unit,
    onNavigateToAccountCenter: () -> Unit,
    viewModel: RecordingsViewModel = hiltViewModel()
){
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = {
                        onNavigateToAccountCenter()
                    }) {
                        Icon(Icons.Filled.Person, "Account center")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                tabBarItems = tabBarItems,
                onTabSelected = onTabSelected,
                selectedTabIndex = selectedTabIndex
            )
        }
    ){ padding ->
        if (state.recordings.isEmpty()){
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.raw.img),
                    contentDescription = "empty list"
                )
                Text(text = "No recordings found:(")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(0.98f)
                    .padding(padding)
                    .clip(RoundedCornerShape(5.dp))
            ) {
                items(state.recordings.size) { i ->
                    SwipeToDismissListItem(
                        onEndToStart={ viewModel.deleteRecording(i) }
                    ){
                        val recording = state.recordings[i]
                        ListItem(
                            headlineContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = recording.id)
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .padding(start = 10.dp),
                                    )
                                }
                            },
                            supportingContent = {
                                Text(text = "Sequence ${i + 1}: length: ${recording.landmarks.size}")
                            }
                        )
                    }

                    if (i != state.recordings.lastIndex) {
                        HorizontalDivider(
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        )
                    }
                }
            }
        }
    }
}

