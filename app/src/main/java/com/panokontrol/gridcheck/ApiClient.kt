package com.panokontrol.gridcheck

import com.google.gson.annotations.SerializedName
import okhttp3.Dns
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
import java.net.InetAddress
import java.net.Inet4Address
import java.util.concurrent.TimeUnit

// ==== VERİ MODELLERİ ====
data class Detection(
    @SerializedName("class", alternate = ["cls", "name", "label"]) val cls: String? = "Bilinmeyen",
    val confidence: Double = 0.0,
    val points: List<List<Double>> = emptyList()
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

// Emülatör IPv6 erişim hatalarını önlemek için IPv4 adreslerini tercih eden DNS çözücü
private val ipv4PreferredDns = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val addresses = Dns.SYSTEM.lookup(hostname)
        val ipv4List = addresses.filterIsInstance<Inet4Address>()
        return if (ipv4List.isNotEmpty()) ipv4List else addresses
    }
}

private val okHttpClient = OkHttpClient.Builder()
    .dns(ipv4PreferredDns)
    .addInterceptor(ngrokSkipInterceptor)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
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
