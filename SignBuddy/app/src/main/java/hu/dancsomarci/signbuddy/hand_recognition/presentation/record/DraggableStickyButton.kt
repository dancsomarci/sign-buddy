package hu.dancsomarci.signbuddy.hand_recognition.presentation.record

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun DraggableStickyFloatingActionButton(
    onClick: ()->Unit,
    modifier: Modifier = Modifier,
    color: Color = FloatingActionButtonDefaults.containerColor,
    content: @Composable ()->Unit
) {
    val d = LocalDensity.current
    var offsetX by remember{ mutableFloatStateOf(0.0f) }
    var offsetY by remember{ mutableFloatStateOf(0.0f) }

    FloatingActionButton(
        containerColor = color,
        onClick = onClick,
        modifier = modifier
            .offset((offsetX/d.density).dp, (offsetY/d.density).dp)
            .pointerInput(Unit){
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        content()
    }
}