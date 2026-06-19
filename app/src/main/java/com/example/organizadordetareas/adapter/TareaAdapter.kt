package com.example.organizadordetareas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadordetareas.R
import com.example.organizadordetareas.databinding.ItemTareaBinding
import com.example.organizadordetareas.model.Tarea

/**
 * Adaptador para el RecyclerView que muestra la lista de Tareas.
 * Usa [ListAdapter] y [DiffUtil] para animaciones eficientes.
 */
class TareaAdapter(
    private val onTareaClick: (Tarea) -> Unit,
    private val onDeleteClick: (Tarea) -> Unit
) : ListAdapter<Tarea, TareaAdapter.TareaViewHolder>(TareaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val binding = ItemTareaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TareaViewHolder(private val binding: ItemTareaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tarea: Tarea) {
            binding.textTitulo.text = tarea.titulo
            binding.textDescripcion.text = tarea.descripcion
            binding.textCategoria.text = "Categoría: ${tarea.categoria}"
            binding.checkCompletada.isChecked = tarea.esCompletada

            // Evento Click normal en la tarjeta
            binding.root.setOnClickListener {
                it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onTareaClick(tarea)
            }

            // Click en el Checkbox
            binding.checkCompletada.setOnClickListener {
                it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onTareaClick(tarea)
            }

            // Click en el botón eliminar
            binding.btnEliminar.setOnClickListener {
                it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                onDeleteClick(tarea)
            }

            // Evento Long Click -> Menú Contextual / Popup
            binding.root.setOnLongClickListener { view ->
                view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                val popup = PopupMenu(view.context, view)
                // Creando menú emergente dinámicamente
                popup.menu.add(0, 1, 0, "Borrar Tarea")
                popup.menu.add(0, 2, 0, "Marcar como Completada")
                
                popup.setOnMenuItemClickListener { menuItem ->
                    view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    when (menuItem.itemId) {
                        1 -> {
                            onDeleteClick(tarea)
                            true
                        }
                        2 -> {
                            onTareaClick(tarea)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
                true
            }
        }
    }

    class TareaDiffCallback : DiffUtil.ItemCallback<Tarea>() {
        override fun areItemsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Tarea, newItem: Tarea): Boolean {
            return oldItem == newItem
        }
    }
}
