package com.example.organizadordetareas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadordetareas.databinding.ItemTareaBinding
import com.example.organizadordetareas.model.Tarea

/**
 * Adaptador para mostrar la lista de [Tarea] en un RecyclerView.
 *
 * Utiliza [ListAdapter] con [DiffUtil] para actualizaciones eficientes,
 * y ViewBinding en el ViewHolder para acceso type-safe a las vistas.
 *
 * @param onTareaClick Callback invocado cuando el usuario presiona una tarjeta.
 */
class TareaAdapter(
    private val onTareaClick: (Tarea) -> Unit
) : ListAdapter<Tarea, TareaAdapter.TareaViewHolder>(TareaDiffCallback()) {

    // ──────────────────────────────────────────────────────────────
    // ViewHolder con ViewBinding
    // ──────────────────────────────────────────────────────────────

    /**
     * ViewHolder que utiliza [ItemTareaBinding] para acceder a las vistas
     * del layout `item_tarea.xml` de forma type-safe.
     */
    class TareaViewHolder(
        private val binding: ItemTareaBinding,
        private val onTareaClick: (Tarea) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentTarea: Tarea? = null

        init {
            // Registramos el click en el CardView completo
            binding.cardTarea.setOnClickListener {
                currentTarea?.let { tarea -> onTareaClick(tarea) }
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
        return TareaViewHolder(binding, onTareaClick)
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
