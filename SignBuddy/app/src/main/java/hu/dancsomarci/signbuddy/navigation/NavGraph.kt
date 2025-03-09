package hu.dancsomarci.signbuddy.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.play.integrity.internal.i
import hu.dancsomarci.signbuddy.auth.presentation.account_details.AccountCenterScreen
import hu.dancsomarci.signbuddy.auth.presentation.login.LoginScreen
import hu.dancsomarci.signbuddy.auth.presentation.register.RegisterScreen
import hu.dancsomarci.signbuddy.hand_recognition.presentation.common.TabBarItem
import hu.dancsomarci.signbuddy.hand_recognition.presentation.record.LandmarkRecordingScreen
import hu.dancsomarci.signbuddy.hand_recognition.presentation.list_recordings.RecordingsScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
) {
    // Bottom navigation
    val recordingsTab = TabBarItem(
        route = Screen.BottomNavItem.Recordings.route,
        title = Screen.BottomNavItem.Recordings.label,
        selectedIcon = Screen.BottomNavItem.Recordings.icon,
        unselectedIcon = Screen.BottomNavItem.Recordings.icon
    )
    val recognizeTab = TabBarItem(
        route = Screen.BottomNavItem.HandRecognition.route,
        title = Screen.BottomNavItem.HandRecognition.label,
        selectedIcon = Screen.BottomNavItem.HandRecognition.icon,
        unselectedIcon = Screen.BottomNavItem.HandRecognition.icon
    )
    val tabs = listOf(recordingsTab, recognizeTab)
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    fun onTabSelected(idx: Int){
        selectedTabIndex = idx
        navController.navigate(tabs[idx].route)
    }

    // Animations
    val animationTime by rememberSaveable {
        mutableIntStateOf(400)
    }
    fun enterSlideTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition {
        return {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(animationTime, easing = EaseInOut)
            )
        }
    }
    fun popEnterSlideTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition {
        return {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animationTime, easing = EaseIn)
            )
        }
    }
    fun exitSlideTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition {
        return {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(animationTime, easing = EaseIn)
            )
        }
    }
    fun popExitSlideTransition(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition {
        return {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(animationTime, easing = EaseIn)
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(
            route=Screen.Login.route,
            enterTransition = enterSlideTransition(),
            exitTransition = exitSlideTransition(),
            popEnterTransition = popEnterSlideTransition(),
            popExitTransition = popExitSlideTransition()
        ) {
            LoginScreen(
                onSuccess = {
                    navController.navigate(Screen.BottomNavItem.Recordings.route)
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(
            route=Screen.Register.route,
            enterTransition = enterSlideTransition(),
            exitTransition = exitSlideTransition(),
            popEnterTransition = popEnterSlideTransition(),
            popExitTransition = popExitSlideTransition()
        ) {
            RegisterScreen(
                onNavigateBack = { navToWithPopBackStack(Screen.Login, navController) },
                onSuccess = { navController.navigate(Screen.BottomNavItem.Recordings.route) }
            )
        }
        composable(
            route=Screen.BottomNavItem.Recordings.route,
            enterTransition = enterSlideTransition(),
            exitTransition = exitSlideTransition(),
            popEnterTransition = popEnterSlideTransition(),
            popExitTransition = popExitSlideTransition()
        ) {
            RecordingsScreen(
                tabBarItems = tabs,
                onTabSelected = {idx->onTabSelected(idx)},
                selectedTabIndex = selectedTabIndex,
                onNavigateToAccountCenter = {
                    navController.navigate(Screen.AccountDetails.route)
                }
            )
        }
        composable(
            route = Screen.BottomNavItem.HandRecognition.route,
            enterTransition = enterSlideTransition(),
            exitTransition = exitSlideTransition(),
            popEnterTransition = popEnterSlideTransition(),
            popExitTransition = popExitSlideTransition()
        ) {
            LandmarkRecordingScreen(
                tabBarItems = tabs,
                onTabSelected = {idx->onTabSelected(idx)},
                selectedTabIndex = selectedTabIndex,
            )
        }
        composable(
            route=Screen.AccountDetails.route,
            enterTransition = enterSlideTransition(),
            exitTransition = exitSlideTransition(),
            popEnterTransition = popEnterSlideTransition(),
            popExitTransition = popExitSlideTransition()
        ){
            AccountCenterScreen(
                afterLogout = { navToWithPopBackStack(Screen.Login, navController) },
                onSignIn = { navToWithPopBackStack(Screen.Login, navController) },
                onSignUp = { navToWithPopBackStack(Screen.Register, navController) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

fun <T : Screen> navToWithPopBackStack(screen: T, navController: NavHostController) {
    navController.popBackStack(
        route = screen.route,
        inclusive = true
    )
    navController.navigate(screen.route)
}