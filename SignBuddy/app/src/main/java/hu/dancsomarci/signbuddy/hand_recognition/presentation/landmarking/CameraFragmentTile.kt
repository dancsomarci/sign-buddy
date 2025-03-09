package hu.dancsomarci.signbuddy.hand_recognition.presentation.landmarking

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import hu.dancsomarci.signbuddy.databinding.FragmentCameraContainerBinding

@Composable
fun CameraFragmentTile() {
   AndroidViewBinding(FragmentCameraContainerBinding::inflate) {
      //cameraFragmentContainerView.getFragment<CameraFragment>()
   }
}