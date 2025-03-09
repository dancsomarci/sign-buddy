package hu.dancsomarci.signbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import hu.dancsomarci.signbuddy.navigation.NavGraph
import hu.dancsomarci.signbuddy.ui.theme.SignBuddyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SignBuddyTheme {
                NavGraph()
            }
        }
    }
}
