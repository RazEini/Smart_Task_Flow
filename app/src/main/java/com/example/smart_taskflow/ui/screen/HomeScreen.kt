package com.example.smart_taskflow.ui.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavController

// ---------- חישוב עדיפות ----------
fun Task.calculatePriority(): Int {
    if (isDone) return 0
    var score = 0
    if (isImportant) score += 3

    dueDate?.let { date ->
        val today = Calendar.getInstance()
        val taskDate = Calendar.getInstance().apply { time = date }

        val daysLeft = ((date.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()

        score += when {
            taskDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    taskDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> 5
            taskDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    taskDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1 -> 3
            daysLeft in 2..3 -> 2
            else -> 0
        }
    }

    if (title.contains("דחוף", ignoreCase = true)) score += 2
    return score.coerceAtMost(10)
}

// ---------- קיבוץ לפי תאריכים ----------
fun Task.dateGroup(): String {
    val today = Calendar.getInstance()
    val taskDate = Calendar.getInstance().apply { dueDate?.let { time = it } }
    return when {
        dueDate == null -> "ללא תאריך"
        taskDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                taskDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "היום"
        taskDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                taskDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1 -> "מחר"
        taskDate.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR) -> "השבוע"
        else -> "בעתיד"
    }
}

// ---------- קטגוריות אוטומטיות ----------
fun Task.assignCategory(): String {
    val categoryPatterns = mapOf(
        "בית" to listOf("קני[ןיות]?", "ניקי[ו]ן", "בישול", "בשל", "כביסה", "סידור", "סידר"),
        "עבודה" to listOf("פגיש[ה|ות]?", "דוח", "שיחה", "טלפון", "מייל", "דוא״ל"),
        "חשבונות" to listOf("תשלום", "חשבונית?", "חוב", "חיוב", "קבלה"),
        "לימודים" to listOf("ספר", "תרגיל", "מבחן", "לימוד", "שיעור", "פרויקט", "שאלה")
    )
    for ((category, patterns) in categoryPatterns) {
        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            if (regex.containsMatchIn(title) || regex.containsMatchIn(description)) return category
        }
    }
    return "אחר"
}

// ---------- צבעים ----------
fun priorityColor(priority: Int): Color = when (priority) {
    0 -> Color.Gray
    1, 2 -> Color(0xFFFFC107)
    3, 4 -> Color(0xFFFF9800)
    else -> Color.Red
}

// ---------- מסך הבית ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel = viewModel(),
    navController: NavController,
    category: String = "all"
) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showDetailsTask by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var archiveExpanded by remember { mutableStateOf(false) }

    val tasksInCategory = remember(tasks, category, searchQuery) {
        tasks
            .filter { category == "all" || it.assignCategory() == category }
            .filter { it.title.contains(searchQuery, true) || it.description.contains(searchQuery, true) }
    }

    val groupedTasks = remember(tasksInCategory) {
        tasksInCategory.sortedByDescending { it.calculatePriority() }
            .groupBy { it.dateGroup() }
    }

    val archivedTasks = tasks.filter { it.isDone }

    LaunchedEffect(archivedTasks) {
        if (archivedTasks.isEmpty()) archiveExpanded = false
    }

    Scaffold(
        topBar = {
            Column {
                SmallTopAppBar(
                    title = { Text("TaskFlow", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(0xFF6200EE),
                        titleContentColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text(" ...חיפוש משימות") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6200EE),
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Add Task") }
        },
        containerColor = Color(0xFFF2F2F2)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) { Text("אין משימות 😴", color = Color.Gray) }
                }
            } else {
                if (category != "all") {
                    item {
                        val total = tasksInCategory.count()
                        Text(
                            "קטגוריה: $category ($total משימות)",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(12.dp)
                        )
                    }
                }

                groupedTasks.forEach { (group, tasksInGroup) ->
                    val normalTasks = tasksInGroup.filter { !it.isDone }
                    if (normalTasks.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray.copy(alpha = 0.2f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(group, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("(${normalTasks.count()} / ${tasksInGroup.size})", color = Color.Gray)
                            }
                        }
                        items(normalTasks) { task ->
                            ModernTaskItem(
                                task = task,
                                onDelete = { viewModel.deleteTask(task.id) },
                                onToggleDone = { viewModel.updateTask(task.copy(isDone = !task.isDone)) },
                                onImportantToggle = { viewModel.updateTask(task.copy(isImportant = !task.isImportant)) },
                                onEdit = {
                                    editingTask = task
                                    showEditDialog = true
                                },
                                onShowDetails = { showDetailsTask = task },
                                isArchiveItem = false
                            )
                        }
                    }
                }

                if (archivedTasks.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .padding(12.dp)
                                .clickable { archiveExpanded = !archiveExpanded },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ארכיון", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (archiveExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }
                    if (archiveExpanded) {
                        items(archivedTasks) { task ->
                            ModernTaskItem(
                                task = task,
                                onDelete = { viewModel.deleteTask(task.id) },
                                onToggleDone = { viewModel.updateTask(task.copy(isDone = false)) },
                                onImportantToggle = { viewModel.updateTask(task.copy(isImportant = !task.isImportant)) },
                                onEdit = {
                                    editingTask = task
                                    showEditDialog = true
                                },
                                onShowDetails = { showDetailsTask = task },
                                isArchiveItem = true
                            )
                        }
                    }
                }

                // כפתור חזרה ל-Dashboard בסוף הרשימה
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
                    ) {
                        Text("חזור למסך הראשי", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        TaskDialog(
            title = "משימה חדשה",
            onConfirm = { title, description, dueDate, important ->
                viewModel.addTask(
                    Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        isImportant = important
                    )
                )
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog && editingTask != null) {
        TaskDialog(
            title = "עריכת משימה",
            initialTitle = editingTask!!.title,
            initialDescription = editingTask!!.description,
            initialDueDate = editingTask!!.dueDate,
            initialImportant = editingTask!!.isImportant,
            onConfirm = { title, description, dueDate, important ->
                viewModel.updateTask(
                    editingTask!!.copy(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                        isImportant = important
                    )
                )
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    showDetailsTask?.let { task -> TaskDetailsDialog(task) { showDetailsTask = null } }
}

// ---------- שורת משימה ----------
@Composable
fun ModernTaskItem(
    task: Task,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit,
    onImportantToggle: () -> Unit,
    onEdit: () -> Unit,
    onShowDetails: () -> Unit,
    isArchiveItem: Boolean
) {
    val backgroundColor by animateColorAsState(if (task.isDone && !isArchiveItem) Color(0xFFE0F7FA) else Color.White)
    val priority = remember(task) {
        var score = task.calculatePriority()
        task.dueDate?.let { date ->
            val calendar = Calendar.getInstance()
            val today = Calendar.getInstance()
            calendar.time = date
            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                score += 2
            }
        }
        score.coerceAtMost(7)
    }
    val animatedColor by animateColorAsState(targetValue = priorityColor(priority))
    val categorySymbol = when(task.assignCategory()) {
        "בית" -> "🏠"
        "עבודה" -> "💼"
        "לימודים" -> "🎓"
        "חשבונות" -> "🧾"
        else -> "🏷️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 100) onToggleDone()
                    if (dragAmount < -100) onDelete()
                }
            }
            .clickable { onShowDetails() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(60.dp)
                    .background(animatedColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f) // תופס רק את החלל שנותר
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isDone && !isArchiveItem) Color.Gray else Color(0xFF212121)
                        ),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f) // תופס את החלל שנותר ולא חורג על האייקונים
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(categorySymbol, fontSize = MaterialTheme.typography.titleMedium.fontSize)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onImportantToggle) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "חשוב",
                            tint = if (task.isImportant) Color(0xFFFFC107) else Color.Gray
                        )
                    }
                }

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        task.description.lines().firstOrNull() ?: "",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (task.isDone && !isArchiveItem) Color.Gray else Color(0xFF616161)
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onToggleDone,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isArchiveItem) Color(0xFFFF9800)
                            else if (task.isDone) Color(0xFF4CAF50)
                            else Color(0xFFE0E0E0)
                        )
                ) {
                    Icon(
                        imageVector = if (isArchiveItem) Icons.Default.ArrowBack else Icons.Default.Check,
                        contentDescription = if (isArchiveItem) "הוצא מהארכיון" else "סמן כבוצע",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2))
                ) { Icon(Icons.Default.Edit, contentDescription = "ערוך", tint = Color.White) }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                ) { Icon(Icons.Default.Delete, contentDescription = "מחיקה", tint = Color.White) }
            }
        }
    }
}

// ---------- דיאלוג פרטי משימה ----------
@Composable
fun TaskDetailsDialog(task: Task, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(task.title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (task.description.isNotEmpty()) Text(task.description)
                task.dueDate?.let {
                    Text("תאריך יעד: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}")
                }
                Text("חשוב: ${if (task.isImportant) "כן" else "לא"}")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("סגור") }
        }
    )
}

// ---------- דיאלוג משימות (חדש / עריכה) ----------
@Composable
fun TaskDialog(
    title: String,
    initialTitle: String = "",
    initialDescription: String = "",
    initialDueDate: Date? = null,
    initialImportant: Boolean = false,
    onConfirm: (title: String, description: String, dueDate: Date?, important: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var taskTitle by remember { mutableStateOf(initialTitle) }
    var taskDescription by remember { mutableStateOf(initialDescription) }
    var taskDueDate by remember { mutableStateOf(initialDueDate) }
    var taskImportant by remember { mutableStateOf(initialImportant) }
    val calendar = Calendar.getInstance().apply { taskDueDate?.let { time = it } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("כותרת") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("תיאור") }
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = taskImportant,
                        onCheckedChange = { taskImportant = it }
                    )
                    Text("חשוב")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, day: Int ->
                            calendar.set(year, month, day)
                            taskDueDate = calendar.time
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(taskDueDate?.let { "עד: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)}" } ?: "בחר תאריך יעד")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(taskTitle, taskDescription, taskDueDate, taskImportant) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) { Text("שמור", color = Color.White) }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("ביטול") }
        }
    )
}
