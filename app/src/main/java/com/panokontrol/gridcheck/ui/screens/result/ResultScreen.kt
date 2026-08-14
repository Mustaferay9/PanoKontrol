package com.panokontrol.gridcheck.ui.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.panokontrol.gridcheck.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    panoId: String = "1", // we can use panoId or passed param to toggle fail/pass
    onBackToDashboard: () -> Unit
) {
    // For demo purposes, we randomly assign pass/fail or use panoId to determine it
    // In actual implementation, this state comes from the backend AI
    val isPassed = panoId != "fail_demo"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Denetim Özeti",
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            modifier = Modifier.padding(end = 48.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onBackToDashboard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = NavyDark)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = NavyDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Yeni Denetim Başlat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Paper
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Status Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isPassed) Color(0xFF2ECA73) else Color(0xFFF34348)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isPassed) Color(0xFF2ECA73) else Color(0xFFF34348),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isPassed) "ONAYLANDI" else "KALDI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            // Dış Kapak Analizi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Dış Kapak Analizi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (isPassed) {
                        CheckListItem("Montaj Talimatı", "Mevcut", true)
                        CheckListItem("Tehlike Levhası", "Mevcut", true)
                        CheckListItem("Ürün CSB No", "Mevcut", true)
                        CheckListItem("Ürün Etiketi", "Mevcut", true)
                    } else {
                        CheckListItem("Montaj Talimatı", "Eksik", false)
                        CheckListItem("Tehlike Levhası", "Mevcut", true)
                        CheckListItem("Ürün CSB No", "Mevcut", true)
                        CheckListItem("Ürün Etiketi", "Eksik", false)
                    }
                }
            }

            // Ürün Etiketi Bilgileri
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ürün Etiketi Bilgileri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (isPassed) {
                        CheckListItem("Marka", "ÇAĞDAŞ", true)
                        CheckListItem("Alıcı Firma", "AYEDAŞ", true)
                        CheckListItem("Garanti Başlangıç", "23/07/26", true)
                        CheckListItem("Garanti Bitiş", "23/07/29", true)
                    } else {
                        CheckListItem("Marka", "TESPİT EDİLEMEDİ", false)
                        CheckListItem("Alıcı Firma", "TESPİT EDİLEMEDİ", false)
                        CheckListItem("Garanti Başlangıç", "TESPİT EDİLEMEDİ", false)
                        CheckListItem("Garanti Bitiş", "TESPİT EDİLEMEDİ", false)
                    }
                }
            }

            // SDK İç Kısım Analizi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SDK İç Kısım Analizi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (isPassed) {
                        CheckListItem("400A Sigorta", "1 Adet", true)
                        CheckListItem("250A Sigorta", "2 Adet", true)
                        CheckListItem("160A Sigorta", "5 Adet", true)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2ECA73)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("SAP Kodu", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                Text("10008670", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    } else {
                        CheckListItem("400A Sigorta", "1 Adet", true)
                        CheckListItem("250A Sigorta", "0 Adet", false)
                        CheckListItem("160A Sigorta", "10 Adet", true)

                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2ECA73) // SAP is matched green even when failing
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("SAP Kodu", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                Text("10008669", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CheckListItem(label: String, value: String, isSuccess: Boolean) {
    val iconColor = if (isSuccess) Color(0xFF2ECA73) else Color(0xFFF34348)
    val icon = if (isSuccess) Icons.Default.Check else Icons.Default.Close
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Navy
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    PanoKontrolTheme { ResultScreen(panoId = "1", onBackToDashboard = {}) }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenFailPreview() {
    PanoKontrolTheme { ResultScreen(panoId = "fail_demo", onBackToDashboard = {}) }
}
