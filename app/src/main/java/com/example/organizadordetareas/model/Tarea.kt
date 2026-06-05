package com.example.organizadordetareas.model

/**
 * Representa una Tarea dentro de la aplicación.
 *
 * @property id Identificador único de la tarea.
 * @property titulo Título de la tarea.
 * @property descripcion Detalle o descripción de la tarea.
 * @property categoria Categoría a la que pertenece la tarea (ej. Trabajo, Personal, Estudio).
 */
data class Tarea(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val esUrgente: Boolean = false
)
