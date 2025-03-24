package hu.dancsomarci.signbuddy.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//Source: https://medium.com/@renaud.mathieu/discovering-material3-for-android-swipetodismissbox-f64905b2b677
@Composable
fun SwipeToDismissListItem(
    modifier: Modifier = Modifier,
    onEndToStart: () -> Unit = {},
    onStartToEnd: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // 1. State is hoisted here
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {

            // 2. Animate the swipe by changing the color
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled -> Color.LightGray
                    SwipeToDismissBoxValue.StartToEnd -> Color.Green
                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                },
                label = "swipe"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color) // 3. Set the animated color here
            ) {

                // 4. Show the correct icon
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = "edit"
                        )
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            imageVector = Icons.Default.Delete,
                            contentDescription = "delete"
                        )
                    }

                    SwipeToDismissBoxValue.Settled -> {
                        // Nothing to do
                    }
                }

            }
        }
    ) {
        content()
    }

    LaunchedEffect(dismissState.currentValue) {
        // 5. Trigger the callbacks
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.EndToStart -> {
                onEndToStart()

                // 6. Don't forget to reset the state value
                dismissState.snapTo(SwipeToDismissBoxValue.Settled) // or dismissState.reset()
            }

            SwipeToDismissBoxValue.StartToEnd -> {
                onStartToEnd()
                dismissState.snapTo(SwipeToDismissBoxValue.Settled) // or dismissState.reset()
            }

            SwipeToDismissBoxValue.Settled -> {
                // Nothing to do
            }
        }
    }
}