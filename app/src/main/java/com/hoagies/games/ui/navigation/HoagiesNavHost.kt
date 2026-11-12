package com.hoagies.games.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hoagies.games.games.sudoku.ui.SudokuLobbyScreen
import com.hoagies.games.games.sudoku.ui.SudokuPlayScreen
import com.hoagies.games.ui.home.HomeScreen
import com.hoagies.games.ui.settings.SettingsScreen

@Composable
fun HoagiesNavHost(navController: NavHostController = rememberNavController()) {
    // Ignore double-taps on "Back" that would otherwise pop past the home screen.
    val goBack: () -> Unit = {
        if (navController.previousBackStackEntry != null) navController.popBackStack()
    }

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                onOpenGame = { route -> navController.navigate(route) { launchSingleTop = true } },
                onOpenSettings = { navController.navigate(SettingsRoute) { launchSingleTop = true } },
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(onBack = goBack)
        }
        composable<SudokuLobbyRoute> {
            SudokuLobbyScreen(
                onBack = goBack,
                onPlay = { difficulty ->
                    navController.navigate(SudokuPlayRoute(difficulty?.name)) { launchSingleTop = true }
                },
            )
        }
        composable<SudokuPlayRoute> {
            SudokuPlayScreen(onBack = goBack)
        }
    }
}
