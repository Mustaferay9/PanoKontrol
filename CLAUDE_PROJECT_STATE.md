# PanoKontrol Android Projesi - YOLO Backend & API Entegrasyon Durumu

Bu belge, **PanoKontrol (GridCheck)** Android projesinin mevcut mimarisini, YOLO model entegrasyonunu ve oluşturulan API dosyalarının detaylarını Claude ile paylaşılmak üzere özetlemektedir.

---

## 📌 1. Proje Genel Bilgileri
- **Uygulama Adı:** PanoKontrol (Enerjisa Dağıtım Panosu Yapay Zeka Denetim Uygulaması)
- **Paket Adı:** `com.panokontrol.gridcheck`
- **Mimari & UI:** %100 Modern **Jetpack Compose**, Material 3, Kotlin Coroutines, Navigation Compose
- **Network Kütüphaneleri:** Retrofit 2.11.0, OkHttp 4.12.0, Gson Converter, Coil Compose (Görsel yükleme)
- **Hedef SDK:** Android 15 (API 35), Min SDK: Android 7.0 (API 24)
- **Backend Durumu:** Google Colab / Ngrok üzerinde çalışan FastAPI/Flask tabanlı YOLO segmentasyon/tespit servisi.

---

## ⚡ 2. Mevcut API & Mock Veri Durumu

| Ekran / Dosya | Durum | Açıklama |
| :--- | :--- | :--- |
| **`ApiClient.kt`** | **CANLI / GERÇEK** | Retrofit arayüzü (`ApiService`), Ngrok interceptor ve `detectFromImage(file)` fonksiyonunu barındırır. |
| **`ApiTestScreen.kt`** | **CANLI / GERÇEK** | Galeriden seçilen görseli backend'e multipart yükler, YOLO'dan dönen gerçek tespitleri (`cls`, `confidence`) ekranda gösterir. **Mock/sahte veri içermez.** |
| **`MainActivity.kt`** | **CANLI TEST MODU** | Açılışta doğrudan `ApiTestScreen()` composable ekranını render eder. |
| **`ProcessingScreen.kt` & `ResultScreen.kt`** | **DEMO / SUNUM MODU** | Projenin önceki sunum arayüzleridir (2 sn'lik tarayıcı lazer animasyonu ve statik pano demosu içerir). |

---

## 🔌 3. API Veri Sözleşmesi (Data Contract)

### İstek (Request)
- **Endpoint:** `POST https://brisket-jokester-plural.ngrok-free.dev/predict`
- **Format:** `multipart/form-data`
- **Parametre:** `image` (JPEG/PNG dosya)
- **Header:** `ngrok-skip-browser-warning: true`

### Yanıt (Response Model)
```json
{
  "count": 2,
  "image_width": 1280,
  "image_height": 720,
  "detections": [
    {
      "cls": "salter",
      "confidence": 0.945,
      "points": [[120.0, 340.0], [250.0, 340.0], [250.0, 500.0], [120.0, 500.0]]
    }
  ]
}
```

---

## 📂 4. Dosya İçerikleri ve Kodlar

### A. `ApiClient.kt`
**Konum:** `app/src/main/java/com/panokontrol/gridcheck/ApiClient.kt`
```kotlin
package com.panokontrol.gridcheck

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

// ==== VERİ MODELLERİ ====
data class Detection(
    val cls: String,
    val confidence: Double,
    val points: List<List<Double>>
)

data class PredictResponse(
    val count: Int,
    val detections: List<Detection>,
    val image_width: Int,
    val image_height: Int
)

// ==== API ARAYÜZÜ ====
interface ApiService {
    @Multipart
    @POST("predict")
    suspend fun predict(@Part image: MultipartBody.Part): Response<PredictResponse>
}

// ==== AYARLAR ====
// NOT: ngrok hücresini her yeniden çalıştırdığında bu URL değişir, güncellemeyi unutma
const val BASE_URL = "https://brisket-jokester-plural.ngrok-free.dev/"

// ngrok'un tarayıcı uyarı sayfasını atlamak için header ekleyen interceptor
private val ngrokSkipInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
        .addHeader("ngrok-skip-browser-warning", "true")
        .build()
    chain.proceed(request)
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(ngrokSkipInterceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

private val apiService = retrofit.create(ApiService::class.java)

// ==== DIŞARIDAN ÇAĞRILACAK FONKSİYON ====
suspend fun detectFromImage(imageFile: File): PredictResponse? {
    val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

    val response = apiService.predict(body)
    return if (response.isSuccessful) response.body() else null
}
```

---

### B. `ApiTestScreen.kt`
**Konum:** `app/src/main/java/com/panokontrol/gridcheck/ui/screens/test/ApiTestScreen.kt`
```kotlin
package com.panokontrol.gridcheck.ui.screens.test

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.panokontrol.gridcheck.PredictResponse
import com.panokontrol.gridcheck.detectFromImage
import com.panokontrol.gridcheck.ui.theme.Navy
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
    var statusMessage by remember { mutableStateOf("Önce bir görsel seçin") }
    var responseResult by remember { mutableStateOf<PredictResponse?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            selectedFile = uriToFile(context, it)
            statusMessage = "Görsel seçildi, şimdi 'Tespit Et' butonuna bas"
            responseResult = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PanoKontrol · Model Test Ekranı", fontWeight = FontWeight.Bold, color = Navy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Görsel Önizleme
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedUri),
                            contentDescription = "Seçilen Görsel",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Henüz bir görsel seçilmedi", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Görsel Seç")
                }

                Button(
                    onClick = {
                        val file = selectedFile
                        if (file == null) {
                            statusMessage = "Önce bir görsel seç"
                            return@Button
                        }
                        isLoading = true
                        statusMessage = "Tespit ediliyor..."
                        scope.launch {
                            try {
                                val res = withContext(Dispatchers.IO) { detectFromImage(file) }
                                isLoading = false
                                if (res == null) {
                                    statusMessage = "Hata: API'den cevap alınamadı"
                                } else {
                                    responseResult = res
                                    val sb = java.lang.StringBuilder()
                                    sb.append("Toplam tespit: ${res.count}\n\n")
                                    res.detections.forEach { d ->
                                        val confStr = String.format("%.2f", d.confidence)
                                        sb.append("${d.cls} - güven: $confStr\n")
                                    }
                                    statusMessage = sb.toString()
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                statusMessage = "Hata: ${e.localizedMessage ?: e.message}"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Navy),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Navy)
                    } else {
                        Text("Tespit Et", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sonuç Alanı
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF071B3E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sonuç Paneli",
                        color = Yellow,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = statusMessage,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    responseResult?.let { res ->
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Görsel Boyutu: ${res.image_width} x ${res.image_height}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_image.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}
```

---

### C. `MainActivity.kt`
**Konum:** `app/src/main/java/com/panokontrol/gridcheck/MainActivity.kt`
```kotlin
package com.panokontrol.gridcheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.panokontrol.gridcheck.ui.screens.test.ApiTestScreen
import com.panokontrol.gridcheck.ui.theme.PanoKontrolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PanoKontrolTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Adım 4: Görsel Seç & YOLO API Tespit Test Ekranı
                    ApiTestScreen()
                }
            }
        }
    }
}
```

---

### D. `activity_main.xml` (Legacy View Referansı)
**Konum:** `app/src/main/res/layout/activity_main.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <Button
        android:id="@+id/pickButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Görsel Seç" />

    <ImageView
        android:id="@+id/imageView"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        android:layout_marginTop="8dp"
        android:scaleType="centerInside" />

    <Button
        android:id="@+id/detectButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="Tespit Et" />

    <TextView
        android:id="@+id/resultText"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="Sonuçlar burada görünecek" />

</LinearLayout>
```
