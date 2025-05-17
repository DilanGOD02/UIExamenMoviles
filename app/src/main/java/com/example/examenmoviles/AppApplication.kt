package com.example.examenmoviles

import android.app.Application
import com.example.examenmoviles.network.RetrofitInstance

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar RetrofitInstance con el contexto de la aplicación
        RetrofitInstance.init(this)
    }
}