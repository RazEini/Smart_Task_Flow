package com.example.smart_taskflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.ui.screen.assignCategory
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val tasksCollection = firestore.collection("tasks")

    // StateFlow של כל המשימות
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    init {
        // מאזין לשינויים בזמן אמת ב-Firestore
        tasksCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val list = snapshot.documents.mapNotNull { doc ->
                try {
                    Task(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        dueDate = doc.getLong("dueDate")?.let { java.util.Date(it) },
                        isDone = doc.getBoolean("isDone") ?: false,
                        isImportant = doc.getBoolean("isImportant") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            _tasks.value = list
        }
    }

    // הוספת משימה
    fun addTask(task: Task) {
        viewModelScope.launch {
            val data = mapOf(
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate?.time,
                "isDone" to task.isDone,
                "isImportant" to task.isImportant
            )
            tasksCollection.add(data)
        }
    }

    // עדכון משימה
    fun updateTask(task: Task) {
        viewModelScope.launch {
            val data = mapOf(
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate?.time,
                "isDone" to task.isDone,
                "isImportant" to task.isImportant
            )
            tasksCollection.document(task.id).set(data)
        }
    }

    // מחיקת משימה
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            tasksCollection.document(taskId).delete()
        }
    }

    // קיבוץ לפי קטגוריה (אם תרצה להשתמש בזה בממשק)
    fun getGroupedTasks(): Map<String, List<Task>> {
        return _tasks.value.groupBy { it.assignCategory() }
    }
}
