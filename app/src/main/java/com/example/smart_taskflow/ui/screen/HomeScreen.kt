package com.example.smart_taskflow.ui.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

fun Task.calculatePriority(): Int {
    if (isDone) return 0
    var score = 0
    if (isImportant) score += 3
    dueDate?.let {
        val daysLeft = ((it.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
        when {
            daysLeft <= 0 -> score += 5
            daysLeft <= 1 -> score += 4
            daysLeft <= 3 -> score += 2
        }
    }
    if (title.contains("דחוף", ignoreCase = true)) score += 2
    return score.coerceAtMost(7)
}

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

fun categoryColor(category: String): Color = when (category) {
    "בית" -> Color(0xFFF57F17)
    "עבודה" -> Color(0xFF1565C0)
    "חשבונות" -> Color(0xFFD84315)
    "לימודים" -> Color(0xFF6A1B9A)
    else -> Color(0xFF424242)
}

fun priorityColor(priority: Int): Color = when (priority) {
    0 -> Color.Gray
    1, 2 -> Color.Yellow
    3, 4 -> Color(0xFFFF9800)
    else -> Color.Red
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newDueDate by remember { mutableStateOf<Date?>(null) }
    var newImportant by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editDueDate by remember { mutableStateOf<Date?>(null) }
    var editImportant by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("TaskFlow", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
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
        val sortedTasks = tasks.sortedByDescending { it.calculatePriority() }
        val groupedTasks = sortedTasks.groupBy { it.assignCategory() }

        if (sortedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("אין משימות כרגע 😴", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                groupedTasks.forEach { (category, tasksInCategory) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(categoryColor(category).copy(alpha = 0.2f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor(category)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "(${tasksInCategory.count { !it.isDone }} / ${tasksInCategory.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    items(tasksInCategory) { task ->
                        ModernTaskItem(
                            task = task,
                            onDelete = { viewModel.deleteTask(task.id) },
                            onToggleDone = {
                                viewModel.updateTask(task.copy(isDone = !task.isDone))
                            },
                            onEdit = {
                                editingTask = task
                                editTitle = task.title
                                editDescription = task.description
                                editDueDate = task.dueDate
                                editImportant = task.isImportant
                                showEditDialog = true
                            },
                            onImportantToggle = { isImportant ->
                                viewModel.updateTask(task.copy(isImportant = isImportant))
                            }
                        )
                    }
                }
            }
        }
    }

    // --- דיאלוג הוספה ---
    if (showAddDialog) {
        TaskDialog(
            title = "משימה חדשה",
            taskTitle = newTitle,
            taskDescription = newDescription,
            taskDueDate = newDueDate,
            taskImportant = newImportant,
            onTitleChange = { newTitle = it },
            onDescriptionChange = { newDescription = it },
            onDueDateChange = { newDueDate = it },
            onImportantChange = { newImportant = it },
            onConfirm = {
                if (newTitle.isNotBlank()) {
                    val task = Task(
                        id = UUID.randomUUID().toString(),
                        title = newTitle,
                        description = newDescription,
                        dueDate = newDueDate,
                        isImportant = newImportant
                    )
                    viewModel.addTask(task)
                    newTitle = ""; newDescription = ""; newDueDate = null; newImportant = false
                    showAddDialog = false
                }
            },
            onDismiss = {
                newTitle = ""; newDescription = ""; newDueDate = null; newImportant = false
                showAddDialog = false
            }
        )
    }

    // --- דיאלוג עריכה ---
    if (showEditDialog && editingTask != null) {
        TaskDialog(
            title = "עריכת משימה",
            taskTitle = editTitle,
            taskDescription = editDescription,
            taskDueDate = editDueDate,
            taskImportant = editImportant,
            onTitleChange = { editTitle = it },
            onDescriptionChange = { editDescription = it },
            onDueDateChange = { editDueDate = it },
            onImportantChange = { editImportant = it },
            onConfirm = {
                editingTask?.let {
                    val updatedTask = it.copy(
                        title = editTitle,
                        description = editDescription,
                        dueDate = editDueDate,
                        isImportant = editImportant
                    )
                    viewModel.updateTask(updatedTask)
                }
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun ModernTaskItem(
    task: Task,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onImportantToggle: (Boolean) -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (task.isDone) Color(0xFFE0F7FA) else Color.White
    )
    val priority = task.calculatePriority()
    val animatedColor by animateColorAsState(targetValue = priorityColor(priority))

    val today = Calendar.getInstance().time
    val dueColor = task.dueDate?.let { date ->
        val diff = date.time - today.time
        when {
            diff < 0 -> Color.Gray
            diff <= 2 * 24 * 60 * 60 * 1000 -> Color.Red
            diff <= 7 * 24 * 60 * 60 * 1000 -> Color.Yellow
            else -> Color.Green
        }
    } ?: Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(),
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
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (task.isDone) Color.Gray else Color(0xFF212121)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconToggleButton(
                        checked = task.isImportant,
                        onCheckedChange = onImportantToggle
                    ) {
                        Icon(
                            imageVector = if (task.isImportant) Icons.Default.Star else Icons.Default.Star,
                            contentDescription = "חשוב",
                            tint = if (task.isImportant) Color(0xFFFFD700) else Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(categoryColor(task.assignCategory()))
                    )
                }
                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (task.isDone) Color.Gray else Color(0xFF616161)
                        )
                    )
                }
                task.dueDate?.let { date ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "יעד: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = dueColor)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onToggleDone,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (task.isDone) Color(0xFF4CAF50) else Color(0xFFE0E0E0))
                ) {
                    Icon(Icons.Default.Check, contentDescription = "סמן כבוצע", tint = Color.White)
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1976D2))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "ערוך", tint = Color.White)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "מחיקה", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun TaskDialog(
    title: String,
    taskTitle: String,
    taskDescription: String,
    taskDueDate: Date?,
    taskImportant: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDueDateChange: (Date?) -> Unit,
    onImportantChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    taskDueDate?.let { calendar.time = it }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                TextField(
                    value = taskTitle,
                    onValueChange = onTitleChange,
                    label = { Text("כותרת") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = taskDescription,
                    onValueChange = onDescriptionChange,
                    label = { Text("תיאור") }
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = taskImportant, onCheckedChange = onImportantChange)
                    Text("חשוב")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    DatePickerDialog(
                        context,
                        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                            val pickedCalendar = Calendar.getInstance()
                            pickedCalendar.set(year, month, dayOfMonth)
                            onDueDateChange(pickedCalendar.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(taskDueDate?.let { "עד: ${dateFormat.format(it)}" } ?: "בחר תאריך יעד")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) { Text("שמור", color = Color.White) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("ביטול") } }
    )
}
