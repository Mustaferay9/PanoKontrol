package com.panokontrol.gridcheck.ui.navigation

/** Uygulamanın 4 ana ekranı — yol haritası bölüm 3. */
sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Capture : Screen("capture")
    data object Processing : Screen("processing")
    data object Result : Screen("result")
}
