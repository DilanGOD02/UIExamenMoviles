package com.example.examenmoviles.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.example.examenmoviles.viewmodel.StudentDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailsPage(
    viewModel: StudentDetailsViewModel,
    onBack: () -> Unit
) {
    val students by viewModel.students.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchStudentsDetails()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles de Estudiantes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (students.isEmpty()) {
          
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay estudiantes registrados.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxSize()
            ) {
                items(students) { student ->
                    var expanded by remember { mutableStateOf(false) }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { expanded = !expanded },
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (expanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = "📧 Email: ${student.email}")
                                Text(text = "📞 Teléfono: ${student.phone}")
                                Text(text = "📚 Curso: ${student.courseName}")
                                Text(text = "📝 Descripción: ${student.courseDescription}")
                                Text(text = "🕒 Horario: ${student.courseSchedule}")
                                Text(text = "👨‍🏫 Profesor: ${student.courseProfessor}")
                            }
                        }
                    }
                }
            }
        }
    }
}
