package com.example.smart_taskflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.ui.screen.assignCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val tasksCollection = firestore.collection("tasks")

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        // מאזין לשינויים ב-auth
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            startListening()
        }
    }

    // UID בזמן אמת
    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val currentUserIdPublic: String
        get() = currentUserId

    // מאזין לשינויים בזמן אמת עבור המשתמש הנוכחי
    private fun startListening() {
        // נקה מאזין קודם אם היה
        listenerRegistration?.remove()
        listenerRegistration = null

        val uid = currentUserId
        if (uid.isEmpty()) {
            _tasks.value = emptyList()
            return
        }

        listenerRegistration = tasksCollection.whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        Task(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            dueDate = doc.getLong("dueDate")?.let { java.util.Date(it) },
                            isDone = doc.getBoolean("isDone") ?: false,
                            isImportant = doc.getBoolean("isImportant") ?: false,
                            userId = doc.getString("userId") ?: ""
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
        val uid = currentUserId
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val data = mapOf(
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate?.time,
                "isDone" to task.isDone,
                "isImportant" to task.isImportant,
                "userId" to uid
            )
            tasksCollection.add(data)
        }
    }

    // עדכון משימה
    fun updateTask(task: Task) {
        val uid = currentUserId
        if (uid.isEmpty()) return

        viewModelScope.launch {
            val data = mapOf(
                "title" to task.title,
                "description" to task.description,
                "dueDate" to task.dueDate?.time,
                "isDone" to task.isDone,
                "isImportant" to task.isImportant,
                "userId" to uid
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

    // קיבוץ לפי קטגוריה
    fun getGroupedTasks(): Map<String, List<Task>> {
        return _tasks.value.groupBy { it.assignCategory() }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
