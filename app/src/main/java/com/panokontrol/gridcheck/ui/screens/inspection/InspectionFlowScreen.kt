package com.panokontrol.gridcheck.ui.screens.inspection

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.panokontrol.gridcheck.PredictResponse
import com.panokontrol.gridcheck.SapMapping
import com.panokontrol.gridcheck.detectFromImage
import com.panokontrol.gridcheck.findSapCodes
import com.panokontrol.gridcheck.ui.screens.test.uriToFile
import com.panokontrol.gridcheck.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.coroutines.async

enum class InspectionStep(val title: String) {
    SELECTION("Toplu Pano Denetimi"),
    SUMMARY("Denetim Özeti")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionFlowScreen() {
    var currentStep by remember { mutableStateOf(InspectionStep.SELECTION) }
    
    // State to keep results
    var step1Result by remember { mutableStateOf<Boolean?>(null) } // true if danger sign found
    var step2Result by remember { mutableStateOf<Boolean?>(null) } // true if label found
    var step3Result by remember { mutableStateOf<SapMapping?>(null) } // SAP mapping if any

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentStep.title, fontWeight = FontWeight.Bold, color = Navy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Crossfade(targetState = currentStep, label = "step_crossfade") { step ->
                when (step) {
                    InspectionStep.SELECTION -> {
                        BatchSelectionScreen(
                            onAnalyzeComplete = { res1, res2, res3 ->
                                step1Result = res1
                                step2Result = res2
                                step3Result = res3
                                currentStep = InspectionStep.SUMMARY
                            }
                        )
                    }
                    InspectionStep.SUMMARY -> {
                        SummaryScreen(
                            step1Result = step1Result,
                            step2Result = step2Result,
                            step3Result = step3Result,
                            onRestart = {
                                step1Result = null
                                step2Result = null
                                step3Result = null
                                currentStep = InspectionStep.SELECTION
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchSelectionScreen(
    onAnalyzeComplete: (Boolean, Boolean, SapMapping?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var uri1 by remember { mutableStateOf<Uri?>(null) }
    var file1 by remember { mutableStateOf<File?>(null) }
    
    var uri2 by remember { mutableStateOf<Uri?>(null) }
    var file2 by remember { mutableStateOf<File?>(null) }
    
    var uri3 by remember { mutableStateOf<Uri?>(null) }
    var file3 by remember { mutableStateOf<File?>(null) }
    
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val picker1 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> 
        uri?.let { uri1 = it; file1 = uriToFile(context, it) }
    }
    val picker2 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> 
        uri?.let { uri2 = it; file2 = uriToFile(context, it) }
    }
    val picker3 = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> 
        uri?.let { uri3 = it; file3 = uriToFile(context, it) }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        
        Text(
            "Lütfen denetim sürecini tamamlamak için gerekli 3 fotoğrafı sisteme yükleyin, ardından toplu analiz işlemini başlatın.", 
            color = Color.DarkGray, 
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        val listState = rememberLazyListState()
        
        LaunchedEffect(uri1, uri2, uri3) {
            val count = listOfNotNull(uri1, uri2, uri3).size
            if (count > 0 && count < 3) {
                listState.animateScrollToItem(count)
            }
        }

        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            item {
                ImagePickBox(
                    title = "1. Dış Kapak",
                    subtitle = "Tehlike veya uyarı levhası",
                    uri = uri1,
                    onClick = { picker1.launch("image/*") }
                )
            }
            if (uri1 != null) {
                item {
                    ImagePickBox(
                        title = "2. Ürün Etiketi",
                        subtitle = "Panonun ürün etiketi ve seri no",
                        uri = uri2,
                        onClick = { picker2.launch("image/*") }
                    )
                }
            }
            if (uri1 != null && uri2 != null) {
                item {
                    ImagePickBox(
                        title = "3. SDK İç Kısım",
                        subtitle = "Sigorta grubu (Büyük/Küçük DSYA)",
                        uri = uri3,
                        onClick = { picker3.launch("image/*") }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val selectedCount = listOf(uri1, uri2, uri3).count { it != null }
        val canAnalyze = selectedCount == 3
        
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(errorMessage!!, color = Fail, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text("$selectedCount / 3 Görsel Yüklendi", color = if (canAnalyze) Pass else Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = {
                if (file1 == null || file2 == null || file3 == null) return@Button
                isAnalyzing = true
                errorMessage = null
                
                scope.launch {
                    try {
                        val res1 = async(Dispatchers.IO) { detectFromImage(file1!!) }
                        val res2 = async(Dispatchers.IO) { detectFromImage(file2!!) }
                        val res3 = async(Dispatchers.IO) { detectFromImage(file3!!) }
                        
                        val response1 = res1.await()
                        val response2 = res2.await()
                        val response3 = res3.await()
                        
                        if (response1 == null || response2 == null || response3 == null) {
                            errorMessage = "Analiz sırasında sunucuya bağlanılamadı."
                            isAnalyzing = false
                            return@launch
                        }
                        
                        // Parse 1
                        val hasDangerSign = response1.detections.any { 
                            val cls = it.cls ?: ""
                            cls == "tehlike_levhasi_on" || cls == "tehlike_levhasi_arka" || cls == "uyari" || cls == "tehlike_levhasi" || cls == "tehlike"
                        }
                        
                        // Parse 2
                        val hasLabel = response2.detections.any { 
                            val cls = it.cls ?: ""
                            cls == "urun_etiketi" || cls == "seri_no" || cls == "etiket"
                        }
                        
                        // Parse 3
                        val grouped = response3.detections.groupBy { it.cls ?: "" }
                        val buyukCount = (grouped["dsya_buyuk"] ?: emptyList()).size
                        val kucukCount = (grouped["dsya_kucuk"] ?: emptyList()).size
                        val sapMapping = findSapCodes(buyukCount, kucukCount)
                        
                        isAnalyzing = false
                        onAnalyzeComplete(hasDangerSign, hasLabel, sapMapping)
                        
                    } catch (e: Exception) {
                        isAnalyzing = false
                        errorMessage = "Hata oluştu: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isAnalyzing) NavySoft else Yellow, contentColor = Navy),
            enabled = canAnalyze && !isAnalyzing,
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Yellow, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Yapay Zeka Destekli Analiz Sürdürülüyor...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Icon(Icons.Default.Analytics, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Toplu Analizi Başlat", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickBox(title: String, subtitle: String, uri: Uri?, onClick: () -> Unit) {
    val isSelected = uri != null
    Card(
        modifier = Modifier.width(220.dp).height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFF0FDF4) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF86EFAC) else Color(0xFFE5E7EB)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color.Transparent else Color(0xFFF9FAFB)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    coil.compose.AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Görsel Yükle", color = Color(0xFF6B7280), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(title, fontWeight = FontWeight.ExtraBold, color = Navy, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2)
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Seçildi", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun SummaryScreen(
    step1Result: Boolean?,
    step2Result: Boolean?,
    step3Result: SapMapping?,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Pass, modifier = Modifier.size(64.dp))
        Text("Denetim Tamamlandı", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Navy)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = NavySoft)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Özet Sonuçlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                HorizontalDivider(color = Color.White)
                
                SummaryRow("1. Dış Kapak", if (step1Result == true) "✅ Tehlike levhası mevcut" else "⚠️ Uyarı levhası eksik", step1Result == true)
                SummaryRow("2. Ürün Etiketi", if (step2Result == true) "✅ Etiket tespit edildi" else "⚠️ Etiket bulunamadı", step2Result == true)
                SummaryRow("3. SDK İç Kısım", if (step3Result != null) "✅ SAP Kodları: ${step3Result.sapCodes.joinToString()}" else "⚠️ SAP Kodu Eşleşmedi", step3Result != null)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Navy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Yeni Denetim Başlat", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SummaryRow(title: String, subtitle: String, isSuccess: Boolean) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, color = Color(0xFFD1D5DB), style = MaterialTheme.typography.bodySmall)
        }
    }
}
