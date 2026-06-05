package com.example.organizadordetareas

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import kotlin.concurrent.thread

/**
 * Servicio en segundo plano para simular la sincronización de tareas locales hacia el backend.
 * Utiliza un hilo secundario con thread {} para garantizar que no se bloquee la interfaz de usuario (UI).
 */
class SyncService : Service() {

    private var isRunning = false
    private var syncThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("SyncService", "SyncService creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SyncService", "SyncService iniciado (onStartCommand)")
        
        if (!isRunning) {
            isRunning = true
            // Iniciamos un hilo secundario con thread {} de Kotlin
            syncThread = thread(start = true) {
                try {
                    while (isRunning) {
                        Log.d("SyncService", "Sincronizando tareas locales con el backend Node.js (Simulado)...")
                        
                        // Espera de 5 segundos entre cada iteración de sincronización
                        Thread.sleep(5000)
                    }
                } catch (e: InterruptedException) {
                    Log.d("SyncService", "Hilo de sincronización interrumpido")
                }
            }
            Toast.makeText(this, "Sincronización iniciada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "El servicio de sincronización ya está corriendo", Toast.LENGTH_SHORT).show()
        }

        // START_STICKY hace que el sistema intente recrear el servicio si es destruido por falta de recursos
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        // Interrumpimos el hilo para que despierte del sleep de inmediato y termine
        syncThread?.interrupt()
        Log.d("SyncService", "SyncService destruido")
        Toast.makeText(this, "Sincronización detenida", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Al ser un Started Service (no Bound), retornamos null
        return null
    }
}
