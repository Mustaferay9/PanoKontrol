package com.panokontrol.gridcheck.ui.screens.processing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panokontrol.gridcheck.ui.theme.NavyDark
import com.panokontrol.gridcheck.ui.theme.PanoKontrolTheme
import com.panokontrol.gridcheck.ui.theme.Yellow

/**
 * İşleme / sunucu senkron ekranı (yol haritası 3.3). Faz 1'de iskelet — 3 adımlı
 * ilerleme animasyonu ve gerçek/mock API bağlanması Faz 3-4'te eklenecek.
 */
@Composable
fun ProcessingScreen(onFinished: () -> Unit, onRetry: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = NavyDark) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            CircularProgressIndicator(
                color = Yellow,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                "Sunucuya gönderiliyor · YOLO çıkarımı · DI-F-142 kural motoru",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
            Button(onClick = onFinished) {
                Text("(Faz 1 önizleme) Sonuca geç")
            }
            TextButton(onClick = onRetry) {
                Text("Geri", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProcessingScreenPreview() {
    PanoKontrolTheme { ProcessingScreen(onFinished = {}, onRetry = {}) }
}
