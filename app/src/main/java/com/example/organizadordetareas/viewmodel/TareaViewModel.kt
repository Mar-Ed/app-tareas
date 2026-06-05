package com.example.organizadordetareas.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.organizadordetareas.model.Tarea

/**
 * ViewModel compartido entre FirstFragment (lista) y SecondFragment (formulario).
 *
 * Actúa como Single Source of Truth para la lista de tareas.
 * Al usar [activityViewModels], ambos fragments comparten la misma instancia
 * y los datos persisten mientras la Activity esté viva.
 */
class TareaViewModel : ViewModel() {

    // ──────────────────────────────────────────────────────────────
    // Estado observable — lista inmutable expuesta, mutable internamente
    // ──────────────────────────────────────────────────────────────

    private val _tareas = MutableLiveData<List<Tarea>>(emptyList())
    val tareas: LiveData<List<Tarea>> get() = _tareas

    /** Contador autoincremental para generar IDs únicos. */
    private var nextId: Int = 1

    // ──────────────────────────────────────────────────────────────
    // Datos iniciales de demostración
    // ──────────────────────────────────────────────────────────────

    init {
        val tareasIniciales = listOf(
            Tarea(
                id = nextId++,
                titulo = "Reunión de Sincronización",
                descripcion = "Revisar el estado de las tareas de la semana y planificar los próximos sprints con el equipo.",
                categoria = "Trabajo",
                esUrgente = true
            ),
            Tarea(
                id = nextId++,
                titulo = "Estudiar Kotlin Coroutines",
                descripcion = "Completar el módulo 5 del curso de Android avanzado sobre concurrencia.",
                categoria = "Estudio",
                esUrgente = false
            ),
            Tarea(
                id = nextId++,
                titulo = "Comprar víveres",
                descripcion = "Ir al supermercado: frutas, verduras, pan y leche para la semana.",
                categoria = "Personal",
                esUrgente = false
            )
        )
        _tareas.value = tareasIniciales
    }

    // ──────────────────────────────────────────────────────────────
    // Operaciones sobre la lista
    // ──────────────────────────────────────────────────────────────

    /**
     * Agrega una nueva tarea al inicio de la lista.
     *
     * Crea una copia nueva de la lista para que [LiveData] detecte el cambio
     * y notifique a los observers correctamente.
     */
    fun agregarTarea(
        titulo: String,
        descripcion: String,
        categoria: String,
        esUrgente: Boolean
    ) {
        val nuevaTarea = Tarea(
            id = nextId++,
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            esUrgente = esUrgente
        )

        // Creamos una nueva lista (inmutable) para disparar el observer de LiveData
        val listaActualizada = listOf(nuevaTarea) + (_tareas.value ?: emptyList())
        _tareas.value = listaActualizada
    }
}
