package com.panokontrol.gridcheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Saha uygulaması tek, sabit (koyu olmayan) marka temasıyla çalışır — sistem karanlık modundan bağımsız.
private val PanoKontrolColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = Card,
    primaryContainer = NavySoft,
    onPrimaryContainer = Navy,
    secondary = Yellow,
    onSecondary = NavyDark,
    background = Paper,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    surfaceVariant = NavySoft,
    onSurfaceVariant = Muted,
    outline = Line,
    error = Fail,
    onError = Card,
    errorContainer = FailBg,
    onErrorContainer = Fail,
)

@Composable
fun PanoKontrolTheme(content: @Composable () -> Unit) {
    val colorScheme = PanoKontrolColorScheme
    // Header her zaman Navy zeminli, dolayısıyla durum çubuğu ikonları hep açık renk.
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as? android.app.Activity
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PanoKontrolTypography,
        content = content,
    )
}

/** Checklist / rozet durumları — Sonuç ekranında ve Dashboard listesinde ortak kullanılır. */
enum class InspectionStatus { PASS, PARTIAL, FAIL }
