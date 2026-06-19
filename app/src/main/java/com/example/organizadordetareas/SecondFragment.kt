package com.example.organizadordetareas

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.organizadordetareas.databinding.FragmentFormularioBinding
import com.example.organizadordetareas.viewmodel.TareaViewModel

class SecondFragment : Fragment() {

    private var _binding: FragmentFormularioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TareaViewModel by activityViewModels()

    // Manejador de GPS
    private var locationManager: LocationManager? = null

    // Launcher para pedir permisos de tiempo de ejecución
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("LocationDebug", "Permiso concedido por el usuario")
            obtenerUbicacion()
        } else {
            Log.d("LocationDebug", "Permiso denegado")
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormularioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        configurarScrollDescripcion()
        configurarBotones()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

    private fun configurarBotones() {
        binding.btnGuardar.setOnClickListener {
            it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            guardarTarea()
        }

        binding.btnCompartir.setOnClickListener {
            it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            compartirPorWhatsApp()
        }

        binding.btnObtenerUbicacion.setOnClickListener {
            it.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            solicitarPermisoUbicacion()
        }
    }

    private fun solicitarPermisoUbicacion() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("LocationDebug", "Permiso ya estaba concedido")
                // Ya tenemos permiso, obtenemos la ubicación
                obtenerUbicacion()
            }
            else -> {
                Log.d("LocationDebug", "Solicitando permiso al usuario")
                // Pedimos el permiso en tiempo de ejecución
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission") // Ya verificamos el permiso antes de llamar a esto
    private fun obtenerUbicacion() {
        binding.textUbicacion.text = "Obteniendo ubicación..."
        Toast.makeText(requireContext(), "Buscando GPS...", Toast.LENGTH_SHORT).show()

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d("LocationDebug", "Ubicación obtenida: ${location.latitude}, ${location.longitude}")
                val coord = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                binding.textUbicacion.text = coord
                
                // Detenemos las actualizaciones para ahorrar batería
                locationManager?.removeUpdates(this)
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        try {
            // Requerimos actualizaciones del GPS o Red
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } catch (e: Exception) {
            Log.e("LocationDebug", "Error al obtener ubicación", e)
            binding.textUbicacion.text = "Error al obtener GPS"
        }
    }

    private fun guardarTarea() {
        val nombre = binding.editNombre.text?.toString()?.trim().orEmpty()
        val descripcion = binding.editDescripcion.text?.toString()?.trim().orEmpty()
        val esUrgente = binding.checkUrgente.isChecked
        val categoria = obtenerCategoriaSeleccionada()

        if (nombre.isEmpty()) {
            binding.inputLayoutNombre.error = getString(R.string.error_nombre_vacio)
            binding.editNombre.requestFocus()
            return
        }

        binding.inputLayoutNombre.error = null

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

        findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
    }

    private fun compartirPorWhatsApp() {
        val nombre = binding.editNombre.text?.toString()?.trim().orEmpty()

        if (nombre.isEmpty()) {
            binding.inputLayoutNombre.error = getString(R.string.error_nombre_compartir)
            binding.editNombre.requestFocus()
            return
        }

        binding.inputLayoutNombre.error = null

        val textoCompartir = "📋 Tarea: $nombre"

        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoCompartir)
            setPackage("com.whatsapp")
        }

        try {
            startActivity(whatsappIntent)
        } catch (e: ActivityNotFoundException) {
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

    private fun obtenerCategoriaSeleccionada(): String {
        return when (binding.radioGroupCategoria.checkedRadioButtonId) {
            R.id.radio_trabajo -> getString(R.string.categoria_trabajo)
            R.id.radio_personal -> getString(R.string.categoria_personal)
            R.id.radio_estudio -> getString(R.string.categoria_estudio)
            else -> getString(R.string.categoria_trabajo)
        }
    }
}