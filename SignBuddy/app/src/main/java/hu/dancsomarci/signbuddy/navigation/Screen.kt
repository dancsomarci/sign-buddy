package hu.dancsomarci.signbuddy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login: Screen("login")
    data object Register: Screen("register")
    data object AccountDetails: Screen("account_details")

    sealed class BottomNavItem(
        route: String,
        val icon: ImageVector,
        val label: String
    ): Screen(route){
        data object Recordings : BottomNavItem(
            "recordings",
            Icons.Default.List,
            "Recordings"
        )
        data object HandRecognition : BottomNavItem(
            "hand_recognition",
            Icons.Default.ThumbUp,
            "Recognition"
        )
    }
}
