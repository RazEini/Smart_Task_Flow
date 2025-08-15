package com.example.smart_taskflow.data.repository

import com.example.smart_taskflow.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tasks")

    suspend fun getAllTasks(): List<Task> {
        val snapshot = tasksCollection.get().await()
        return snapshot.toObjects(Task::class.java)
            .filter { it.id.isNotBlank() && it.title.isNotBlank() }
    }

    suspend fun addTask(task: Task) {
        // תמיד צור id חדש אם אין
        val taskId = if (task.id.isBlank()) tasksCollection.document().id else task.id
        tasksCollection.document(taskId).set(task.copy(id = taskId)).await()
    }

    suspend fun deleteTask(taskId: String) {
        tasksCollection.document(taskId).delete().await()
    }

    suspend fun updateTask(task: Task) {
        db.collection("tasks")
            .document(task.id)
            .set(task) // מחליף את כל המסמך
            .await()
    }

}
