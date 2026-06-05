package com.example.organizadordetareas.network

/**
 * Modelo genérico que envuelve las respuestas JSON del backend.
 *
 * El servidor siempre responde con esta estructura:
 * ```json
 * {
 *   "success": true/false,
 *   "message": "texto opcional",
 *   "count": 3,         // solo en GET
 *   "data": { ... }     // el payload real
 * }
 * ```
 *
 * @param T Tipo del payload contenido en [data] (ej: List<Tarea>, Tarea).
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val count: Int? = null,
    val data: T? = null
)
