package com.example.organizadordetareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.organizadordetareas.adapter.TareaAdapter
import com.example.organizadordetareas.databinding.FragmentFirstBinding
import com.example.organizadordetareas.viewmodel.TareaViewModel

/**
 * Fragment principal que muestra la lista de tareas en un RecyclerView.
 *
 * Observa el [TareaViewModel] compartido para reaccionar a cambios en la lista
 * (por ejemplo, cuando se agrega una tarea desde el formulario).
 */
class FirstFragment : Fragment() {

    // ──────────────────────────────────────────────────────────────
    // ViewBinding — pattern recomendado para fragments
    // ──────────────────────────────────────────────────────────────

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // ──────────────────────────────────────────────────────────────
    // ViewModel compartido con SecondFragment (formulario)
    // ──────────────────────────────────────────────────────────────

    private val viewModel: TareaViewModel by activityViewModels()

    // ──────────────────────────────────────────────────────────────
    // Adapter del RecyclerView
    // ──────────────────────────────────────────────────────────────

    private lateinit var tareaAdapter: TareaAdapter

    // ──────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────

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
        observarTareas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Evitar memory leaks — liberar la referencia al binding
        _binding = null
    }

    // ──────────────────────────────────────────────────────────────
    // Setup del RecyclerView
    // ──────────────────────────────────────────────────────────────

    /**
     * Inicializa el RecyclerView con su LayoutManager y Adapter.
     * El click en cada tarjeta muestra un Toast con el título de la tarea.
     */
    private fun configurarRecyclerView() {
        tareaAdapter = TareaAdapter { tarea ->
            Toast.makeText(
                requireContext(),
                tarea.titulo,
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.recyclerTareas.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tareaAdapter
            setHasFixedSize(false)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Observer de datos
    // ──────────────────────────────────────────────────────────────

    /**
     * Observa los cambios en la lista de tareas del ViewModel.
     * Actualiza el RecyclerView y alterna la visibilidad del estado vacío.
     */
    private fun observarTareas() {
        viewModel.tareas.observe(viewLifecycleOwner) { listaTareas ->
            // Enviar nueva lista al adapter (DiffUtil calcula los cambios)
            tareaAdapter.submitList(listaTareas.toList())

            // Alternar visibilidad: lista vs. estado vacío
            val hayTareas = listaTareas.isNotEmpty()
            binding.recyclerTareas.visibility = if (hayTareas) View.VISIBLE else View.GONE
            binding.textEmpty.visibility = if (hayTareas) View.GONE else View.VISIBLE
        }
    }
}