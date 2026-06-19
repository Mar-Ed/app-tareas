package com.example.organizadordetareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.organizadordetareas.adapter.TareaAdapter
import com.example.organizadordetareas.databinding.FragmentFirstBinding
import com.example.organizadordetareas.viewmodel.TareaViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment principal que muestra la lista de tareas en un RecyclerView.
 *
 * Observa el [TareaViewModel] compartido para reaccionar a cambios en la lista
 * e implementa filtros por chips y gestos de eliminación por deslizamiento.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TareaViewModel by activityViewModels()
    private lateinit var tareaAdapter: TareaAdapter

    private var filtroActual = "Todas"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView()
        configurarFiltros()
        observarTareas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Inicializa el RecyclerView, su adaptador y el ItemTouchHelper para borrar por deslizamiento.
     */
    private fun configurarRecyclerView() {
        tareaAdapter = TareaAdapter(
            onTareaClick = { tarea ->
                // Al hacer clic, alternamos el estado de completado en el ViewModel
                viewModel.toggleTareaCompletada(tarea.id)
            },
            onDeleteClick = { tarea ->
                // Al elegir borrar desde el menú contextual o botón
                val position = tareaAdapter.currentList.indexOf(tarea)
                viewModel.eliminarTarea(tarea.id)
                Snackbar.make(binding.root, "Tarea eliminada: ${tarea.titulo}", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") {
                        viewModel.restaurarTarea(tarea, position)
                    }
                    .show()
            }
        )

        binding.recyclerTareas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tareaAdapter
            setHasFixedSize(false)
        }

        // Swipe-to-Delete (deslizar a la izquierda o derecha para borrar)
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No soportamos drag-and-drop
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val list = tareaAdapter.currentList
                if (position in list.indices) {
                    val tarea = list[position]
                    // Eliminar del ViewModel
                    viewModel.eliminarTarea(tarea.id)

                    // Mostrar Snackbar para deshacer la eliminación
                    Snackbar.make(binding.root, "Tarea eliminada: ${tarea.titulo}", Snackbar.LENGTH_LONG)
                        .setAction("Deshacer") {
                            viewModel.restaurarTarea(tarea, position)
                        }
                        .show()
                }
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerTareas)
    }

    /**
     * Configura el listener del ChipGroup para filtrar las tareas dinámicamente.
     */
    private fun configurarFiltros() {
        binding.chipGroupFiltro.setOnCheckedStateChangeListener { group, checkedIds ->
            // Sonido al cambiar de filtro
            group.playSoundEffect(android.view.SoundEffectConstants.CLICK)

            val checkedId = checkedIds.firstOrNull()
            filtroActual = when (checkedId) {
                R.id.chip_trabajo -> "Trabajo"
                R.id.chip_personal -> "Personal"
                R.id.chip_estudio -> "Estudio"
                R.id.chip_urgentes -> "Urgentes"
                R.id.chip_completadas -> "Completadas"
                else -> "Todas"
            }
            actualizarListaFiltrada()
        }
    }

    /**
     * Observa los cambios del LiveData del ViewModel.
     */
    private fun observarTareas() {
        viewModel.tareas.observe(viewLifecycleOwner) {
            actualizarListaFiltrada()
        }
    }

    /**
     * Filtra la lista de tareas del ViewModel basándose en el chip seleccionado y actualiza la UI.
     */
    private fun actualizarListaFiltrada() {
        val listaTareas = viewModel.tareas.value ?: emptyList()
        val listaFiltrada = when (filtroActual) {
            "Trabajo" -> listaTareas.filter { it.categoria.equals("Trabajo", true) }
            "Personal" -> listaTareas.filter { it.categoria.equals("Personal", true) }
            "Estudio" -> listaTareas.filter { it.categoria.equals("Estudio", true) }
            "Urgentes" -> listaTareas.filter { it.esUrgente }
            "Completadas" -> listaTareas.filter { it.esCompletada }
            else -> listaTareas
        }

        tareaAdapter.submitList(listaFiltrada)

        val hayTareas = listaFiltrada.isNotEmpty()
        binding.recyclerTareas.visibility = if (hayTareas) View.VISIBLE else View.GONE
        binding.textEmpty.visibility = if (hayTareas) View.GONE else View.VISIBLE

        if (!hayTareas && listaTareas.isNotEmpty()) {
            binding.textEmpty.text = "No hay tareas que coincidan con este filtro"
        } else {
            binding.textEmpty.text = getString(R.string.empty_task_list)
        }
    }
}