package com.example.organizadordetareas

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.organizadordetareas.databinding.FragmentFormularioBinding
import com.example.organizadordetareas.viewmodel.TareaViewModel

/**
 * Fragment del formulario para crear una nueva tarea.
 *
 * Contiene campos para nombre, descripción, urgencia y categoría.
 * Permite guardar la tarea (vía [TareaViewModel]) y compartir el título por WhatsApp.
 */
class SecondFragment : Fragment() {

    // ──────────────────────────────────────────────────────────────
    // ViewBinding
    // ──────────────────────────────────────────────────────────────

    private var _binding: FragmentFormularioBinding? = null
    private val binding get() = _binding!!

    // ──────────────────────────────────────────────────────────────
    // ViewModel compartido con FirstFragment (lista)
    // ──────────────────────────────────────────────────────────────

    private val viewModel: TareaViewModel by activityViewModels()

    // ──────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormularioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarScrollDescripcion()
        configurarBotones()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ──────────────────────────────────────────────────────────────
    // Configuración de scroll en el campo de descripción
    // ──────────────────────────────────────────────────────────────

    /**
     * Permite scroll vertical independiente dentro del EditText de descripción,
     * sin que el ScrollView padre intercepte los toques.
     */
    private fun configurarScrollDescripcion() {
        binding.editDescripcion.setOnTouchListener { view, event ->
            if (view.hasFocus()) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                if (event.action == MotionEvent.ACTION_UP) {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Configuración de botones
    // ──────────────────────────────────────────────────────────────

    private fun configurarBotones() {
        binding.btnGuardar.setOnClickListener {
            guardarTarea()
        }

        binding.btnCompartir.setOnClickListener {
            compartirPorWhatsApp()
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Lógica de negocio: Guardar tarea
    // ──────────────────────────────────────────────────────────────

    /**
     * Valida los campos del formulario y, si son correctos,
     * agrega la tarea al [TareaViewModel] y navega de vuelta a la lista.
     */
    private fun guardarTarea() {
        val nombre = binding.editNombre.text?.toString()?.trim().orEmpty()
        val descripcion = binding.editDescripcion.text?.toString()?.trim().orEmpty()
        val esUrgente = binding.checkUrgente.isChecked
        val categoria = obtenerCategoriaSeleccionada()

        // Validación: nombre obligatorio
        if (nombre.isEmpty()) {
            binding.inputLayoutNombre.error = getString(R.string.error_nombre_vacio)
            binding.editNombre.requestFocus()
            return
        }

        // Limpiar error previo
        binding.inputLayoutNombre.error = null

        // Agregar tarea al ViewModel (Single Source of Truth)
        viewModel.agregarTarea(
            titulo = nombre,
            descripcion = descripcion,
            categoria = categoria,
            esUrgente = esUrgente
        )

        Toast.makeText(
            requireContext(),
            getString(R.string.toast_tarea_guardada),
            Toast.LENGTH_SHORT
        ).show()

        // Navegar de vuelta a la lista de tareas
        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    // ──────────────────────────────────────────────────────────────
    // Lógica de negocio: Compartir por WhatsApp
    // ──────────────────────────────────────────────────────────────

    /**
     * Crea un [Intent.ACTION_SEND] dirigido a WhatsApp con el título de la tarea.
     * Si WhatsApp no está instalado, abre el selector de apps del sistema.
     */
    private fun compartirPorWhatsApp() {
        val nombre = binding.editNombre.text?.toString()?.trim().orEmpty()

        // Validación: necesitamos un nombre para compartir
        if (nombre.isEmpty()) {
            binding.inputLayoutNombre.error = getString(R.string.error_nombre_compartir)
            binding.editNombre.requestFocus()
            return
        }

        binding.inputLayoutNombre.error = null

        val textoCompartir = "📋 Tarea: $nombre"

        // Intent específico para WhatsApp
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoCompartir)
            setPackage("com.whatsapp")
        }

        try {
            startActivity(whatsappIntent)
        } catch (e: ActivityNotFoundException) {
            // WhatsApp no está instalado → abrir selector genérico
            Toast.makeText(
                requireContext(),
                getString(R.string.toast_whatsapp_no_instalado),
                Toast.LENGTH_SHORT
            ).show()

            val chooserIntent = Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, textoCompartir)
                },
                getString(R.string.share_chooser_title)
            )
            startActivity(chooserIntent)
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Helper: obtener categoría del RadioGroup
    // ──────────────────────────────────────────────────────────────

    /**
     * Determina la categoría seleccionada en el RadioGroup.
     * Retorna el string correspondiente al RadioButton activo.
     */
    private fun obtenerCategoriaSeleccionada(): String {
        return when (binding.radioGroupCategoria.checkedRadioButtonId) {
            R.id.radio_trabajo -> getString(R.string.categoria_trabajo)
            R.id.radio_personal -> getString(R.string.categoria_personal)
            R.id.radio_estudio -> getString(R.string.categoria_estudio)
            else -> getString(R.string.categoria_trabajo) // Default
        }
    }
}