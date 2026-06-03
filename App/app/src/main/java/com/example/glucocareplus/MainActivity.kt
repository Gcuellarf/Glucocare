package com.example.glucocareplus

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glucocareplus.ui.theme.GlucoCarePlusTheme
import androidx.compose.ui.graphics.Color
import com.example.glucocareplus.ui.screens.WelcomeScreen
import com.example.glucocareplus.ui.screens.LoginScreen
import com.example.glucocareplus.ui.screens.RegisterScreen
import com.example.glucocareplus.ui.screens.MedicationReminderScreen
import com.example.glucocareplus.ui.screens.GlucemiaScreen
import com.example.glucocareplus.data.local.AppDatabase
import com.example.glucocareplus.ui.screens.CitasScreen
import com.example.glucocareplus.ui.screens.NovedadScreen
import com.example.glucocareplus.ui.screens.AprendizajeScreen
import com.example.glucocareplus.ui.screens.InicioScreen
import com.example.glucocareplus.ui.viewmodels.CitaViewModel
import com.example.glucocareplus.ui.viewmodels.MedicamentoViewModel
import com.example.glucocareplus.ui.viewmodels.GlucemiaViewModel
import com.example.glucocareplus.ui.viewmodels.NovedadViewModel
import com.example.glucocareplus.ui.viewmodels.SyncViewModel
import com.example.glucocareplus.ui.viewmodels.UsuarioViewModel

sealed class OpcionNavegacion(val ruta: String, val titulo: String, val icono: androidx.compose.ui.graphics.vector.ImageVector) {
    object Inicio : OpcionNavegacion("inicio", "Inicio", Icons.Default.Home)
    object Medicamentos : OpcionNavegacion("medicamentos", "Medicamentos", Icons.Default.Edit)
    object Glucemias: OpcionNavegacion("glucemias", "Glucemias", Icons.Default.MonitorHeart)
    object Formulario: OpcionNavegacion("formulario", "Formulario", Icons.Default.Warning)
    object Citas : OpcionNavegacion("citas", "Citas", Icons.Default.DateRange)
    object Aprendizaje : OpcionNavegacion("aprendizaje", "Aprender", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val medicamentoDao = db.medicamentoDao()
        val glucemiaDao = db.glucemiaDao()
        val novedadDao = db.novedadDao()
        val citaDao = db.citaDao()
        val usuarioDao = db.usuarioDao()


        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(MedicamentoViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        MedicamentoViewModel(medicamentoDao) as T
                    }
                    modelClass.isAssignableFrom(GlucemiaViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        GlucemiaViewModel(glucemiaDao) as T
                    }
                    modelClass.isAssignableFrom(NovedadViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        NovedadViewModel(novedadDao) as T
                    }
                    modelClass.isAssignableFrom(CitaViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        CitaViewModel(citaDao) as T
                    }
                    modelClass.isAssignableFrom(UsuarioViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        UsuarioViewModel(usuarioDao) as T
                    }
                    modelClass.isAssignableFrom(SyncViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        SyncViewModel(glucemiaDao, citaDao, medicamentoDao, novedadDao) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        setContent {
            GlucoCarePlusTheme {
                var sesionActiva by remember { mutableStateOf(false) }
                var usernameLogueado by remember { mutableStateOf("") }
                var pantallaAuth by remember { mutableStateOf("welcome") }

                val medicamentoViewModel: MedicamentoViewModel = viewModel(factory = viewModelFactory)
                val glucemiaViewModel: GlucemiaViewModel = viewModel(factory = viewModelFactory)
                val novedadViewModel: NovedadViewModel = viewModel(factory = viewModelFactory)
                val citaViewModel: CitaViewModel = viewModel(factory = viewModelFactory)
                val usuarioViewModel: UsuarioViewModel = viewModel(factory = viewModelFactory)
                val syncViewModel: SyncViewModel = viewModel(factory = viewModelFactory)

                if (!sesionActiva) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        when (pantallaAuth) {
                            "welcome" -> {
                                WelcomeScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onLoginClick = { pantallaAuth = "login" },
                                    onRegisterClick = { pantallaAuth = "register" }
                                )
                            }
                            "login" -> {
                                LoginScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    viewModel = usuarioViewModel,
                                    onLoginSuccess = {
                                        // Extraemos dinámicamente el valor almacenado en el ViewModel al loguearse con éxito
                                        usernameLogueado = usuarioViewModel.usuarioLogueado
                                        sesionActiva = true
                                    },
                                    onForgotPasswordClick = { /* Lógica pass */ },
                                    onRegisterClick = { pantallaAuth = "register" }
                                )
                            }
                            "register" -> {
                                RegisterScreen(
                                    modifier = Modifier.padding(innerPadding),
                                    onRegisterSuccessClick = {
                                        pantallaAuth = "login"
                                    },
                                    onBackToLoginClick = { pantallaAuth = "login" },
                                    viewModel = usuarioViewModel
                                )
                            }
                        }
                    }
                } else {
                    PantallaPrincipalApp(
                        medicamentoViewModel = medicamentoViewModel,
                        glucemiaViewModel = glucemiaViewModel,
                        novedadViewModel = novedadViewModel,
                        citaViewModel = citaViewModel,
                        syncViewModel = syncViewModel,
                        usernameActivo = usernameLogueado
                    )
                }
            }
        }
    }
}

@Composable
fun PantallaPrincipalApp(
    medicamentoViewModel: MedicamentoViewModel,
    glucemiaViewModel: GlucemiaViewModel,
    novedadViewModel: NovedadViewModel,
    citaViewModel: CitaViewModel,
    syncViewModel: SyncViewModel,
    usernameActivo: String
) {
    var seccionActual by remember { mutableStateOf<OpcionNavegacion>(OpcionNavegacion.Inicio) }

    val botonesBarra = listOf(
        OpcionNavegacion.Inicio,
        OpcionNavegacion.Medicamentos,
        OpcionNavegacion.Glucemias,
        OpcionNavegacion.Formulario,
        OpcionNavegacion.Citas,
        OpcionNavegacion.Aprendizaje
    )

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                botonesBarra.forEach { opcion ->
                    NavigationBarItem(
                        icon = { Icon(opcion.icono, contentDescription = opcion.titulo) },
                        selected = seccionActual == opcion,
                        onClick = { seccionActual = opcion },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (seccionActual) {
                OpcionNavegacion.Inicio -> {
                    InicioScreen(
                        usernameActivo = usernameActivo,
                        syncViewModel = syncViewModel,
                        glucemiaViewModel = glucemiaViewModel,
                        citaViewModel = citaViewModel,
                        innerPadding = innerPadding,
                        onNavegarA = { destino ->
                            seccionActual = when (destino) {
                                "glucemias" -> OpcionNavegacion.Glucemias
                                "formulario" -> OpcionNavegacion.Formulario
                                else -> OpcionNavegacion.Inicio
                            }
                        }
                    )
                }
                OpcionNavegacion.Medicamentos -> {
                    MedicationReminderScreen(
                        viewModel = medicamentoViewModel,
                        onNavigateToNovedades = {
                            Toast.makeText(context, "Abriendo formulario ¿Algo anda mal?", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                OpcionNavegacion.Glucemias -> {
                    GlucemiaScreen(
                        viewModel = glucemiaViewModel,
                        innerPadding = innerPadding
                    )
                }
                OpcionNavegacion.Formulario -> {
                    NovedadScreen(
                        viewModel = novedadViewModel,
                        innerPadding = innerPadding,
                        onReporteExitoso = {
                            seccionActual = OpcionNavegacion.Inicio
                        }
                    )
                }
                OpcionNavegacion.Citas -> {
                    CitasScreen(
                        viewModel = citaViewModel,
                        innerPadding = innerPadding
                    )
                }
                OpcionNavegacion.Aprendizaje -> {
                    AprendizajeScreen(
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }
}