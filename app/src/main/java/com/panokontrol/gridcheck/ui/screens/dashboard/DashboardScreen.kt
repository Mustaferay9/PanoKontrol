package com.panokontrol.gridcheck.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.sp
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

// Removed CabinetCategory as it is no longer needed

private data class RecentInspection(
    val id: String,
    val type: String,
    val date: String,
    val status: InspectionStatus,
    val accuracy: String,
)

private val sampleRecentInspections = listOf(
    RecentInspection("KKT-2026-0148", "Siemens A.Ş. - İrsaliye: 3491", "29 Tem 2026 · 09:14", InspectionStatus.PARTIAL, "%96.8"),
    RecentInspection("KKT-2026-0147", "Schneider Electric - İrsaliye: 3482", "28 Tem 2026 · 16:02", InspectionStatus.PASS, "%99.1"),
    RecentInspection("KKT-2026-0146", "ABB - İrsaliye: 3475", "28 Tem 2026 · 11:47", InspectionStatus.FAIL, "%94.5"),
    RecentInspection("KKT-2026-0145", "Siemens A.Ş. - İrsaliye: 3460", "27 Tem 2026 · 14:30", InspectionStatus.PASS, "%98.7"),
)

private enum class TaskPriority(val label: String, val color: Color) {
    HIGH("Yüksek", Fail),
    NORMAL("Normal", Pass)
}

private data class DashboardTask(
    val id: String,
    val location: String,
    val type: String,
    val priority: TaskPriority
)

private val sampleTasks = listOf(
    DashboardTask("İŞ EMRİ: 1042", "Kabul Alanı - A1 Bölgesi", "Siemens A.Ş. - İrsaliye: 3501", TaskPriority.HIGH),
    DashboardTask("İŞ EMRİ: 1043", "Kabul Alanı - B2 Bölgesi", "Schneider Electric - İrsaliye: 3502", TaskPriority.NORMAL),
    DashboardTask("İŞ EMRİ: 1044", "Kabul Alanı - C1 Bölgesi", "ABB - İrsaliye: 3503", TaskPriority.NORMAL),
)

@Composable
fun DashboardScreen(
    onStartInspection: () -> Unit,
    onOpenPastInspection: (String) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            DashboardHeader(userName = "Mustafa Eray Erdoğan")

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    StartInspectionCta(
                        title = "Kalite Kontrol Testi Başlat",
                        onClick = onStartInspection
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Tamamlanan", "12", Icons.Default.CheckCircle, Pass, PassBg)
                        StatCard("Uygunsuz", "2", Icons.Default.Warning, Fail, FailBg)
                        StatCard("Bekleyen", "3", Icons.Default.PendingActions, Warn, WarnBg)
                    }
                }
                item { RecentQuickAccess() }
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent,
                        contentColor = Navy,
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                    color = Navy
                                )
                            }
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTabIndex == 0,
                            onClick = { selectedTabIndex = 0 },
                            text = { Text("Bekleyen Kabuller", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTabIndex == 1,
                            onClick = { selectedTabIndex = 1 },
                            text = { Text("Geçmiş", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                if (selectedTabIndex == 0) {
                    items(sampleTasks) { task ->
                        TaskCard(task = task, onClick = { onStartInspection() })
                    }
                } else {
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
}

@Composable
private fun DashboardHeader(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(colors = listOf(Navy, NavyDark))
            )
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand: Sparky
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Yellow,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sparky",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }

            // Minimal User Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x22FFFFFF))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Yellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ME",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        userName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        "Başkent EDAŞ",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StartInspectionCta(
    title: String = "⚡ Canlı Pano Denetimi Başlat",
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = NavyDark),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NavyDark)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RecentQuickAccess() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Son Eklenen 3 Pano",
            style = MaterialTheme.typography.labelMedium,
            color = Navy.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("PNO-8921", "PNO-8920", "PNO-8919").forEach { panoId ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NavySoft,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Pass, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(panoId, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Navy)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentInspectionCard(inspection: RecentInspection, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        inspection.id,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NavySoft
                    ) {
                        Text(
                            inspection.accuracy,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(inspection.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Navy)
                Text(
                    inspection.date,
                    style = MaterialTheme.typography.bodySmall,
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Card(
        modifier = Modifier.width(120.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(26.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Navy)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Navy.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun TaskCard(task: DashboardTask, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(4.dp), color = NavySoft) {
                    Text(task.id, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Navy, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text(
                    task.priority.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = task.priority.color,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(task.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Navy)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(task.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun DashboardScreenPreview() {
    PanoKontrolTheme { DashboardScreen(onStartInspection = {}, onOpenPastInspection = {}) }
}
