package com.example.examenmoviles.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:5275/"
    private const val CACHE_SIZE = 10 * 1024 * 1024 // 10 MiB

    // Variable para almacenar el contexto de la aplicación
    private var appContext: Context? = null

    // Inicializar el contexto de la aplicación
    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    // Crear la caché
    private fun createCache(): Cache {
        val context = appContext
            ?: throw IllegalStateException("RetrofitInstance no ha sido inicializado. Llama a init(context) primero.")
        return Cache(context.cacheDir, CACHE_SIZE.toLong())
    }

    // Verificar conectividad de red
    private fun hasNetwork(): Boolean {
        val context = appContext
            ?: throw IllegalStateException("RetrofitInstance no ha sido inicializado. Llama a init(context) primero.")
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Crear OkHttpClient
    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(createCache())
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork()) {
                    request.newBuilder().header("Cache-Control", "public, max-age=5").build()
                } else {
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}"
                    ).build()
                }
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Inicializar OkHttpClient de forma perezosa
    private val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient()
    }

    // Inicializar Retrofit de forma perezosa
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Servicios de API
    val courseApi: CourseApiService by lazy {
        retrofit.create(CourseApiService::class.java)
    }

    val studentApi: StudentApiService by lazy {
        retrofit.create(StudentApiService::class.java)
    }
}
