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

    // Crear la caché
    private fun createCache(context: Context): Cache {
        return Cache(context.cacheDir, CACHE_SIZE.toLong())
    }

    // Verificar conectividad de red
    private fun hasNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Crear OkHttpClient
    private fun createOkHttpClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(createCache(context))
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork(context))
                    request.newBuilder().header("Cache-Control", "public, max-age=5").build()
                else
                    request.newBuilder().header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=${60 * 60 * 24 * 7}"
                    ).build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Contexto global para inicialización
    private lateinit var appContext: Context

    // Método para inicializar el contexto
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Inicializar OkHttpClient de forma perezosa
    private val okHttpClient: OkHttpClient by lazy {
        createOkHttpClient(appContext)
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Propiedad de CourseApiService
    val courseApi: CourseApiService by lazy {
        retrofit.create(CourseApiService::class.java)
    }

    // Propiedad de StudentApiService
    val studentApi: StudentApiService by lazy {
        retrofit.create(StudentApiService::class.java)
    }


}

