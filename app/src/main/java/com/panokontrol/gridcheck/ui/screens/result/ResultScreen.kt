package com.panokontrol.gridcheck.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panokontrol.gridcheck.ui.theme.PanoKontrolTheme

/**
 * Denetim sonucu ekranı (yol haritası 3.4). Faz 1'de iskelet — bbox overlay, skor
 * kartı ve checklist Faz 3'te (mock veriyle) doldurulacak.
 */
@Composable
fun ResultScreen(onBackToDashboard: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            "Denetim Sonucu",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Viewport, skor kartı ve checklist — Faz 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onBackToDashboard) {
            Text("Panele Dön")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    PanoKontrolTheme { ResultScreen(onBackToDashboard = {}) }
}
