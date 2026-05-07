package com.financetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.financetracker.ui.screen.add.AddTransactionScreen
import com.financetracker.ui.screen.detail.TransactionDetailScreen
import com.financetracker.ui.screen.home.HomeScreen
import com.financetracker.ui.screen.search.SearchScreen
import com.financetracker.ui.screen.settings.SettingsScreen
import com.financetracker.ui.screen.statistics.StatisticsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = modifier) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = { navController.navigate(Screen.addTransactionRoute()) },
                onTransactionClick = { id -> navController.navigate(Screen.detailRoute(id)) },
            )
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onTransactionClick = { id -> navController.navigate(Screen.detailRoute(id)) },
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(navArgument("editId") { type = NavType.LongType; defaultValue = -1L }),
        ) { backStackEntry ->
            val editId = backStackEntry.arguments?.getLong("editId") ?: -1L
            AddTransactionScreen(
                editTransactionId = if (editId > 0) editId else null,
                onNavigateBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: return@composable
            TransactionDetailScreen(
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.addTransactionRoute(id)) },
            )
        }
    }
}
