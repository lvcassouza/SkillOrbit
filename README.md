# SkillOrbit

Aplicativo nativo Android para acompanhar estudos com um Dashboard simples e eficaz. Construído em Kotlin com Jetpack Compose (Material 3), arquitetura MVVM, StateFlow, Room (SQLite), DataStore Preferences e Navigation Compose.

## Tech Stack
- Kotlin
- Jetpack Compose (Material Design 3)
- MVVM + ViewModel + StateFlow
- Room Database (SQLite)
- DataStore Preferences (auth leve)
- Navigation Compose
- Material Icons Extended

## Estrutura do Projeto
- `app/src/main/java/com/skillorbit/data/`
  - `CourseEntity.kt`: Entidade Room e enum `CourseStatus`
  - `CourseDao.kt`: DAO com CRUD e `Flow<List<Course>>`
  - `AppDatabase.kt`: Configuração Room + `TypeConverters` do enum
- `app/src/main/java/com/skillorbit/prefs/`
  - `UserPreferences.kt`: DataStore para salvar/ler o nome do usuário
- `app/src/main/java/com/skillorbit/repo/`
  - `CourseRepository.kt`: Integra DAO e DataStore, expõe fluxos e operações
- `app/src/main/java/com/skillorbit/ui/`
  - `CourseViewModel.kt`: Lógica de negócio, `DashboardState`, StateFlow, sugestão UI/UX
  - `Screens.kt`: Telas Compose (Login, Dashboard, Detalhe) e Navegação
- `app/src/main/java/com/skillorbit/MainActivity.kt`: Ponto de entrada, inicializa Room, DataStore, Repository e ViewModel

## Funcionalidades
- Login leve: captura nome do usuário e persiste com DataStore; se existir, vai direto ao Dashboard
- Dashboard:
  - Saudação: "Olá, {Nome do Usuário}"
  - Barra de progresso geral animada (média dos cursos)
  - Lista de cursos com progresso individual e categoria
  - FAB para adicionar novo curso
- Detalhes do curso: editar aulas concluídas e anotações com atualização do progresso em tempo real
- Sugestão (especial): se houver muitos cursos de Backend e nenhum de Frontend/UI/UX, exibir card "Que tal explorar UI/UX?"

## Banco de Dados (Room)
- Entidade `Course`:
  - `id: Long` (PK, auto)
  - `title: String`
  - `category: String` (ex: Backend, Frontend, UI/UX)
  - `totalLessons: Int`
  - `completedLessons: Int`
  - `notes: String`
  - `status: CourseStatus` (EM_ANDAMENTO, CONCLUIDO, PAUSADO)
- `CourseDao` com `insert`, `update`, `delete`, `getAll(): Flow<List<Course>>`, `getById(id)`
- `TypeConverters` para persistir `CourseStatus` como `String`

## Autenticação Leve (DataStore)
- `UserPreferences` expõe `userNameFlow: Flow<String?>`
- `setUserName(name: String)` salva o nome localmente
- A tela de Login observa o fluxo e navega para o Dashboard quando há nome

## ViewModel e Estado
- `CourseViewModel` combina `userNameFlow` e `coursesFlow` em um `DashboardState` único
- `DashboardState` inclui: `userName`, `courses`, `overallProgress`, `suggestion`, `isAddCourseDialogOpen`
- Funções:
  - `saveUserName(name)`
  - `openAddCourseDialog()` / `closeAddCourseDialog()`
  - `addCourse(title, category, totalLessons)`
  - `updateCourseProgress(id, completedLessons, notes)`
  - `calculateOverallProgress(courses)`
  - `buildSuggestion(courses)`

## Navegação
- Rotas:
  - `login`
  - `dashboard`
  - `courseDetail/{id}` (argumento `Long`)

## UI (Compose)
- Material 3 e animações com `animateFloatAsState`
- `LoginScreen`: entrada e persistência do nome
- `DashboardScreen`: progresso geral, card de sugestão e lista com `LazyColumn`
- `CourseDetailScreen`: edição com `OutlinedTextField` e feedback visual

## Configuração do Projeto (Gradle)
Adicione as dependências no `build.gradle` do módulo (versões meramente exemplificativas; ajuste para as mais recentes):

```gradle
plugins {
    id "com.android.application"
    id "org.jetbrains.kotlin.android"
    id "kotlin-kapt"
}

android {
    buildFeatures { compose true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
}

dependencies {
    implementation "androidx.compose.ui:ui:1.7.0"
    implementation "androidx.compose.material3:material3:1.3.0"
    implementation "androidx.compose.material:material-icons-extended:1.7.0"

    implementation "androidx.navigation:navigation-compose:2.8.0"

    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"

    implementation "androidx.datastore:datastore-preferences:1.1.1"
}
```

## Como executar
1. Abra no Android Studio (Koala ou superior)
2. Sincronize Gradle e aguarde o indexamento
3. Rode no emulador/dispositivo
4. Na primeira execução, informe o nome do usuário; depois será redirecionado ao Dashboard

## Exemplo de Uso
- Adicionar curso: no Dashboard, toque no FAB, informe Título, Categoria (ex.: Backend), Total de aulas e confirme
- Editar curso: toque no item da lista, ajuste "Aulas concluídas" e "Anotações", salve

## Próximos passos (opcional)
- Tema claro/escuro dedicado e tipografia customizada
- Testes de unidade/instrumentação para ViewModel e DAO
- Filtros por categoria e ordenação
- Swipe para excluir, undo com SnackBar

## Licença
MIT
