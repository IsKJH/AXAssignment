package com.ax.assignment.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
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

@Composable
fun NavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(
                    currentRoute = currentRoute ?: Screen.Home.route,
                    navController = navController,
                )
            }
        },
        containerColor = Surface,
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) + fadeOut(tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) + fadeOut(tween(300))
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
