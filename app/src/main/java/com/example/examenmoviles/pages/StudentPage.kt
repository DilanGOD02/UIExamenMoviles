package com.example.examenmoviles.pages

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.examenmoviles.models.Student
import com.example.examenmoviles.viewmodel.StudentViewModel
import com.google.firebase.messaging.FirebaseMessaging

class StudentPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        subscribeToTopic()
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
    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(isInternetAvailable(context)) }

    // Cargar estudiantes al iniciar
    LaunchedEffect(courseId) {
        viewModel.loadLocalStudents(courseId)
        if (isOnline) {
            viewModel.fetchStudentsByCourseId(courseId)
        }
    }

    // Observar cambios en la conexión
    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        isOnline = isInternetAvailable(context)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                isOnline = true
                viewModel.fetchStudentsByCourseId(courseId)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isOnline = false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } else {
            @Suppress("DEPRECATION")
            val request = NetworkRequest.Builder().build()
            connectivityManager.registerNetworkCallback(request, callback)
        }

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Estudiantes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (isOnline) {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar Estudiante")
                        }
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
            // Lista de estudiantes
            StudentList(
                students = viewModel.students.collectAsState().value,
                onEdit = { student ->
                    if (isOnline) {
                        currentStudent = student
                        showEditDialog = true
                    }
                },
                onDelete = { studentId ->
                    if (isOnline) {
                        viewModel.deleteStudent(studentId)
                    }
                },
                isLoading = viewModel.isLoading.collectAsState().value
            )

            // Diálogo para agregar estudiante
            if (showAddDialog) {
                StudentFormDialog(
                    title = "Agregar Estudiante",
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
                        title = "Editar Estudiante",
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
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
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
                        text = "Todos los campos son obligatorios",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono") },
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
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "student_reminder_channel"
        val channelName = "Notificaciones de Estudiantes"
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Notificaciones sobre estudiantes"
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }
}

fun subscribeToTopic() {
    FirebaseMessaging.getInstance().subscribeToTopic("student_notifications")
        .addOnCompleteListener { task ->
            val msg = if (task.isSuccessful) "Suscripción exitosa" else "Suscripción fallida"
            Log.d("FCM", msg)
        }
}

fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        networkInfo?.isConnected == true
    }
}
