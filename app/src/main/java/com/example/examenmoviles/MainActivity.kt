package com.example.examenmoviles

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.examenmoviles.pages.CoursePage
import com.example.examenmoviles.pages.StudentPage
import com.example.examenmoviles.ui.theme.ExamenMovilesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExamenMovilesTheme {
                var currentPage by remember { mutableStateOf("") }

                when (currentPage) {
                    "students" -> {
                    // Navegar a la actividad StudentActivity
                        val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        val intent = Intent(context, StudentPage::class.java)
                        context.startActivity(intent)
                        currentPage = "" // Restablecer página actual
                    }
                }
                    "courses" -> CoursePage(onBack = { currentPage = "" })
                    else -> MainScreen(onNavigate = { selectedPage -> currentPage = selectedPage })
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigate: (String) -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Examen Moviles") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
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
                onClick = { onNavigate("students") },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
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
                onClick = { onNavigate("courses") },
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Go to Courses Page", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowForward, contentDescription = "Arrow to Courses")
            }
        }
    }
}
