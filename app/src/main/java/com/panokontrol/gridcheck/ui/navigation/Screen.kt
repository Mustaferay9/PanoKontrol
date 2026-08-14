package com.panokontrol.gridcheck.ui.navigation

/** Uygulamanın ana ekranları ve dinamik rota tanımları. */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Capture : Screen("capture")
    data object Processing : Screen("processing/{panoId}") {
        fun createRoute(panoId: String) = "processing/$panoId"
    }
    data object Result : Screen("result/{panoId}") {
        fun createRoute(panoId: String) = "result/$panoId"
    }
    data object ApiTest : Screen("api_test")
    data object InspectionFlow : Screen("inspection_flow")
}
