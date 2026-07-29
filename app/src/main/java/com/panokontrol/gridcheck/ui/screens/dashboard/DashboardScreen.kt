package com.panokontrol.gridcheck.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panokontrol.gridcheck.ui.theme.Fail
import com.panokontrol.gridcheck.ui.theme.FailBg
import com.panokontrol.gridcheck.ui.theme.InspectionStatus
import com.panokontrol.gridcheck.ui.theme.Navy
import com.panokontrol.gridcheck.ui.theme.NavyDark
import com.panokontrol.gridcheck.ui.theme.NavySoft
import com.panokontrol.gridcheck.ui.theme.PanoKontrolTheme
import com.panokontrol.gridcheck.ui.theme.Pass
import com.panokontrol.gridcheck.ui.theme.PassBg
import com.panokontrol.gridcheck.ui.theme.Warn
import com.panokontrol.gridcheck.ui.theme.WarnBg
import com.panokontrol.gridcheck.ui.theme.Yellow

private enum class CabinetCategory(val label: String) {
    DSYA_BUYUK("DSYA Büyük"),
    DSYA_KUCUK("DSYA Küçük"),
    OTOMATIK("Otomatik Tespit (AI)"),
}

/** Faz 1 yer tutucu veri — Faz 5'te Room'dan (InspectionDao) gelecek. */
private data class RecentInspection(
    val id: String,
    val type: String,
    val date: String,
    val status: InspectionStatus,
)

private val sampleRecentInspections = listOf(
    RecentInspection("SDK-2026-0148", "DSYA Büyük", "29 Tem 2026 · 09:14", InspectionStatus.PARTIAL),
    RecentInspection("SDK-2026-0147", "DSYA Küçük", "28 Tem 2026 · 16:02", InspectionStatus.PASS),
    RecentInspection("SDK-2026-0146", "DSYA Büyük", "28 Tem 2026 · 11:47", InspectionStatus.FAIL),
    RecentInspection("SDK-2026-0145", "DSYA Küçük", "27 Tem 2026 · 14:30", InspectionStatus.PASS),
)

@Composable
fun DashboardScreen(
    onStartInspection: () -> Unit,
    onOpenPastInspection: (String) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            DashboardHeader(technicianName = "Mustafa Eray Erdoğan", isOnline = true)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { StartInspectionCta(onClick = onStartInspection) }
                item { CategorySelector() }
                item {
                    Text(
                        "SON DENETİMLER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(sampleRecentInspections) { inspection ->
                    RecentInspectionCard(
                        inspection = inspection,
                        onClick = { onOpenPastInspection(inspection.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(technicianName: String, isOnline: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(colors = listOf(Navy, NavyDark)),
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "PanoKontrol",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            Text(
                " AI",
                style = MaterialTheme.typography.headlineLarge,
                color = Yellow,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, Yellow),
            ) {
                Text(
                    "GRIDCHECK AI",
                    style = MaterialTheme.typography.labelSmall,
                    color = Yellow,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Yellow),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    technicianName.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = NavyDark,
                )
            }
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(technicianName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    "BAŞKENT EDAŞ · GİRİŞ KALİTE KONTROL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9FB0D4),
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) Color(0xFF4FDA96) else Fail),
                )
                Text(
                    if (isOnline) " ÇEVRİMİÇİ" else " ÇEVRİMDIŞI",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOnline) Color(0xFF4FDA96) else Fail,
                )
            }
        }
    }
}

@Composable
private fun StartInspectionCta(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = NavyDark),
    ) {
        Text(
            "Yeni SDK Denetimi Başlat",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun CategorySelector() {
    var selected by remember { mutableStateOf(CabinetCategory.OTOMATIK) }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CabinetCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { selected = category },
                label = { Text(category.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavySoft,
                    selectedLabelColor = Navy,
                ),
            )
        }
    }
}

@Composable
private fun RecentInspectionCard(inspection: RecentInspection, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    inspection.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(inspection.type, style = MaterialTheme.typography.titleMedium)
                Text(
                    inspection.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusBadge(status = inspection.status)
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusBadge(status: InspectionStatus) {
    val (label, fg, bg) = when (status) {
        InspectionStatus.PASS -> Triple("UYGUN", Pass, PassBg)
        InspectionStatus.PARTIAL -> Triple("KISMİ", Warn, WarnBg)
        InspectionStatus.FAIL -> Triple("RED", Fail, FailBg)
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = Modifier.padding(end = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun DashboardScreenPreview() {
    PanoKontrolTheme { DashboardScreen(onStartInspection = {}, onOpenPastInspection = {}) }
}
