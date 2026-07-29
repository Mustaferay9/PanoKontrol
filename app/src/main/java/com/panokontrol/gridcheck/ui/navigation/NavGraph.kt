package com.panokontrol.gridcheck.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.panokontrol.gridcheck.ui.screens.capture.CaptureScreen
import com.panokontrol.gridcheck.ui.screens.dashboard.DashboardScreen
import com.panokontrol.gridcheck.ui.screens.processing.ProcessingScreen
import com.panokontrol.gridcheck.ui.screens.result.ResultScreen

@Composable
fun PanoKontrolNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartInspection = { navController.navigate(Screen.Capture.route) },
                onOpenPastInspection = { navController.navigate(Screen.Result.route) },
            )
        }
        composable(Screen.Capture.route) {
            CaptureScreen(
                onBack = { navController.popBackStack() },
                onAnalyze = { navController.navigate(Screen.Processing.route) },
            )
        }
        composable(Screen.Processing.route) {
            ProcessingScreen(
                onFinished = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onRetry = { navController.popBackStack() },
            )
        }
        composable(Screen.Result.route) {
            ResultScreen(
                onBackToDashboard = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                },
            )
        }
    }
}
