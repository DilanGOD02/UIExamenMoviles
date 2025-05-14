package com.example.examenmoviles.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.examenmoviles.models.Student
import com.example.examenmoviles.viewmodel.StudentViewModel

class StudentPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val courseId = intent.getIntExtra("COURSE_ID", -1)
        setContent {
            StudentPageContent(courseId = courseId, onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentPageContent(courseId: Int, onBack: () -> Unit) {
    val viewModel: StudentViewModel = viewModel()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentStudent by remember { mutableStateOf<Student?>(null) }

    // Cargar estudiantes al iniciar
    LaunchedEffect(courseId) {
        viewModel.fetchStudentsByCourseId(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List Students Available") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mostrar mensajes de error/éxito
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
                    title = { Text("Success") },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK")
                        }
                    }
                )
            }

            // Lista de estudiantes
            StudentList(
                students = viewModel.students.collectAsState().value,
                onEdit = { student ->
                    currentStudent = student
                    showEditDialog = true
                },
                onDelete = { studentId ->
                    viewModel.deleteStudent(studentId)
                },
                isLoading = viewModel.isLoading.collectAsState().value
            )

            // Diálogo para agregar estudiante
            if (showAddDialog) {
                StudentFormDialog(
                    title = "Add Student",
                    courseId = courseId,
                    onDismiss = { showAddDialog = false },
                    onSave = { student ->
                        viewModel.createStudent(student)
                        showAddDialog = false
                    }
                )
            }

            // Diálogo para editar estudiante
            currentStudent?.let { student ->
                if (showEditDialog) {
                    StudentFormDialog(
                        title = "Edit Student",
                        student = student,
                        courseId = courseId,
                        onDismiss = { showEditDialog = false },
                        onSave = { updatedStudent ->
                            updatedStudent.id?.let { id ->
                                viewModel.updateStudent(id, updatedStudent)
                            }
                            showEditDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentList(
    students: List<Student>,
    onEdit: (Student) -> Unit,
    onDelete: (Int) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (students.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay estudiantes inscritos en este curso")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(students) { student ->
                StudentListItem(
                    student = student,
                    onEdit = { onEdit(student) },
                    onDelete = { student.id?.let { onDelete(it) } }
                )
            }
        }
    }
}

@Composable
fun StudentListItem(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFormDialog(
    title: String,
    courseId: Int,
    student: Student? = null,
    onDismiss: () -> Unit,
    onSave: (Student) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var phone by remember { mutableStateOf(student?.phone ?: "") }
    var showEmptyFieldsError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showEmptyFieldsError) {
                    Text(
                        text = "Los campos están vacíos",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        showEmptyFieldsError = false // Resetear el error al empezar a escribir
                    },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        showEmptyFieldsError = false
                    },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        showEmptyFieldsError = false
                    },
                    label = { Text("Phone") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank() || email.isBlank() || phone.isBlank()) {
                        showEmptyFieldsError = true
                    } else {
                        val newStudent = Student(
                            id = student?.id,
                            name = name,
                            email = email,
                            phone = phone,
                            courseId = courseId
                        )
                        onSave(newStudent)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}