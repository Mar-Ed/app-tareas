package com.example.organizadordetareas.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.organizadordetareas.model.Tarea
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * ViewModel compartido entre FirstFragment (lista) y SecondFragment (formulario).
 *
 * Actúa como Single Source of Truth para la lista de tareas.
 * Hereda de [AndroidViewModel] para acceder a SharedPreferences y persistir las tareas locales.
 */
class TareaViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("tareas_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _tareas = MutableLiveData<List<Tarea>>(emptyList())
    val tareas: LiveData<List<Tarea>> get() = _tareas

    /** Contador autoincremental para generar IDs únicos. */
    private var nextId: Int = 1

    init {
        cargarTareas()
    }

    /**
     * Carga las tareas desde SharedPreferences. Si está vacío, carga tareas de demostración.
     */
    private fun cargarTareas() {
        val json = sharedPreferences.getString("tareas_list", null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Tarea>>() {}.type
                val lista: List<Tarea> = gson.fromJson(json, type)
                _tareas.value = lista
                // Ajustar nextId para evitar colisiones
                nextId = (lista.maxOfOrNull { it.id } ?: 0) + 1
            } catch (e: Exception) {
                cargarTareasDemostracion()
            }
        } else {
            cargarTareasDemostracion()
        }
    }

    private fun cargarTareasDemostracion() {
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
        guardarTareas(tareasIniciales)
    }

    /**
     * Guarda la lista de tareas en SharedPreferences de forma asíncrona.
     */
    private fun guardarTareas(lista: List<Tarea>) {
        sharedPreferences.edit().putString("tareas_list", gson.toJson(lista)).apply()
    }

    /**
     * Agrega una nueva tarea al inicio de la lista y persiste el cambio.
     */
    fun agregarTarea(
        titulo: String,
        descripcion: String,
        categoria: String,
        esUrgente: Boolean,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        val nuevaTarea = Tarea(
            id = nextId++,
            titulo = titulo,
            descripcion = descripcion,
            categoria = categoria,
            esUrgente = esUrgente,
            latitud = latitud,
            longitud = longitud,
            esCompletada = false
        )

        val listaActualizada = listOf(nuevaTarea) + (_tareas.value ?: emptyList())
        _tareas.value = listaActualizada
        guardarTareas(listaActualizada)
    }

    /**
     * Alterna el estado de completado de una tarea por su ID.
     */
    fun toggleTareaCompletada(id: Int) {
        val listaActual = _tareas.value ?: emptyList()
        val listaActualizada = listaActual.map {
            if (it.id == id) it.copy(esCompletada = !it.esCompletada) else it
        }
        _tareas.value = listaActualizada
        guardarTareas(listaActualizada)
    }

    /**
     * Elimina una tarea por su ID y persiste el cambio.
     */
    fun eliminarTarea(id: Int) {
        val listaActual = _tareas.value ?: emptyList()
        val listaActualizada = listaActual.filter { it.id != id }
        _tareas.value = listaActualizada
        guardarTareas(listaActualizada)
    }

    /**
     * Restaura una tarea en una posición específica (para deshacer borrados).
     */
    fun restaurarTarea(tarea: Tarea, posicion: Int) {
        val listaActual = (_tareas.value ?: emptyList()).toMutableList()
        if (posicion >= 0 && posicion <= listaActual.size) {
            listaActual.add(posicion, tarea)
        } else {
            listaActual.add(0, tarea)
        }
        _tareas.value = listaActual
        guardarTareas(listaActual)
    }
}
