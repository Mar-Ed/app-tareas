package com.example.organizadordetareas.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.organizadordetareas.R
import kotlin.concurrent.thread

/**
 * Servicio en segundo plano para reproducir música de concentración.
 * Sobrevive al ciclo de vida de la pantalla (Fragments/Activities).
 */
class MusicService : Service() {

    private val TAG = "MusicServiceDebug"
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Servicio creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Iniciando servicio y preparando música...")

        // Tarea asíncrona usando hilos (thread {}) para evitar bloquear la UI
        thread {
            Log.d(TAG, "Hilo en segundo plano: Simulando carga de configuración o procesamiento...")
            Thread.sleep(1000) // Simular retardo
            
            // Regresamos al flujo principal para tocar la música,
            // aunque MediaPlayer.create es rápido y puede ir directo.
            if (mediaPlayer == null) {
                // Instanciando hardware de audio a través de MediaPlayer
                mediaPlayer = MediaPlayer.create(this, R.raw.focus_music)
                mediaPlayer?.isLooping = true
            }

            mediaPlayer?.start()
            Log.d(TAG, "Hilo en segundo plano: Música iniciada")
        }

        Toast.makeText(this, "🎶 Servicio de Música Iniciado", Toast.LENGTH_SHORT).show()

        // START_STICKY para que el sistema lo reinicie si se queda sin memoria
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: Destruyendo servicio y liberando hardware")
        
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release() // Liberar recursos del hardware de salida de audio
        }
        mediaPlayer = null
        
        Toast.makeText(this, "⏹ Música detenida", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // No permitimos binding en este servicio simple
        return null
    }
}
