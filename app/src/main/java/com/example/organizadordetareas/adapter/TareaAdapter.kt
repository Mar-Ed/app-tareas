package com.example.organizadordetareas.adapter

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadordetareas.R
import com.example.organizadordetareas.databinding.ItemTareaBinding
import com.example.organizadordetareas.model.Tarea

/**
 * Adaptador para mostrar la lista de [Tarea] en un RecyclerView.
 *
 * Utiliza [ListAdapter] con [DiffUtil] para actualizaciones eficientes,
 * y ViewBinding en el ViewHolder para acceso type-safe a las vistas.
 *
 * @param onTareaClick Callback invocado cuando el usuario presiona una tarjeta o la completa.
 */
class TareaAdapter(
    private val onTareaClick: (Tarea) -> Unit
) : ListAdapter<Tarea, TareaAdapter.TareaViewHolder>(TareaDiffCallback()) {

    // Instancia de MediaPlayer para reproducir sonidos de éxito
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Reproduce un sonido de éxito al completar una tarea.
     * Implementa las mejores prácticas al liberar la instancia previa de MediaPlayer.
     */
    private fun playSuccessSound(context: Context) {
        try {
            // Liberamos el reproductor previo si existe para evitar fugas de memoria
            mediaPlayer?.release()
            
            // Creamos y configuramos el reproductor con el recurso de audio success
            mediaPlayer = MediaPlayer.create(context, R.raw.success)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                if (mediaPlayer == mp) {
                    mediaPlayer = null
                }
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            android.util.Log.e("TareaAdapter", "Error al reproducir sonido: ${e.message}")
        }
    }

    /**
     * Libera el reproductor de medios cuando el adaptador ya no se use (evita memory leaks).
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ──────────────────────────────────────────────────────────────
    // ViewHolder con ViewBinding
    // ──────────────────────────────────────────────────────────────

    /**
     * ViewHolder que utiliza [ItemTareaBinding] para acceder a las vistas
     * del layout `item_tarea.xml` de forma type-safe.
     */
    class TareaViewHolder(
        private val binding: ItemTareaBinding,
        private val onTareaClick: (Tarea) -> Unit,
        private val onPlaySound: (Context) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentTarea: Tarea? = null

        init {
            // Click en la tarjeta completa: solicita togglear e invocar sonido si se va a completar
            binding.cardTarea.setOnClickListener {
                currentTarea?.let { tarea ->
                    if (!tarea.esCompletada) {
                        onPlaySound(binding.root.context)
                    }
                    onTareaClick(tarea)
                }
            }

            // Click directo en el CheckBox
            binding.checkCompletada.setOnClickListener {
                currentTarea?.let { tarea ->
                    if (!tarea.esCompletada) {
                        onPlaySound(binding.root.context)
                    }
                    onTareaClick(tarea)
                }
            }
        }

        /**
         * Vincula los datos de una [Tarea] a las vistas del layout.
         */
        fun bind(tarea: Tarea) {
            currentTarea = tarea

            binding.textTitulo.text = tarea.titulo
            binding.textDescripcion.text = tarea.descripcion
            binding.textCategoria.text = tarea.categoria.uppercase()
            binding.checkCompletada.isChecked = tarea.esCompletada
            
            // Si la tarea está completada, aplicamos tachado y reducción de opacidad
            if (tarea.esCompletada) {
                binding.textTitulo.paintFlags = binding.textTitulo.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.alpha = 0.5f
            } else {
                binding.textTitulo.paintFlags = binding.textTitulo.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.alpha = 1.0f
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Adapter overrides
    // ──────────────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ItemTareaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TareaViewHolder(binding, onTareaClick) { context ->
            playSuccessSound(context)
        }
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ──────────────────────────────────────────────────────────────
    // DiffUtil callback para actualizaciones eficientes
    // ──────────────────────────────────────────────────────────────

    /**
     * Compara ítems de [Tarea] para determinar cambios mínimos en la lista.
     * - [areItemsTheSame]: compara por [Tarea.id] (identidad).
     * - [areContentsTheSame]: compara por igualdad total (contenido).
     */
    private class TareaDiffCallback : DiffUtil.ItemCallback<Tarea>() {

        override fun areItemsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem == newItem
        }
    }
}
