package com.example.organizadordetareas

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.organizadordetareas.databinding.ActivityMainBinding
import com.example.organizadordetareas.service.MusicService

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Añadimos el DrawerLayout a la configuración de la AppBar para el menú hamburguesa
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.FirstFragment, R.id.SecondFragment),
            binding.drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        binding.bottomNavigation.setupWithNavController(navController)
        binding.navView.setupWithNavController(navController)

        // Configurar acciones manuales del Navigation Drawer (Servicio de Música)
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_music_play -> {
                    // Iniciar el servicio explícitamente
                    val intent = Intent(this, MusicService::class.java)
                    startService(intent)
                }
                R.id.nav_music_stop -> {
                    // Interrumpir/detener el servicio explícitamente
                    val intent = Intent(this, MusicService::class.java)
                    stopService(intent)
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Inflado de menú superior (Opciones)
    // ──────────────────────────────────────────────────────────────
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflamos el archivo top_menu.xml
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    // ──────────────────────────────────────────────────────────────
    // Intercepción de clics en el menú superior
    // ──────────────────────────────────────────────────────────────
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Respuesta visual corta con Toast
                Toast.makeText(this, "Abriendo Configuración...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_about -> {
                Toast.makeText(this, "Acerca de Organizador de Tareas", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}