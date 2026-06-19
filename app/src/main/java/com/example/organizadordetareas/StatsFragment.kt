package com.example.organizadordetareas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.organizadordetareas.databinding.FragmentStatsBinding
import com.example.organizadordetareas.viewmodel.TareaViewModel
import java.util.Locale

/**
 * Fragmento que muestra el Dashboard de Estadísticas de productividad del usuario,
 * incluyendo tareas totales, tasa de completado, categorización y listado de tareas GPS.
 */
class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TareaViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observarEstadisticas()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Observa la lista de tareas del ViewModel para recalcular las estadísticas reactivamente.
     */
    private fun observarEstadisticas() {
        viewModel.tareas.observe(viewLifecycleOwner) { listaTareas ->
            val total = listaTareas.size
            val completadas = listaTareas.count { it.esCompletada }
            val pendientes = total - completadas
            val urgentes = listaTareas.count { it.esUrgente }

            // 1. Tasa de Finalización
            val tasa = if (total > 0) (completadas * 100) / total else 0
            binding.textCompletionRate.text = String.format(Locale.getDefault(), "Tasa de Finalización: %d%%", tasa)
            binding.progressCompletion.progress = tasa
            binding.textCompletionDetail.text = String.format(Locale.getDefault(), "%d de %d tareas completadas", completadas, total)

            // 2. Métricas Secundarias
            binding.textCountUrgent.text = urgentes.toString()
            binding.textCountPending.text = pendientes.toString()

            // 3. Distribución por Categorías
            val trabajo = listaTareas.count { it.categoria.equals("Trabajo", true) }
            val personal = listaTareas.count { it.categoria.equals("Personal", true) }
            val estudio = listaTareas.count { it.categoria.equals("Estudio", true) }

            // Configurar máximos y valores en las barras de progreso
            binding.progressTrabajo.max = total
            binding.progressTrabajo.progress = trabajo
            binding.textLabelTrabajo.text = String.format(Locale.getDefault(), "Trabajo (%d)", trabajo)

            binding.progressPersonal.max = total
            binding.progressPersonal.progress = personal
            binding.textLabelPersonal.text = String.format(Locale.getDefault(), "Personal (%d)", personal)

            binding.progressEstudio.max = total
            binding.progressEstudio.progress = estudio
            binding.textLabelEstudio.text = String.format(Locale.getDefault(), "Estudio (%d)", estudio)

            // 4. Lista de Tareas Geolocalizadas
            actualizarTareasGps(listaTareas)
        }
    }

    /**
     * Filtra las tareas que contienen datos de GPS y las añade dinámicamente al contenedor visual.
     */
    private fun actualizarTareasGps(listaTareas: List<com.example.organizadordetareas.model.Tarea>) {
        val gpsTareas = listaTareas.filter { it.latitud != null && it.longitud != null }
        binding.layoutLocalizedTasks.removeAllViews()

        if (gpsTareas.isEmpty()) {
            // Re-añadimos el TextView informativo por defecto
            val tvNoGps = TextView(requireContext()).apply {
                id = R.id.text_no_gps_tasks
                text = "No hay tareas con coordenadas GPS registradas."
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setPadding(0, 24, 0, 24)
            }
            binding.layoutLocalizedTasks.addView(tvNoGps)
        } else {
            val inflater = LayoutInflater.from(requireContext())
            for (tarea in gpsTareas) {
                // Inflamos el diseño de item_tarea_gps.xml
                val itemView = inflater.inflate(R.layout.item_tarea_gps, binding.layoutLocalizedTasks, false)
                
                val textGpsTitulo = itemView.findViewById<TextView>(R.id.text_gps_titulo)
                val textGpsCoordenadas = itemView.findViewById<TextView>(R.id.text_gps_coordenadas)

                textGpsTitulo.text = String.format("%s (%s)", tarea.titulo, tarea.categoria.uppercase())
                textGpsCoordenadas.text = String.format(
                    Locale.getDefault(),
                    "📍 Lat: %.6f, Long: %.6f",
                    tarea.latitud,
                    tarea.longitud
                )

                itemView.setOnClickListener {
                    it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                }

                binding.layoutLocalizedTasks.addView(itemView)
            }
        }
    }
}
