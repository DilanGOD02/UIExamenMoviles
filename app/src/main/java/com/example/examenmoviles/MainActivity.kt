package com.example.examenmoviles

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.examenmoviles.pages.CoursePage
import com.example.examenmoviles.pages.StudentPage
import com.example.examenmoviles.ui.theme.ExamenMovilesTheme
import com.example.examenmoviles.viewmodel.CourseViewModel
import androidx.activity.viewModels
import com.example.examenmoviles.models.StudentAppDatabase


class MainActivity : ComponentActivity() {
    // Obtener el ViewModel usando la factory por defecto de AndroidViewModel
    private val viewModel: CourseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Room Database
        StudentAppDatabase.getInstance(this)

        enableEdgeToEdge()
        setContent {
            ExamenMovilesTheme {
                val context = LocalContext.current
                var showNoInternetAlert by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("main") } // Control de navegación

                // Verificar conexión al iniciar
                LaunchedEffect(Unit) {
                    val isOnline = checkInternetConnection(context)
                    showNoInternetAlert = !isOnline
                    if (currentScreen == "main") {
                        viewModel.fetchEvents() // Cargar cursos al inicio
                    }
                }

                // Navegación principal
                when (currentScreen) {
                    "main" -> MainScreen(
                        onNavigateToStudents = {
                            startActivity(Intent(this@MainActivity, StudentPage::class.java))
                        },
                        onNavigateToCourses = {
                            currentScreen = "courses"
                        }
                    )
                    "courses" -> CoursePage(
                        onBack = { currentScreen = "main" },
                        courseViewModel = viewModel
                    )
                }

                // Alerta de conexión
                if (showNoInternetAlert) {
                    AlertDialog(
                        onDismissRequest = { showNoInternetAlert = false },
                        title = { Text("Sin conexión a Internet") },
                        text = { Text("Estás en modo offline. Los datos mostrados son los últimos guardados localmente.") },
                        confirmButton = {
                            Button(onClick = {
                                showNoInternetAlert = false
                                // Intenta reconectar
                                val isOnline = checkInternetConnection(context)
                                if (isOnline) {
                                    viewModel.fetchEvents()
                                }
                            }) {
                                Text("Entendido")
                            }
                        }
                    )
                }

                // Mostrar mensajes de error/éxito del ViewModel
                viewModel.errorMessage.collectAsState().value?.let { message ->
                    AlertDialog(
                        onDismissRequest = { viewModel.clearMessages() },
                        title = { Text("Error") },
                        text = { Text(message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.clearMessages() }) {
                                Text("OK")
                            }
                        }
                    )
                }

                viewModel.successMessage.collectAsState().value?.let { message ->
                    AlertDialog(
                        onDismissRequest = { viewModel.clearMessages() },
                        title = { Text("Éxito") },
                        text = { Text(message) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.clearMessages() }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkInternetConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToStudents: () -> Unit,
    onNavigateToCourses: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Examen Moviles") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Welcome to the Examen Moviles App!",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = onNavigateToStudents,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Go to Students Page", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowForward, contentDescription = "Arrow to Students")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = onNavigateToCourses,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Go to Courses Page", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowForward, contentDescription = "Arrow to Courses")
            }
        }
    }
}