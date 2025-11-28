package com.skillorbit.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skillorbit.data.Course

@Composable
fun SkillOrbitTheme(content: @Composable () -> Unit) {
    val light = lightColorScheme()
    val dark = darkColorScheme()
    MaterialTheme(colorScheme = light, content = content)
}

@Composable
fun SkillOrbitApp(viewModel: CourseViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(viewModel = viewModel, onLoggedIn = { navController.navigate("dashboard") { popUpTo("login") { inclusive = true } } })
        }
        composable("dashboard") {
            DashboardScreen(viewModel = viewModel, onCourseClick = { id -> navController.navigate("courseDetail/$id") })
        }
        composable(
            route = "courseDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            CourseDetailScreen(id = id, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: CourseViewModel, onLoggedIn: () -> Unit) {
    val state = viewModel.state
    val userName = androidx.compose.runtime.collectAsState(state).value.userName
    LaunchedEffect(userName) {
        if (userName.isNotBlank()) onLoggedIn()
    }
    var input by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Digite seu nome de usuário")
        Spacer(modifier = Modifier.padding(8.dp))
        OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.padding(8.dp))
        Button(onClick = { if (input.isNotBlank()) viewModel.saveUserName(input) }) { Text("Salvar") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: CourseViewModel, onCourseClick: (Long) -> Unit) {
    val state = androidx.compose.runtime.collectAsState(viewModel.state).value
    val progressAnim by animateFloatAsState(targetValue = state.overallProgress, label = "overall")
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Olá, ${state.userName}") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddCourseDialog() }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = "Progresso geral")
            LinearProgressIndicator(progress = progressAnim, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            if (state.suggestion != null) {
                Card(colors = CardDefaults.cardColors(), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(text = state.suggestion, modifier = Modifier.padding(16.dp))
                }
            }
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(state.courses) { course ->
                    CourseItem(course = course, onClick = { onCourseClick(course.id) })
                }
            }
        }
        if (state.isAddCourseDialogOpen) {
            AddCourseDialog(onDismiss = { viewModel.closeAddCourseDialog() }, onConfirm = { title, category, total -> viewModel.addCourse(title, category, total) })
        }
    }
}

@Composable
fun CourseItem(course: Course, onClick: () -> Unit) {
    val ratio = if (course.totalLessons > 0) course.completedLessons.toFloat() / course.totalLessons.toFloat() else 0f
    val anim by animateFloatAsState(targetValue = ratio, label = "item")
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = course.title, style = MaterialTheme.typography.titleMedium)
            Text(text = course.category, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = anim, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Novo curso")
            OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Título") })
            OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Categoria") })
            OutlinedTextField(value = totalText, onValueChange = { totalText = it.filter { ch -> ch.isDigit() } }, modifier = Modifier.fillMaxWidth(), label = { Text("Total de aulas") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Spacer(modifier = Modifier.padding(4.dp))
                Button(onClick = {
                    val total = totalText.toIntOrNull() ?: 0
                    onConfirm(title, category, total)
                    onDismiss()
                }) { Text("Adicionar") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(id: Long, viewModel: CourseViewModel, onBack: () -> Unit) {
    val state = androidx.compose.runtime.collectAsState(viewModel.state).value
    val course = state.courses.find { it.id == id }
    if (course == null) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Curso não encontrado")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onBack) { Text("Voltar") }
        }
        return
    }
    var completedText by remember { mutableStateOf(course.completedLessons.toString()) }
    var notes by remember { mutableStateOf(course.notes) }
    val total = course.totalLessons
    val localRatio = remember(completedText, total) {
        val v = completedText.toIntOrNull()?.coerceIn(0, total) ?: 0
        if (total > 0) v.toFloat() / total.toFloat() else 0f
    }
    val anim by animateFloatAsState(targetValue = localRatio, label = "detail")
    Scaffold(topBar = { TopAppBar(title = { Text(course.title) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(text = course.category)
            LinearProgressIndicator(progress = anim, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            OutlinedTextField(value = completedText, onValueChange = { completedText = it.filter { ch -> ch.isDigit() } }, label = { Text("Aulas concluídas") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Anotações") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onBack) { Text("Cancelar") }
                Spacer(modifier = Modifier.padding(4.dp))
                Button(onClick = {
                    val completed = completedText.toIntOrNull() ?: 0
                    viewModel.updateCourseProgress(id, completed, notes)
                    onBack()
                }) { Text("Salvar") }
            }
        }
    }
}

