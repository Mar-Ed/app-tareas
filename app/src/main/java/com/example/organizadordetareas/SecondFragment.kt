package com.example.organizadordetareas

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Fragment del formulario para crear una nueva tarea.
 *
 * Contiene campos para nombre, descripción, urgencia y categoría.
 * Permite guardar la tarea (vía [TareaViewModel]), capturar ubicación GPS y mostrar un mapa de Google.
 */
class SecondFragment : Fragment(), OnMapReadyCallback {

    // ──────────────────────────────────────────────────────────────
    // ViewBinding y Google Maps
    // ──────────────────────────────────────────────────────────────

    private var _binding: FragmentFormularioBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null

    // ──────────────────────────────────────────────────────────────
    // ViewModel compartido con FirstFragment (lista)
    // ──────────────────────────────────────────────────────────────

    private val viewModel: TareaViewModel by activityViewModels()

    // ──────────────────────────────────────────────────────────────
    // Estado de ubicación GPS capturada
    // ──────────────────────────────────────────────────────────────
    private var latitudCapturada: Double? = null
    private var longitudCapturada: Double? = null

    // SoundPool para efectos de sonidos rápidos en teclas/botones
    private var soundPool: android.media.SoundPool? = null
    private var clickSoundId: Int = 0

    // Launcher para solicitar múltiples permisos de ubicación en tiempo de ejecución
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

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

        // Inicializar el MapView
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        configurarSonidos()
        configurarScrollDescripcion()
        configurarBotones()
        configurarTecladoSonido()
    }

    // ──────────────────────────────────────────────────────────────
    // Implementación de Google Maps
    // ──────────────────────────────────────────────────────────────

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Coordenadas de Lima, Perú
        val lima = LatLng(-12.046374, -77.042793)
        
        // Posicionar cámara en Lima con un zoom adecuado
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12f))
        
        // Permitir que el usuario seleccione una ubicación tocando el mapa
        googleMap?.setOnMapClickListener { latLng ->
            actualizarCoordenadasDesdeMapa(latLng)
        }
        
        // Habilitar capa de ubicación si hay permisos
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap?.isMyLocationEnabled = true
        }
    }

    private fun actualizarCoordenadasDesdeMapa(latLng: LatLng) {
        latitudCapturada = latLng.latitude
        longitudCapturada = latLng.longitude
        
        // Limpiar marcadores previos y poner uno nuevo
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(latLng).title("Ubicación de la tarea"))
        
        // Reproducir sonido al seleccionar en el mapa
        playClickSound()

        binding.textUbicacion.text = String.format(
            java.util.Locale.getDefault(),
            "Latitud: %.6f\nLongitud: %.6f",
            latitudCapturada,
            longitudCapturada
        )
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
            playClickSound()
            guardarTarea()
        }

        binding.btnCompartir.setOnClickListener {
            playClickSound()
            compartirPorWhatsApp()
        }

        binding.btnObtenerUbicacion.setOnClickListener {
            playClickSound()
            verificarPermisosYCapturarUbicacion()
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
            esUrgente = esUrgente,
            latitud = latitudCapturada,
            longitud = longitudCapturada
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

    // ──────────────────────────────────────────────────────────────
    // Gestión de GPS y ubicación
    // ──────────────────────────────────────────────────────────────

    private fun verificarPermisosYCapturarUbicacion() {
        val finePerm = Manifest.permission.ACCESS_FINE_LOCATION
        val coarsePerm = Manifest.permission.ACCESS_COARSE_LOCATION

        val hasFine = ContextCompat.checkSelfPermission(requireContext(), finePerm) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(requireContext(), coarsePerm) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            obtenerUbicacionActual()
        } else {
            requestPermissionLauncher.launch(arrayOf(finePerm, coarsePerm))
        }
    }

    private fun obtenerUbicacionActual() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val gpsHabilitado = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val redHabilitada = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!gpsHabilitado && !redHabilitada) {
            Toast.makeText(requireContext(), "GPS y red desactivados.", Toast.LENGTH_LONG).show()
            return
        }

        val proveedor = if (gpsHabilitado) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
        Toast.makeText(requireContext(), "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()

        val ultimaUbicacion = locationManager.getLastKnownLocation(proveedor)
        if (ultimaUbicacion != null) {
            actualizarCoordenadas(ultimaUbicacion)
            moverMapaAUbicacion(ultimaUbicacion.latitude, ultimaUbicacion.longitude)
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                actualizarCoordenadas(location)
                moverMapaAUbicacion(location.latitude, location.longitude)
                locationManager.removeUpdates(this)
            }
            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
            override fun onProviderEnabled(p0: String) {}
            override fun onProviderDisabled(p0: String) {}
        }

        try {
            locationManager.requestSingleUpdate(proveedor, locationListener, null)
        } catch (e: Exception) {
            try {
                locationManager.requestLocationUpdates(proveedor, 0L, 0f, locationListener)
            } catch (ex: Exception) {}
        }
    }

    private fun actualizarCoordenadas(location: Location) {
        latitudCapturada = location.latitude
        longitudCapturada = location.longitude
        binding.textUbicacion.text = String.format(
            java.util.Locale.getDefault(),
            "Latitud: %.6f\nLongitud: %.6f",
            latitudCapturada,
            longitudCapturada
        )
    }

    private fun moverMapaAUbicacion(lat: Double, lng: Double) {
        val pos = LatLng(lat, lng)
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(pos).title("Tu ubicación actual"))
        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))
    }

    // ──────────────────────────────────────────────────────────────
    // Gestión del ciclo de vida del MapView y Sonidos
    // ──────────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
        liberarSonidos()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    private fun configurarSonidos() {
        try {
            val audioAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ASSISTANT)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = android.media.SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()

            clickSoundId = soundPool?.load(requireContext(), R.raw.click, 1) ?: 0
        } catch (e: Exception) {}
    }

    private fun playClickSound() {
        if (clickSoundId != 0) {
            soundPool?.play(clickSoundId, 0.4f, 0.4f, 1, 0, 1.0f)
        }
    }

    private fun liberarSonidos() {
        soundPool?.release()
        soundPool = null
    }

    private fun configurarTecladoSonido() {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                playClickSound()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        binding.editNombre.addTextChangedListener(textWatcher)
        binding.editDescripcion.addTextChangedListener(textWatcher)
    }
}