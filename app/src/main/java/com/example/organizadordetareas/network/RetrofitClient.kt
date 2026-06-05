package com.example.organizadordetareas.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton que provee la instancia configurada de [TareaApi].
 *
 * Configuración:
 * - Base URL apunta a `10.0.2.2:3000` — esta es la IP especial que el
 *   emulador de Android usa para comunicarse con el `localhost` de la
 *   máquina host (tu computadora donde corre el servidor Node.js).
 * - Usa [GsonConverterFactory] para serializar/deserializar Kotlin ↔ JSON
 *   automáticamente, mapeando los campos de [Tarea] al JSON del backend.
 *
 * Uso:
 * ```kotlin
 * val api = RetrofitClient.tareaApi
 * api.obtenerTareas().enqueue(object : Callback<ApiResponse<List<Tarea>>> { ... })
 * ```
 *
 * Nota sobre dispositivos físicos:
 * Si pruebas en un dispositivo real conectado a la misma red WiFi,
 * cambia [BASE_URL] por la IP local de tu computadora (ej: "http://192.168.1.100:3000/").
 */
object RetrofitClient {

    // ──────────────────────────────────────────────────────────────
    // Configuración
    // ──────────────────────────────────────────────────────────────

    /**
     * 10.0.2.2 = localhost del host desde el emulador de Android.
     * Si usas un dispositivo físico, reemplaza con la IP de tu máquina en la red local.
     */
    private const val BASE_URL = "http://10.0.2.2:3000/"

    // ──────────────────────────────────────────────────────────────
    // Instancia lazy de Retrofit
    // ──────────────────────────────────────────────────────────────

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ──────────────────────────────────────────────────────────────
    // API pública
    // ──────────────────────────────────────────────────────────────

    /** Instancia de [TareaApi] lista para hacer llamadas al backend. */
    val tareaApi: TareaApi by lazy {
        retrofit.create(TareaApi::class.java)
    }
}
