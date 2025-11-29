package com.skillorbit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Icon
import com.skillorbit.ui.theme.LocalSpacing
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skillorbit.data.Course
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillOrbitApp(viewModel: CourseViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf("dashboard", "profile")
    Scaffold(
        topBar = {
            when (currentRoute) {
                "dashboard" -> TopAppBar(title = { val state by viewModel.state.collectAsState(); Text(text = "Olá, ${state.userName}") })
                "profile" -> TopAppBar(title = { Text(text = "Perfil") })
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = { if (currentRoute != "dashboard") navController.navigate("dashboard") },
                        icon = { Icon(imageVector = Icons.Outlined.Home, contentDescription = "Início") },
                        label = { Text("Início") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = { if (currentRoute != "profile") navController.navigate("profile") },
                        icon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(viewModel = viewModel) { destination ->
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
            composable("login") {
                LoginScreen(viewModel = viewModel, onLoggedIn = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("dashboard") {
                DashboardScreen(viewModel = viewModel, onCourseClick = { id -> navController.navigate("courseDetail/$id") }, outerPadding = padding)
            }
            composable("onboarding") {
                OnboardingScreen(viewModel = viewModel, onFinished = {
                    val state = viewModel.state.value
                    val next = if (state.userName.isBlank()) "login" else "dashboard"
                    navController.navigate(next) { popUpTo("onboarding") { inclusive = true } }
                })
            }
            composable("profile") {
                ProfileScreen(viewModel = viewModel, outerPadding = padding)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: CourseViewModel, onLoggedIn: () -> Unit) {
    val uiState by viewModel.state.collectAsState()
    val userName = uiState.userName
    LaunchedEffect(userName) {
        if (userName.isNotBlank()) onLoggedIn()
    }
    var input by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.fillMaxSize().padding(spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bem-vindo ao SkillOrbit", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(text = "Personalize sua experiência informando seu nome.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(spacing.md))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nome de usuário") },
            singleLine = true,
            supportingText = { if (input.isBlank()) Text("Campo obrigatório") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
        )
        Spacer(modifier = Modifier.height(spacing.md))
        Button(
            onClick = { if (input.isNotBlank()) viewModel.saveUserName(input) },
            enabled = input.isNotBlank()
        ) { Text("Continuar") }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(viewModel: CourseViewModel, onCourseClick: (Long) -> Unit, outerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val progressAnim by animateFloatAsState(targetValue = state.overallProgress, label = "overall")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val spacing = LocalSpacing.current
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openAddCourseDialog() }) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Adicionar curso")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(outerPadding).padding(padding).padding(spacing.lg)) {
            Text(text = "Progresso geral")
            LinearProgressIndicator(progress = progressAnim, modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm))
            val suggestion = state.suggestion
            AnimatedVisibility(visible = suggestion != null, enter = fadeIn(), exit = fadeOut()) {
                Card(colors = CardDefaults.cardColors(), modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm)) {
                    Text(text = suggestion ?: "", modifier = Modifier.padding(spacing.lg))
                }
            }
            var categoryFilter by remember { mutableStateOf("") }
            val sortOptions = listOf("Título", "Progresso", "Categoria")
            var sortIndex by remember { mutableStateOf(0) }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = categoryFilter,
                    onValueChange = { categoryFilter = it },
                    label = { Text("Filtrar categoria") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(spacing.sm))
                TextButton(onClick = { sortIndex = (sortIndex + 1) % sortOptions.size }) {
                    Text(text = "Ordenar: ${sortOptions[sortIndex]}")
                }
            }
            val displayCourses = remember(state.courses, categoryFilter, sortIndex) {
                val filtered = state.courses.filter { categoryFilter.isBlank() || it.category.contains(categoryFilter, ignoreCase = true) }
                when (sortOptions[sortIndex]) {
                    "Título" -> filtered.sortedBy { it.title.lowercase() }
                    "Progresso" -> filtered.sortedByDescending { if (it.totalLessons > 0) it.completedLessons.toFloat() / it.totalLessons.toFloat() else 0f }
                    else -> filtered.sortedBy { it.category.lowercase() }
                }
            }
            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 220.dp), contentPadding = PaddingValues(vertical = spacing.md, horizontal = spacing.sm)) {
                items(displayCourses, key = { it.id }) { course ->
                    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        CourseItem(course = course, onClick = { onCourseClick(course.id) })
                    }
                }
            }
        }
        if (state.isAddCourseDialogOpen) {
            AddCourseDialog(onDismiss = { viewModel.closeAddCourseDialog() }, onConfirm = { title, category, total -> viewModel.addCourse(title, category, total) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseItem(course: Course, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ratio = if (course.totalLessons > 0) course.completedLessons.toFloat() / course.totalLessons.toFloat() else 0f
    val anim by animateFloatAsState(targetValue = ratio, label = "item")
    val spacing = LocalSpacing.current
    Card(onClick = onClick, modifier = modifier.fillMaxWidth().padding(vertical = spacing.sm)) {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Text(text = course.title, style = MaterialTheme.typography.titleMedium)
            Text(text = course.category, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = anim, modifier = Modifier.fillMaxWidth().padding(top = spacing.sm))
        }
    }
}

@Composable
fun AddCourseDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }
    val spacing = LocalSpacing.current
    Card(modifier = Modifier.fillMaxWidth().padding(spacing.lg)) {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Text(text = "Novo curso")
            OutlinedTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Título") })
            OutlinedTextField(value = category, onValueChange = { category = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Categoria") })
            OutlinedTextField(value = totalText, onValueChange = { totalText = it.filter { ch -> ch.isDigit() } }, modifier = Modifier.fillMaxWidth(), label = { Text("Total de aulas") }, keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))
            Row(modifier = Modifier.fillMaxWidth().padding(top = spacing.md), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(spacing.xs))
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
    val state by viewModel.state.collectAsState()
    val course = state.courses.find { it.id == id }
    if (course == null) {
        val spacing = LocalSpacing.current
        Column(modifier = Modifier.fillMaxSize().padding(spacing.xl), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Curso não encontrado")
            Spacer(modifier = Modifier.height(spacing.sm))
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
    val spacing = LocalSpacing.current
    Scaffold(topBar = { TopAppBar(title = { Text(course.title) }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(spacing.lg)) {
            Text(text = course.category)
            LinearProgressIndicator(progress = anim, modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm))
            val isValid = completedText.toIntOrNull()?.let { it in 0..total } == true
            OutlinedTextField(
                value = completedText,
                onValueChange = { completedText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Aulas concluídas") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { if (!isValid) Text("Valor entre 0 e $total") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Anotações") }, modifier = Modifier.fillMaxWidth().padding(top = spacing.sm))
            Row(modifier = Modifier.fillMaxWidth().padding(top = spacing.md), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onBack) { Text("Cancelar") }
                Spacer(modifier = Modifier.width(spacing.xs))
                Button(onClick = {
                    val completed = completedText.toIntOrNull() ?: 0
                    viewModel.updateCourseProgress(id, completed, notes)
                    onBack()
                }, enabled = isValid) { Text("Salvar") }
            }
        }
    }
}

@Composable
fun SplashScreen(viewModel: CourseViewModel, decide: (String) -> Unit) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.onboardingCompleted, state.userName) {
        val destination = when {
            !state.onboardingCompleted -> "onboarding"
            state.userName.isBlank() -> "login"
            else -> "dashboard"
        }
        decide(destination)
    }
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Carregando...")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: CourseViewModel, onFinished: () -> Unit) {
    val spacing = LocalSpacing.current
    Column(modifier = Modifier.fillMaxSize().padding(spacing.xl), verticalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(text = "Bem-vindo ao SkillOrbit", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(text = "Acompanhe seus estudos com progresso, anotações e sugestões.")
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(text = "Adicione cursos, edite progresso e personalize seu tema.")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = {
                viewModel.completeOnboarding()
                onFinished()
            }) { Text("Pular") }
            Spacer(modifier = Modifier.width(spacing.xs))
            Button(onClick = {
                viewModel.completeOnboarding()
                onFinished()
            }) { Text("Começar") }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: CourseViewModel, outerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    var notifications by remember { mutableStateOf(state.notificationsEnabled) }
    var theme by remember { mutableStateOf(state.themeMode) }
    val spacing = LocalSpacing.current
    Column(
        modifier = Modifier.fillMaxSize().padding(outerPadding).padding(spacing.xl),
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Usuário", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(text = state.userName.ifBlank { "Não definido" }, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(spacing.md))
        Text(text = "Preferências", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(spacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Notificações")
            Spacer(modifier = Modifier.weight(1f))
            androidx.compose.material3.Switch(checked = notifications, onCheckedChange = {
                notifications = it
                viewModel.setNotificationsEnabled(it)
            })
        }
        Spacer(modifier = Modifier.height(spacing.sm))
        Text(text = "Tema")
        Row(modifier = Modifier.fillMaxWidth().padding(top = spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { theme = "SYSTEM"; viewModel.setThemeMode("SYSTEM") }) { Text("Sistema") }
            Spacer(modifier = Modifier.width(spacing.xs))
            TextButton(onClick = { theme = "LIGHT"; viewModel.setThemeMode("LIGHT") }) { Text("Claro") }
            Spacer(modifier = Modifier.width(spacing.xs))
            TextButton(onClick = { theme = "DARK"; viewModel.setThemeMode("DARK") }) { Text("Escuro") }
        }
    }
}
