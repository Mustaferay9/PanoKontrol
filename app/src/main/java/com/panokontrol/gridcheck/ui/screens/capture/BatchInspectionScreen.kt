package com.panokontrol.gridcheck.ui.screens.capture

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panokontrol.gridcheck.R
import com.panokontrol.gridcheck.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchInspectionScreen(
    onBack: () -> Unit,
    onAnalyze: () -> Unit
) {
    var coverPhotoAdded by remember { mutableStateOf(false) }
    var labelPhotoAdded by remember { mutableStateOf(false) }
    var innerPhotoAdded by remember { mutableStateOf(false) }

    var showUploadDialog by remember { mutableStateOf(false) }
    var currentSlotToFill by remember { mutableStateOf(-1) } // 0: Cover, 1: Label, 2: Inner

    val totalAdded = listOf(coverPhotoAdded, labelPhotoAdded, innerPhotoAdded).count { it }
    val allPhotosAdded = totalAdded == 3

    if (showUploadDialog) {
        ModalBottomSheet(
            onDismissRequest = { showUploadDialog = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fotoğraf Yükle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Görseli nasıl eklemek istersiniz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            when (currentSlotToFill) {
                                0 -> coverPhotoAdded = true
                                1 -> labelPhotoAdded = true
                                2 -> innerPhotoAdded = true
                            }
                            showUploadDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavySoft, contentColor = Navy),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Galeriden Seç", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            when (currentSlotToFill) {
                                0 -> coverPhotoAdded = true
                                1 -> labelPhotoAdded = true
                                2 -> innerPhotoAdded = true
                            }
                            showUploadDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavySoft, contentColor = Navy),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Kameradan Çek", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Toplu Pano Denetimi",
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            modifier = Modifier.padding(end = 48.dp) // Offset for back button to perfectly center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Navy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Paper
                )
            )
        },
        containerColor = Paper
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Denetim için gereken 3 fotoğrafı da yükleyin, ardından\ntümünü tek seferde analiz edin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Photo Slots Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PhotoSlot(
                    title = "1. Dış Kapak",
                    isAdded = coverPhotoAdded,
                    imageRes = R.drawable.pano_door_raw,
                    onClick = { currentSlotToFill = 0; showUploadDialog = true }
                )
                PhotoSlot(
                    title = "2. Ürün Etiketi",
                    isAdded = labelPhotoAdded,
                    imageRes = R.drawable.etiket_400a_yakin,
                    onClick = { currentSlotToFill = 1; showUploadDialog = true }
                )
                PhotoSlot(
                    title = "3. İç Kısım",
                    isAdded = innerPhotoAdded,
                    imageRes = R.drawable.pano_interior_raw,
                    onClick = { currentSlotToFill = 2; showUploadDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "$totalAdded / 3 Fotoğraf Seçildi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAnalyze,
                enabled = allPhotosAdded,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow,
                    contentColor = NavyDark,
                    disabledContainerColor = Color(0xFFE2E8F0),
                    disabledContentColor = Color(0xFF94A3B8)
                )
            ) {
                Text(
                    "Tümünü Analiz Et",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PhotoSlot(
    title: String,
    isAdded: Boolean,
    imageRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(280.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isAdded) PassBg.copy(alpha = 0.3f) else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isAdded) BorderStroke(1.dp, Pass.copy(alpha = 0.5f)) else BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAdded) 2.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAdded) Color.Black else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (isAdded) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Dokun ve Yükle",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isAdded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Pass,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Seçildi",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Pass
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp)) // Maintain height when empty
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BatchInspectionScreenPreview() {
    PanoKontrolTheme { BatchInspectionScreen(onBack = {}, onAnalyze = {}) }
}
