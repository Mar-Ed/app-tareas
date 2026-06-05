package com.example.organizadordetareas.network

import com.example.organizadordetareas.model.Tarea
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Interfaz de Retrofit que define los endpoints de la REST API.
 *
 * Cada método mapea a un endpoint del backend Node.js:
 *   - GET  /tareas  → [obtenerTareas]
 *   - POST /tareas  → [crearTarea]
 *
 * Retrofit genera la implementación automáticamente en tiempo de ejecución.
 *
 * Nota: Usamos [Call] para manejo asíncrono con callbacks (enqueue).
 * En un proyecto más avanzado, se pueden usar funciones `suspend` con Coroutines.
 */
interface TareaApi {

    /**
     * Obtiene la lista completa de tareas del servidor.
     *
     * Respuesta esperada del backend:
     * ```json
     * {
     *   "success": true,
     *   "count": 3,
     *   "data": [ { "id": 1, "titulo": "...", ... }, ... ]
     * }
     * ```
     *
     * @return [Call] que envuelve la respuesta con la lista de tareas.
     */
    @GET("tareas")
    fun obtenerTareas(): Call<ApiResponse<List<Tarea>>>

    /**
     * Envía una nueva tarea al servidor para guardarla.
     *
     * @param tarea Objeto [Tarea] serializado como JSON en el body del request.
     * @return [Call] que envuelve la respuesta con la tarea creada.
     */
    @POST("tareas")
    fun crearTarea(@Body tarea: Tarea): Call<ApiResponse<Tarea>>
}
