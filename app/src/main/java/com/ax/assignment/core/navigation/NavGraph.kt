package com.ax.assignment.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.ax.assignment.core.component.BottomNavBar
import com.ax.assignment.core.theme.Surface
import com.ax.assignment.feature.category.CategoryManageScreen
import com.ax.assignment.feature.home.HomeScreen
import com.ax.assignment.feature.settings.HelpScreen
import com.ax.assignment.feature.settings.SettingsScreen
import com.ax.assignment.feature.settings.StartDaySettingScreen
import com.ax.assignment.feature.statistics.SixMonthHistoryScreen
import com.ax.assignment.feature.statistics.StatisticsScreen
import com.ax.assignment.feature.splash.SplashScreen
import com.ax.assignment.feature.transaction.TransactionAddScreen
import com.ax.assignment.feature.transaction.TransactionDetailScreen

private val bottomBarRoutes = setOf(Screen.Home.route, Screen.Statistics.route, Screen.Settings.route)

// Bottom-tab switches cross-fade (a lateral slide implies hierarchy the tabs don't have)
private fun isTabSwitch(from: String?, to: String?) =
    from in bottomBarRoutes && to in bottomBarRoutes

// Material 3 motion tokens — emphasized easing for screen-level transitions
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
private const val ENTER_MS = 400
private const val EXIT_MS = 350
private const val MODAL_ENTER_MS = 450

// M3 fade-through for top-level (tab) destinations: outgoing fades out fast,
// incoming fades in afterwards while scaling up from 92%
private val tabEnter = fadeIn(tween(260, delayMillis = 90)) +
    scaleIn(initialScale = 0.92f, animationSpec = tween(260, delayMillis = 90, easing = EmphasizedDecelerate))
private val tabExit = fadeOut(tween(90))

@Composable
fun NavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        // Screens handle top/bottom insets themselves, but nothing handles the
        // horizontal ones — in landscape the system bars/cutout move to the sides,
        // so consume them once here (zero in portrait, no effect on existing screens)
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        ),
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    navController = navController,
                )
            }
        },
        containerColor = Surface,
        // Each screen handles its own status-bar inset (statusBarsPadding in top bars);
        // default Scaffold insets would double the top gap
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                val from = initialState.destination.route
                val to = targetState.destination.route
                when {
                    // Add screen opens like a modal sheet — slide up from the bottom
                    to == Screen.TransactionAdd.route ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            tween(MODAL_ENTER_MS, easing = EmphasizedDecelerate),
                        ) + fadeIn(tween(250))
                    from == Screen.Splash.route || isTabSwitch(from, to) -> tabEnter
                    else ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(ENTER_MS, easing = EmphasizedDecelerate),
                        ) + fadeIn(tween(ENTER_MS))
                }
            },
            exitTransition = {
                val from = initialState.destination.route
                val to = targetState.destination.route
                when {
                    // Underlying screen stays put while the modal slides over it
                    to == Screen.TransactionAdd.route -> fadeOut(tween(MODAL_ENTER_MS))
                    from == Screen.Splash.route || isTabSwitch(from, to) -> tabExit
                    else ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(ENTER_MS, easing = EmphasizedDecelerate),
                        ) + fadeOut(tween(ENTER_MS))
                }
            },
            popEnterTransition = {
                when {
                    initialState.destination.route == Screen.TransactionAdd.route -> fadeIn(tween(EXIT_MS))
                    isTabSwitch(initialState.destination.route, targetState.destination.route) -> tabEnter
                    else ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(ENTER_MS, easing = EmphasizedDecelerate),
                        ) + fadeIn(tween(ENTER_MS))
                }
            },
            popExitTransition = {
                when {
                    initialState.destination.route == Screen.TransactionAdd.route ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            tween(EXIT_MS, easing = EmphasizedAccelerate),
                        ) + fadeOut(tween(EXIT_MS))
                    isTabSwitch(initialState.destination.route, targetState.destination.route) -> tabExit
                    else ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            tween(ENTER_MS, easing = EmphasizedDecelerate),
                        ) + fadeOut(tween(ENTER_MS))
                }
            },
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(navController = navController)
            }
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(navController = navController)
            }
            composable(
                route = Screen.TransactionAdd.route,
                arguments = listOf(
                    navArgument("periodStart") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("periodEnd") {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
            ) { backStackEntry ->
                TransactionAddScreen(
                    navController = navController,
                    periodStartArg = backStackEntry.arguments?.getString("periodStart"),
                    periodEndArg = backStackEntry.arguments?.getString("periodEnd"),
                )
            }
            composable(
                route = Screen.TransactionEdit.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
                TransactionDetailScreen(navController = navController, transactionId = id)
            }
            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
                TransactionDetailScreen(navController = navController, transactionId = id)
            }
            composable(Screen.SixMonthHistory.route) {
                SixMonthHistoryScreen(navController = navController)
            }
            composable(Screen.CategoryManage.route) {
                CategoryManageScreen(navController = navController)
            }
            composable(
                route = Screen.CategorySelect.route,
                arguments = listOf(navArgument("selectedId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }),
            ) { backStackEntry ->
                val selectedId = backStackEntry.arguments?.getLong("selectedId") ?: -1L
                CategoryManageScreen(navController = navController, isSelectMode = true, initialSelectedId = selectedId)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable(Screen.StartDaySetting.route) {
                StartDaySettingScreen(navController = navController)
            }
            composable(Screen.Help.route) {
                HelpScreen(navController = navController)
            }
        }
    }
}
