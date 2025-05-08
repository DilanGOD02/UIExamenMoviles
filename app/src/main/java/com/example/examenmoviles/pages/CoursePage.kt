package com.example.examenmoviles.pages

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.examenmoviles.models.Course
import com.example.examenmoviles.viewmodel.CourseViewModel
import com.moviles.taskmind.common.Constants
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursePage(
    onBack: () -> Unit,
    courseViewModel: CourseViewModel = viewModel()
) {
    val courses by courseViewModel.course.collectAsState()
    var showForm by remember { mutableStateOf(false) }
    var currentCourseToEdit by remember { mutableStateOf<Course?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        courseViewModel.fetchEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cursos Disponibles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showForm = true
                currentCourseToEdit = null
            }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add course")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (showForm) {
                AddCourseForm(
                    courseToEdit = currentCourseToEdit,
                    onSubmit = { course, imageUri ->
                        if (imageUri != null) {
                            val imageFile = uriToFile(imageUri, context)
                            if (currentCourseToEdit == null) {
                                // Add new course
                                courseViewModel.addCourse(course, imageFile)
                            } else {
                                // Update existing course
                                courseViewModel.updateCourse(
                                    currentCourseToEdit!!.id ?: 0,
                                    course,
                                    imageFile
                                )
                            }
                        } else if (currentCourseToEdit != null && currentCourseToEdit?.imageUrl != null) {
                            // Editing but no new image selected - keep existing image
                            courseViewModel.updateCourse(
                                currentCourseToEdit!!.id ?: 0,
                                course,
                                null
                            )
                        } else {
                            Toast.makeText(context, "Se requiere una imagen", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDismiss = {
                        showForm = false
                        currentCourseToEdit = null
                    }
                )
            } else {
                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay cursos disponibles", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(courses) { course ->
                            CourseCard(
                                course = course,
                                onEditClick = {
                                    currentCourseToEdit = course
                                    showForm = true
                                },
                                onDeleteClick = {
                                    courseViewModel.deleteCourse(course.id ?: 0)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val fullImageUrl = Constants.IMAGES_BASE_URL + (course.imageUrl ?: "")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Imagen del curso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(fullImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen del curso ${course.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x66000000))
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }

            // Información del curso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar curso",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar curso",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Profesor: ${course.professor}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Horario: ${course.schedule}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseForm(
    courseToEdit: Course? = null,
    onSubmit: (Course, Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(courseToEdit?.name ?: "") }
    var description by remember { mutableStateOf(courseToEdit?.description ?: "") }
    var professor by remember { mutableStateOf(courseToEdit?.professor ?: "") }
    var schedule by remember { mutableStateOf(courseToEdit?.schedule ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> imageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (courseToEdit == null) "Agregar Curso" else "Editar Curso",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del curso*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = professor,
                    onValueChange = { professor = it },
                    label = { Text("Profesor*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = professor.isBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Horario*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = schedule.isBlank()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text("Seleccionar imagen${if (courseToEdit == null) "*" else ""}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Vista previa de la imagen",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (courseToEdit?.imageUrl != null) {
                    AsyncImage(
                        model = "${Constants.IMAGES_BASE_URL}${courseToEdit.imageUrl}",
                        contentDescription = "Imagen actual del curso",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "Imagen actual (selecciona una nueva para cambiarla)",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay imagen seleccionada",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (name.isBlank() || description.isBlank() ||
                                professor.isBlank() || schedule.isBlank() ||
                                (courseToEdit == null && imageUri == null)
                            ) {
                                Toast.makeText(
                                    context,
                                    "Por favor complete todos los campos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onSubmit(
                                    Course(
                                        id = courseToEdit?.id,
                                        name = name,
                                        description = description,
                                        professor = professor,
                                        schedule = schedule,
                                        imageUrl = courseToEdit?.imageUrl
                                    ),
                                    imageUri
                                )
                            }
                        },
                        enabled = name.isNotBlank() && description.isNotBlank() &&
                                professor.isNotBlank() && schedule.isNotBlank() &&
                                (imageUri != null || courseToEdit != null)
                    ) {
                        Text(if (courseToEdit == null) "Guardar curso" else "Actualizar curso")
                    }
                }
            }
        }
    }
}
// Helper function to convert URI to File
fun uriToFile(uri: Uri, context: Context): File {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val file = File.createTempFile("course_img_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
    file.outputStream().use { output ->
        inputStream.copyTo(output)
    }
    return file
}