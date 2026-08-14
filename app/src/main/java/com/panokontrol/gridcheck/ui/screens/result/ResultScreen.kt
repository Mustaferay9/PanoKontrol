package com.panokontrol.gridcheck.ui.screens.result

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panokontrol.gridcheck.R
import com.panokontrol.gridcheck.ui.theme.Navy
import com.panokontrol.gridcheck.ui.theme.NavySoft
import com.panokontrol.gridcheck.ui.theme.PanoKontrolTheme
import com.panokontrol.gridcheck.ui.theme.Pass
import com.panokontrol.gridcheck.ui.theme.PassBg
import com.panokontrol.gridcheck.ui.theme.Yellow
import kotlinx.coroutines.launch

@Composable
fun ResultScreen(
    panoId: String = "interior",
    onBackToDashboard: () -> Unit
) {
    var showRawImage by remember { mutableStateOf(false) }
    var show400aFullModule by remember { mutableStateOf(false) }
    var show160aFullModule by remember { mutableStateOf(false) }

    val rawImageRes = if (panoId == "interior") R.drawable.pano_interior_raw else R.drawable.pano_door_raw
    val processedImageRes = if (panoId == "interior") R.drawable.pano_interior_processed else R.drawable.pano_door_processed
    val displayedImageRes = if (showRawImage) rawImageRes else processedImageRes

    val overallScore = if (panoId == "interior") "%98.4" else "%99.1"

    val pageCount = if (panoId == "interior") 3 else 1
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White)
                    ) {
                        Text(
                            "Kontrol Paneline Dön",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Compact Header Badge with SAP Match
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PassBg),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Pass.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Pass),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (panoId == "interior") "SAP Kodu: 1086 · Uygun" else "Denetim Uygun",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Pass
                        )
                        Text(
                            if (panoId == "interior") "1x 400A + 9x 160A DSYA Eşleşti" else "DI-F-142 Şartname standardına uygundur",
                            style = MaterialTheme.typography.bodySmall,
                            color = Pass.copy(alpha = 0.85f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Yellow
                    ) {
                        Text(
                            overallScore,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Swipeable Image Card with Gallery Pager & Switcher
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Module Tabs / Indicators for Interior Pano
                    if (panoId == "interior") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NavySoft)
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "Genel Pano",
                                "400A DSYA",
                                "160A DSYA"
                            ).forEachIndexed { index, title ->
                                val isSelected = pagerState.currentPage == index
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                ) {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 7.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Sub-switchers depending on active page
                    when (pagerState.currentPage) {
                        0 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySoft.copy(alpha = 0.6f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!showRawImage) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showRawImage = false }
                                ) {
                                    Text(
                                        "İşlenmiş (AI)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (!showRawImage) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!showRawImage) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (showRawImage) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { showRawImage = true }
                                ) {
                                    Text(
                                        "Orijinal",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (showRawImage) FontWeight.Bold else FontWeight.Medium,
                                        color = if (showRawImage) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        1 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySoft.copy(alpha = 0.6f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!show400aFullModule) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { show400aFullModule = false }
                                ) {
                                    Text(
                                        "🔍 Etiket (Büyük / Okunabilir)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (!show400aFullModule) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!show400aFullModule) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (show400aFullModule) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { show400aFullModule = true }
                                ) {
                                    Text(
                                        "Tüm Modül",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (show400aFullModule) FontWeight.Bold else FontWeight.Medium,
                                        color = if (show400aFullModule) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        2 -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySoft.copy(alpha = 0.6f))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (!show160aFullModule) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { show160aFullModule = false }
                                ) {
                                    Text(
                                        "🔍 Etiket (Büyük / Okunabilir)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (!show160aFullModule) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!show160aFullModule) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (show160aFullModule) Navy else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { show160aFullModule = true }
                                ) {
                                    Text(
                                        "Tüm Modül",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (show160aFullModule) FontWeight.Bold else FontWeight.Medium,
                                        color = if (show160aFullModule) Color.White else Navy,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Pager for Swipeable Gallery
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when (page) {
                                0 -> {
                                    Image(
                                        painter = painterResource(id = displayedImageRes),
                                        contentDescription = "Pano Çıktısı",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                1 -> {
                                    val res400 = if (show400aFullModule) R.drawable.modul_400a_buyuk else R.drawable.etiket_400a_yakin
                                    Image(
                                        painter = painterResource(id = res400),
                                        contentDescription = "400A'lik DSYA (Federal FVS 400)",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                2 -> {
                                    val res160 = if (show160aFullModule) R.drawable.modul_160a_kucuk else R.drawable.etiket_160a_yakin
                                    Image(
                                        painter = painterResource(id = res160),
                                        contentDescription = "160A'lik DSYA (Federal FVS 160)",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dots & Page Legend
                    if (panoId == "interior") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(pageCount) { index ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Navy else Color(0xFFCBD5E1))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (pagerState.currentPage) {
                            0 -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LegendTag(color = Color(0xFF00E5FF), text = "1x 400A DSYA [0.90]", modifier = Modifier.weight(1f))
                                    LegendTag(color = Color(0xFFFF9100), text = "9x 160A DSYA [0.88-0.94]", modifier = Modifier.weight(1f))
                                }
                            }
                            1 -> {
                                LegendTag(
                                    color = Color(0xFF00E5FF),
                                    text = "Federal FVS 400 · 400A'lik DSYA (1 Adet · TEDAŞ Uygun)",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            2 -> {
                                LegendTag(
                                    color = Color(0xFFFF9100),
                                    text = "Federal FVS 160 · 160A'lik DSYA (9 Adet · TEDAŞ Uygun)",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LegendTag(color = Color(0xFF00E5FF), text = "urun_etiketi [0.98]", modifier = Modifier.weight(1f))
                            LegendTag(color = Color(0xFFFFD600), text = "tehlike_levhasi [0.95]", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Teknik Etiket Bilgileri (Büyük ve Net Okunabilir Tablo)
            if (panoId == "interior" && pagerState.currentPage != 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (pagerState.currentPage == 1) "🏷️ 400A'LİK DSYA ETİKET BİLGİLERİ" else "🏷️ 160A'LİK DSYA ETİKET BİLGİLERİ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (pagerState.currentPage == 1) {
                            SpecRow("Üretici / Model", "Federal Electric · Type: FVS 400")
                            SpecRow("Anma Akımı (In)", "400 A")
                            SpecRow("Yalıtım Gerilimi (Ui)", "690 V")
                            SpecRow("Darbe Dayanım (Uimp)", "12 kV")
                            SpecRow("Frekans / Koruma", "50/60 Hz · IP20")
                            SpecRow("Kablo Kesiti", "95-240 mm²")
                            SpecRow("TEDAŞ Uyum Durumu", "✅ Tam Uyumlu (400A DSYA)")
                        } else {
                            SpecRow("Üretici / Model", "Federal Electric · Type: FVS 160")
                            SpecRow("Anma Akımı (In)", "160 A")
                            SpecRow("Yalıtım Gerilimi (Ui)", "690 V")
                            SpecRow("Darbe Dayanım (Uimp)", "12 kV")
                            SpecRow("Frekans / Koruma", "50/60 Hz · IP20")
                            SpecRow("Kablo Kesiti", "35-95 mm²")
                            SpecRow("TEDAŞ Uyum Durumu", "✅ Tam Uyumlu (160A DSYA)")
                        }
                    }
                }
            }

            // Doğruluk Oranları
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "DOĞRULUK ORANLARI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (panoId == "interior") {
                        ConfidenceRow(label = "1x 400A'lik DSYA", value = "%90.0", progress = 0.900f, color = Color(0xFF00B4D8))
                        Spacer(modifier = Modifier.height(10.dp))
                        ConfidenceRow(label = "9x 160A'lik DSYA", value = "%91.5", progress = 0.915f, color = Color(0xFFF77F00))
                    } else {
                        ConfidenceRow(label = "urun_etiketi", value = "%98.0", progress = 0.980f, color = Color(0xFF00B4D8))
                        Spacer(modifier = Modifier.height(10.dp))
                        ConfidenceRow(label = "tehlike_levhasi", value = "%95.0", progress = 0.950f, color = Color(0xFFE5A100))
                    }
                }
            }

            // Simple Checklist
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "KONTROL LİSTESİ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (panoId == "interior") {
                        SimpleCheckItem("SAP Kodu Eşleşmesi (1086)")
                        SimpleCheckItem("1x 400A'lik DSYA (FVS 400)")
                        SimpleCheckItem("9x 160A'lik DSYA (FVS 160)")
                    } else {
                        SimpleCheckItem("Ürün ve CE Bilgi Etiketi")
                        SimpleCheckItem("Ölüm Tehlikesi Uyarı Levhası")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Navy
        )
    }
}

@Composable
private fun LegendTag(color: Color, text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = NavySoft,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Navy
            )
        }
    }
}

@Composable
private fun ConfidenceRow(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Navy)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = NavySoft
        )
    }
}

@Composable
private fun SimpleCheckItem(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(PassBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Pass, modifier = Modifier.size(13.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Navy,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    PanoKontrolTheme { ResultScreen(panoId = "interior", onBackToDashboard = {}) }
}
