package com.panokontrol.gridcheck.ui.screens.test

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.panokontrol.gridcheck.findSapCodes
import com.panokontrol.gridcheck.SapMapping
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.panokontrol.gridcheck.Detection
import com.panokontrol.gridcheck.PredictResponse
import com.panokontrol.gridcheck.detectFromImage
import com.panokontrol.gridcheck.ui.theme.Fail
import com.panokontrol.gridcheck.ui.theme.FailBg
import com.panokontrol.gridcheck.ui.theme.Navy
import com.panokontrol.gridcheck.ui.theme.NavyDark
import com.panokontrol.gridcheck.ui.theme.NavySoft
import com.panokontrol.gridcheck.ui.theme.Pass
import com.panokontrol.gridcheck.ui.theme.PassBg
import com.panokontrol.gridcheck.ui.theme.Yellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiTestScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var responseResult by remember { mutableStateOf<PredictResponse?>(null) }
    var isFullScreenOpen by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedFile = uriToFile(context, it)
            errorMessage = null
            responseResult = null
        }
    }

    // Laser scanning animation transition
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    // Fullscreen Zoom Dialog
    if (isFullScreenOpen && selectedUri != null) {
        Dialog(
            onDismissRequest = { isFullScreenOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                ZoomableInspectionView(
                    imageUri = selectedUri,
                    responseResult = responseResult,
                    isLoading = false,
                    isFullScreen = true,
                    onFullScreenToggle = { isFullScreenOpen = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Canlı Pano Denetimi",
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "YOLOv8 Yapay Zeka Analizi",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Central Image / Scanning / Interactive Zoom Overlay Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071F4C)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (selectedUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ZoomableInspectionView(
                            imageUri = selectedUri,
                            responseResult = responseResult,
                            isLoading = isLoading,
                            isFullScreen = false,
                            onFullScreenToggle = { isFullScreenOpen = true },
                            modifier = Modifier.fillMaxSize()
                        )

                        // If Loading: Animated Laser Scanning Overlay
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x66071F4C))
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.TopCenter)
                                    .graphicsLayer {
                                        translationY = scanOffset * density
                                    }
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                Yellow,
                                                Color(0xFF00E5FF),
                                                Yellow,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xDD071B3E),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            color = Yellow,
                                            strokeWidth = 3.5.dp,
                                            modifier = Modifier.size(34.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xCC071B3E)
                                ) {
                                    Text(
                                        text = "YOLOv8 Taraması Yapılıyor...",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Empty State Placeholder
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x22FFFFFF),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = Yellow
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            "Denetim Yapılacak Fotoğrafı Seçin",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Galeriden veya kameradan pano fotoğrafı yükleyin",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Navy),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (selectedUri == null) "Pano Görseli Seç" else if (responseResult != null) "Yeni Analiz Başlat" else "Görseli Değiştir",
                        fontWeight = FontWeight.Bold
                    )
                }

                if (responseResult == null) {
                    Button(
                        onClick = {
                            val file = selectedFile
                            if (file == null) {
                                errorMessage = "Lütfen önce denetlenecek bir pano fotoğrafı seçin."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    val res = withContext(Dispatchers.IO) { detectFromImage(file) }
                                    isLoading = false
                                    if (res == null) {
                                        errorMessage = "API'den cevap alınamadı. Lütfen Ngrok bağlantınızı kontrol edin."
                                    } else {
                                        responseResult = res
                                    }
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Bağlantı Hatası: ${e.localizedMessage ?: e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = NavyDark),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && selectedUri != null,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = NavyDark)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NavyDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Analizi Başlat", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Display Card
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = FailBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Fail.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Fail, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Fail,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Results Section
            if (responseResult != null) {
                val res = responseResult!!
                val grouped = res.detections.groupBy { it.cls ?: "" }
                val dsyaBuyukRaw = grouped["dsya_buyuk"] ?: emptyList()
                val dsyaKucukRaw = grouped["dsya_kucuk"] ?: emptyList()

                val dsyaBuyukSorted = dsyaBuyukRaw.sortedBy { d ->
                    d.points.minOfOrNull { pt -> if (pt.isNotEmpty()) pt[0].toFloat() else 0f } ?: 0f
                }

                val buyukCount = dsyaBuyukSorted.size
                val kucukCount = dsyaKucukRaw.size
                val sapMapping = findSapCodes(buyukCount, kucukCount)

                val dsya400AList: List<Detection>
                val dsya250AList: List<Detection>
                val dsyaGenericList: List<Detection>
                val dsya160AList = dsyaKucukRaw

                if (sapMapping != null) {
                    dsya400AList = dsyaBuyukSorted.take(sapMapping.count400A)
                    dsya250AList = dsyaBuyukSorted.drop(sapMapping.count400A).take(sapMapping.count250A)
                    dsyaGenericList = emptyList()
                } else {
                    dsya400AList = emptyList()
                    dsya250AList = emptyList()
                    dsyaGenericList = dsyaBuyukSorted
                }

                val tehlikeList = (grouped["tehlike_levhasi"] ?: emptyList()) + 
                                  (grouped["tehlike"] ?: emptyList()) + 
                                  res.detections.filter { (it.cls ?: "").contains("tehlike") && it.cls !in listOf("tehlike_levhasi", "tehlike") }
                val urunEtiketList = (grouped["urun_etiketi"] ?: emptyList()) + 
                                     (grouped["etiket"] ?: emptyList()) + 
                                     res.detections.filter { (it.cls ?: "").contains("etiket") && it.cls !in listOf("urun_etiketi", "etiket") }
                val klemensList = grouped["klemens"] ?: emptyList()
                val baraList = grouped["bara"] ?: emptyList()

                val handledDetections = (dsyaBuyukRaw + dsyaKucukRaw + tehlikeList + urunEtiketList + klemensList + baraList).toSet()
                val otherGrouped = res.detections.filter { it !in handledDetections }.groupBy { it.cls ?: "ekipman" }

                val hasDsya = dsyaBuyukRaw.isNotEmpty() || dsyaKucukRaw.isNotEmpty()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Summary Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyDark)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Pass, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Denetim Tamamlandı",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = PassBg
                                ) {
                                    Text(
                                        text = "Toplam ${res.count} Ekipman",
                                        color = Pass,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(14.dp))

                            if (hasDsya) {
                                // Highlighted Equipment Count Grid (DSYA) with distinct class colors
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (dsya400AList.isNotEmpty()) {
                                        EquipmentQuickCountBadge(
                                            title = "400A DSYA",
                                            count = dsya400AList.size,
                                            accentColor = Color(0xFFFFB300),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (dsya250AList.isNotEmpty()) {
                                        EquipmentQuickCountBadge(
                                            title = "250A DSYA",
                                            count = dsya250AList.size,
                                            accentColor = Color(0xFFFF9100),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (dsyaGenericList.isNotEmpty()) {
                                        EquipmentQuickCountBadge(
                                            title = "DSYA",
                                            count = dsyaGenericList.size,
                                            accentColor = Color(0xFFFFB300),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (dsya160AList.isNotEmpty()) {
                                        EquipmentQuickCountBadge(
                                            title = "160A DSYA",
                                            count = dsya160AList.size,
                                            accentColor = Color(0xFF00E5FF),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            } else {
                                // Summary info for non-DSYA scans (e.g. Danger signs, Exterior labels)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Çözünürlük: ${res.image_width} x ${res.image_height}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Model: YOLOv8 Seg",
                                        color = Yellow,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // SAP Kodu Eşleşmesi Bölümü
                    val totalDsya = buyukCount + kucukCount
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (sapMapping != null) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (sapMapping != null) Color(0xFF86EFAC) else Color(0xFFCBD5E1)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (sapMapping != null) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (sapMapping != null) Color(0xFF16A34A) else Navy,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SAP Kodu Eşleşmesi",
                                        fontWeight = FontWeight.Bold,
                                        color = NavyDark,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = when {
                                        sapMapping != null -> Color(0xFFDCFCE7)
                                        totalDsya > 0 -> Color(0xFFFEF3C7)
                                        else -> Color(0xFFE2E8F0)
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            sapMapping != null -> "Eşleşti"
                                            totalDsya > 0 -> "Tespit Edilemedi"
                                            else -> "Eşleşme Yapılmadı"
                                        },
                                        color = when {
                                            sapMapping != null -> Color(0xFF15803D)
                                            totalDsya > 0 -> Color(0xFFB45309)
                                            else -> Color(0xFF475569)
                                        },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (sapMapping != null) {
                                Text(
                                    text = "SAP Kodları: ${sapMapping.sapCodes.joinToString(", ")}",
                                    color = Color(0xFF15803D),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                if (sapMapping.not.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Açıklama: ${sapMapping.not}",
                                        color = Color(0xFF166534),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                Text(
                                    text = if (totalDsya > 0) {
                                        "Tanımlı olmayan DSYA kombinasyonu, SAP kodu tespit edilemedi."
                                    } else {
                                        "DSYA tespit edilemediği için SAP eşleşmesi yapılmadı."
                                    },
                                    color = Color(0xFF64748B),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Detailed Equipment Inventory List
                    Text(
                        text = "PANODAKİ EKİPMAN SAYIM LİSTESİ (Detay için dokunun)",
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )

                    if (res.detections.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NavySoft)
                        ) {
                            Text(
                                "Görsel üzerinde tanımlı herhangi bir pano ekipmanı tespit edilemedi.",
                                color = Navy,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        // 1. 400A DSYA Group Card (Expandable to see confidence rates)
                        if (dsya400AList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "400A DSYA",
                                description = "Pano ana besleme / yüksek akım sigortası",
                                detections = dsya400AList,
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFFFFB300),
                                badgeColor = Color(0xFFFEF3C7),
                                badgeTextColor = Color(0xFFB45309)
                            )
                        }

                        // 2. 250A DSYA Group Card (Expandable to see confidence rates)
                        if (dsya250AList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "250A DSYA",
                                description = "Besleme ve ara yük sigorta grubu",
                                detections = dsya250AList,
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFFFF9100),
                                badgeColor = Color(0xFFFFEDD5),
                                badgeTextColor = Color(0xFFC2410C)
                            )
                        }

                        // 3. Generic DSYA Card (if unmatched)
                        if (dsyaGenericList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "DSYA Sigorta Grubu",
                                description = "Pano besleme sigorta grubu",
                                detections = dsyaGenericList,
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFFFFB300),
                                badgeColor = Color(0xFFFEF3C7),
                                badgeTextColor = Color(0xFFB45309)
                            )
                        }

                        // 4. 160A DSYA Group Card (Expandable to see confidence rates)
                        if (dsya160AList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "160A DSYA",
                                description = "Standart çıkış ve dağıtım sigorta kolları",
                                detections = dsya160AList,
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFF00E5FF),
                                badgeColor = Color(0xFFE0F2FE),
                                badgeTextColor = Color(0xFF0369A1)
                            )
                        }

                        // 5. Tehlike Levhası (Expandable to see confidence rates)
                        if (tehlikeList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "Ölüm Tehlikesi Uyarı Levhası",
                                description = "TEDAŞ standardı ikaz levhası",
                                detections = tehlikeList,
                                icon = Icons.Default.Warning,
                                accentColor = Color(0xFFFF1744),
                                badgeColor = Color(0xFFFFE4E6),
                                badgeTextColor = Color(0xFFBE123C)
                            )
                        }

                        // 6. Ürün ve Tip Bilgi Etiketi (Expandable to see confidence rates)
                        if (urunEtiketList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "Ürün ve Tip Bilgi Etiketi",
                                description = "Pano teknik künyesi ve CE onay etiketi",
                                detections = urunEtiketList,
                                icon = Icons.Default.Info,
                                accentColor = Color(0xFF00E676),
                                badgeColor = Color(0xFFDCFCE7),
                                badgeTextColor = Color(0xFF15803D)
                            )
                        }

                        // 7. Klemens Group Card
                        if (klemensList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "Klemens Bağlantı Bloğu",
                                description = "Kablo giriş ve klemens bağlantı sırası",
                                detections = klemensList,
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFFD500F9),
                                badgeColor = Color(0xFFF3E8FF),
                                badgeTextColor = Color(0xFF7E22CE)
                            )
                        }

                        // 8. Bara Group Card
                        if (baraList.isNotEmpty()) {
                            EquipmentGroupCountCard(
                                title = "Bakır Bara Hattı",
                                description = "Pano ana enerji dağıtım bakır baraları",
                                detections = baraList,
                                icon = Icons.Default.Bolt,
                                accentColor = Color(0xFFFF6D00),
                                badgeColor = Color(0xFFFFEDD5),
                                badgeTextColor = Color(0xFFC2410C)
                            )
                        }



                        // 7. Diğer Ekipmanlar
                        otherGrouped.forEach { (clsKey, items) ->
                            val cleanName = clsKey.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                            EquipmentGroupCountCard(
                                title = cleanName,
                                description = "Tespit edilen yardımcı pano bileşeni",
                                detections = items,
                                icon = Icons.Default.Bolt,
                                accentColor = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

/**
 * Interactive Pinch-to-Zoom & Pan Inspection View
 * Scales both the image and the segmentation overlay in perfect synchronization.
 */
@Composable
private fun ZoomableInspectionView(
    imageUri: Uri?,
    responseResult: PredictResponse?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    onFullScreenToggle: (() -> Unit)? = null
) {
    var zoomScale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .clip(if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(18.dp))
            .background(Color(0xFF071F4C))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (zoomScale > 1.2f) {
                            zoomScale = 1f
                            panOffset = Offset.Zero
                        } else {
                            zoomScale = 2.5f
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (zoomScale * zoom).coerceIn(1f, 6f)
                    zoomScale = newScale
                    if (newScale == 1f) {
                        panOffset = Offset.Zero
                    } else {
                        val maxPan = 1000f * (newScale - 1f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-maxPan, maxPan),
                            y = (panOffset.y + pan.y).coerceIn(-maxPan, maxPan)
                        )
                    }
                }
            }
    ) {
        // Scalable Content Layer (Image + Polygon/Badge Overlay)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = "Pano Görseli",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Canvas Detection Overlay
                if (responseResult != null && !isLoading && responseResult.detections.isNotEmpty()) {
                    val imgW = responseResult.image_width.toFloat()
                    val imgH = responseResult.image_height.toFloat()

                    val dsyaDetectionMap = remember(responseResult) {
                        val map = mutableMapOf<Detection, Pair<String, Triple<Color, Int, Int>>>()
                        if (responseResult != null) {
                            val bList = responseResult.detections.filter { it.cls == "dsya_buyuk" }.sortedBy { d ->
                                d.points.minOfOrNull { pt -> if (pt.isNotEmpty()) pt[0].toFloat() else 0f } ?: 0f
                            }
                            val kList = responseResult.detections.filter { it.cls == "dsya_kucuk" }
                            val mapping = findSapCodes(bList.size, kList.size)

                            if (mapping != null) {
                                val list400 = bList.take(mapping.count400A)
                                val list250 = bList.drop(mapping.count400A).take(mapping.count250A)

                                list400.forEach { d ->
                                    map[d] = Pair(
                                        "400A DSYA",
                                        Triple(
                                            Color(0xFFFFB300), // Amber
                                            android.graphics.Color.argb(245, 255, 179, 0),
                                            android.graphics.Color.argb(255, 7, 31, 76)
                                        )
                                    )
                                }
                                list250.forEach { d ->
                                    map[d] = Pair(
                                        "250A DSYA",
                                        Triple(
                                            Color(0xFFFF9100), // Vibrant Orange
                                            android.graphics.Color.argb(245, 255, 145, 0),
                                            android.graphics.Color.argb(255, 7, 31, 76)
                                        )
                                    )
                                }
                            } else {
                                bList.forEach { d ->
                                    map[d] = Pair(
                                        "DSYA",
                                        Triple(
                                            Color(0xFFFFB300),
                                            android.graphics.Color.argb(245, 255, 179, 0),
                                            android.graphics.Color.argb(255, 7, 31, 76)
                                        )
                                    )
                                }
                            }

                            kList.forEach { d ->
                                map[d] = Pair(
                                    "160A DSYA",
                                    Triple(
                                        Color(0xFF00E5FF), // Cyan
                                        android.graphics.Color.argb(245, 0, 229, 255),
                                        android.graphics.Color.argb(255, 7, 31, 76)
                                    )
                                )
                            }
                        }
                        map
                    }

                    if (imgW > 0f && imgH > 0f) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height

                            val scaleRatio = minOf(canvasW / imgW, canvasH / imgH)
                            val renderedW = imgW * scaleRatio
                            val renderedH = imgH * scaleRatio
                            val offsetX = (canvasW - renderedW) / 2f
                            val offsetY = (canvasH - renderedH) / 2f

                            responseResult.detections.forEach { d ->
                                if (d.points.isNotEmpty()) {
                                    val clsName = d.cls ?: ""
                                    val customInfo = dsyaDetectionMap[d]
                                    val (strokeComposeColor, badgeBgColorInt, badgeTextColorInt) = if (customInfo != null) {
                                        customInfo.second
                                    } else {
                                        when {
                                            clsName.contains("tehlike") -> Triple(
                                                Color(0xFFFF1744), // Tehlike Levhası - Parlak Kırmızı
                                                android.graphics.Color.argb(245, 255, 23, 68),
                                                android.graphics.Color.WHITE
                                            )
                                            clsName.contains("etiket") -> Triple(
                                                Color(0xFF00E676), // Ürün Etiketi - Zümrüt Yeşili
                                                android.graphics.Color.argb(245, 0, 230, 118),
                                                android.graphics.Color.argb(255, 7, 31, 76)
                                            )
                                            clsName == "klemens" -> Triple(
                                                Color(0xFFD500F9), // Klemens - Neon Mor
                                                android.graphics.Color.argb(245, 213, 0, 249),
                                                android.graphics.Color.WHITE
                                            )
                                            clsName == "bara" -> Triple(
                                                Color(0xFFFF6D00), // Bara - Turuncu
                                                android.graphics.Color.argb(245, 255, 109, 0),
                                                android.graphics.Color.WHITE
                                            )
                                            else -> Triple(
                                                Color(0xFF38BDF8), // Diğer - Gökyüzü Mavisi
                                                android.graphics.Color.argb(245, 56, 189, 248),
                                                android.graphics.Color.argb(255, 7, 31, 76)
                                            )
                                        }
                                    }

                                    val polyPath = Path()
                                    var minX = Float.MAX_VALUE
                                    var minY = Float.MAX_VALUE

                                    d.points.forEachIndexed { idx, pt ->
                                        if (pt.size >= 2) {
                                            val px = offsetX + (pt[0].toFloat() * scaleRatio)
                                            val py = offsetY + (pt[1].toFloat() * scaleRatio)
                                            if (idx == 0) {
                                                polyPath.moveTo(px, py)
                                            } else {
                                                polyPath.lineTo(px, py)
                                            }
                                            if (px < minX) minX = px
                                            if (py < minY) minY = py
                                        }
                                    }
                                    polyPath.close()

                                    // Fill (subtle translucent with the class color)
                                    drawPath(
                                        path = polyPath,
                                        color = strokeComposeColor.copy(alpha = 0.12f),
                                        style = Fill
                                    )

                                    // Thinner 1.0dp Stroke matching the badge color
                                    drawPath(
                                        path = polyPath,
                                        color = strokeComposeColor,
                                        style = Stroke(
                                            width = 1.0.dp.toPx(),
                                            join = StrokeJoin.Round,
                                            cap = StrokeCap.Round
                                        )
                                    )

                                    // Label Badge
                                    val displayName = if (customInfo != null) {
                                        customInfo.first
                                    } else {
                                        when {
                                            clsName.contains("tehlike") -> "Tehlike Levhası %${(d.confidence * 100).toInt()}"
                                            clsName.contains("etiket") -> "Ürün Etiketi %${(d.confidence * 100).toInt()}"
                                            clsName == "klemens" -> "Klemens"
                                            clsName == "bara" -> "Bara"
                                            else -> "$clsName %${(d.confidence * 100).toInt()}"
                                        }
                                    }
                                    val labelText = displayName

                                    val textPaint = Paint().apply {
                                        isAntiAlias = true
                                        textSize = 9.sp.toPx()
                                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                                        color = badgeTextColorInt
                                    }

                                    val badgeBgPaint = Paint().apply {
                                        isAntiAlias = true
                                        color = badgeBgColorInt
                                        style = Paint.Style.FILL
                                    }

                                    val badgeBorderPaint = Paint().apply {
                                        isAntiAlias = true
                                        color = badgeBgColorInt
                                        style = Paint.Style.STROKE
                                        strokeWidth = 0.75.dp.toPx()
                                    }

                                    val bounds = Rect()
                                    textPaint.getTextBounds(labelText, 0, labelText.length, bounds)

                                    val padH = 3.5.dp.toPx()
                                    val padV = 1.5.dp.toPx()
                                    val badgeW = bounds.width() + padH * 2
                                    val badgeH = bounds.height() + padV * 2

                                    // Safe boundary clamping: never exceed canvas top edge (y >= 0f and y >= offsetY)
                                    val minAllowedY = maxOf(0f, offsetY)
                                    val maxAllowedY = (offsetY + renderedH - badgeH).coerceAtLeast(minAllowedY)

                                    val desiredAboveY = minY - badgeH - 2.dp.toPx()
                                    val badgeY = if (desiredAboveY >= minAllowedY) {
                                        desiredAboveY.coerceIn(minAllowedY, maxAllowedY)
                                    } else {
                                        (minY + 2.dp.toPx()).coerceIn(minAllowedY, maxAllowedY)
                                    }

                                    val minAllowedX = maxOf(0f, offsetX)
                                    val maxAllowedX = (offsetX + renderedW - badgeW).coerceAtLeast(minAllowedX)
                                    val badgeX = minX.coerceIn(minAllowedX, maxAllowedX)

                                    val badgeRect = RectF(badgeX, badgeY, badgeX + badgeW, badgeY + badgeH)
                                    val cr = 2.5.dp.toPx()

                                    drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, cr, cr, badgeBgPaint)
                                    drawContext.canvas.nativeCanvas.drawRoundRect(badgeRect, cr, cr, badgeBorderPaint)

                                    val tx = badgeX + padH - bounds.left
                                    val ty = badgeY + padV - bounds.top
                                    drawContext.canvas.nativeCanvas.drawText(labelText, tx, ty, textPaint)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overlay Controls (Zoom indicator badge, Reset Zoom button, Fullscreen toggle)
        if (imageUri != null && !isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom Indicator / Double Tap Hint Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD071B3E)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (zoomScale > 1.05f) "🔍 ${String.format("%.1f", zoomScale)}x Yakınlaştırma" else "🔍 Çift dokun / parmakla yakınlaştır",
                            color = if (zoomScale > 1.05f) Yellow else Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Reset Button (if zoomed)
                    if (zoomScale > 1.05f) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xDD071B3E),
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.RestartAlt,
                                    contentDescription = "Sıfırla",
                                    tint = Yellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Fullscreen Toggle Button
                    if (onFullScreenToggle != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xDD071B3E),
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(
                                onClick = onFullScreenToggle,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (isFullScreen) Icons.Default.Close else Icons.Default.Fullscreen,
                                    contentDescription = if (isFullScreen) "Kapat" else "Tam Ekran",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EquipmentQuickCountBadge(
    title: String,
    count: Int,
    accentColor: Color = Yellow,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0x22FFFFFF),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count Adet",
                color = if (count > 0) accentColor else Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun EquipmentGroupCountCard(
    title: String,
    description: String,
    detections: List<Detection>,
    icon: ImageVector = Icons.Default.Bolt,
    accentColor: Color,
    badgeColor: Color = PassBg,
    badgeTextColor: Color = Pass
) {
    var isExpanded by remember { mutableStateOf(false) }

    val count = detections.size
    val avgConfidence = if (detections.isNotEmpty()) {
        (detections.map { it.confidence }.average() * 100).toInt()
    } else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = if (accentColor == Yellow) NavyDark else accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = NavyDark,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeColor
                    ) {
                        Text(
                            text = "$count Adet",
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Detayları Göster",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DETAYLI DOĞRULUK ORANLARI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE0E7FF)
                        ) {
                            Text(
                                text = "Ortalama: %$avgConfidence",
                                color = Navy,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    detections.forEachIndexed { index, det ->
                        val conf = (det.confidence * 100).toInt()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFE2E8F0),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (detections.size == 1) title else "${index + 1}. Ekipman Tespiti",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = NavyDark
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { det.confidence.toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (conf >= 80) Color(0xFF16A34A) else Color(0xFFF59E0B),
                                    trackColor = Color(0xFFE2E8F0),
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (conf >= 80) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "%$conf",
                                        color = if (conf >= 80) Color(0xFF16A34A) else Color(0xFFD97706),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_image.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}
