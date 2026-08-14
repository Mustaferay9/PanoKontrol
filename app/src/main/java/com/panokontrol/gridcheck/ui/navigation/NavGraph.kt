package com.panokontrol.gridcheck.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.panokontrol.gridcheck.ui.screens.capture.BatchInspectionScreen
import com.panokontrol.gridcheck.ui.screens.dashboard.DashboardScreen
import com.panokontrol.gridcheck.ui.screens.login.LoginScreen
import com.panokontrol.gridcheck.ui.screens.processing.ProcessingScreen
import com.panokontrol.gridcheck.ui.screens.result.ResultScreen
import com.panokontrol.gridcheck.ui.screens.test.ApiTestScreen
import com.panokontrol.gridcheck.ui.screens.inspection.InspectionFlowScreen

@Composable
fun PanoKontrolNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartInspection = { navController.navigate(Screen.Capture.route) },
                onOpenPastInspection = { inspectionId ->
                    val targetPanoId = if (inspectionId.contains("0147") || inspectionId.contains("0145")) "pass_demo" else "fail_demo"
                    navController.navigate(Screen.Result.createRoute(targetPanoId))
                }
            )
        }
        composable(Screen.Capture.route) {
            BatchInspectionScreen(
                onBack = { navController.popBackStack() },
                onAnalyze = { 
                    navController.navigate(Screen.Processing.createRoute("demo"))
                },
            )
        }
        composable(
            route = Screen.Processing.route,
            arguments = listOf(navArgument("panoId") { type = NavType.StringType; defaultValue = "interior" })
        ) { backStackEntry ->
            val panoId = backStackEntry.arguments?.getString("panoId") ?: "interior"
            ProcessingScreen(
                panoId = panoId,
                onFinished = {
                    runCatching {
                        navController.navigate(Screen.Result.createRoute(panoId)) {
                            popUpTo(Screen.Dashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onRetry = { navController.popBackStack() },
            )
        }
        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("panoId") { type = NavType.StringType; defaultValue = "interior" })
        ) { backStackEntry ->
            val panoId = backStackEntry.arguments?.getString("panoId") ?: "interior"
            ResultScreen(
                panoId = panoId,
                onBackToDashboard = {
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                },
            )
        }
        composable(Screen.ApiTest.route) {
            ApiTestScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.InspectionFlow.route) {
            InspectionFlowScreen()
        }
    }
}
