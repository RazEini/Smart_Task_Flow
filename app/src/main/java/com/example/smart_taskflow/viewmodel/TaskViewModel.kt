package com.example.smart_taskflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.ui.screen.assignCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    // StateFlow של כל המשימות
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    // הוספת משימה
    fun addTask(task: Task) {
        viewModelScope.launch {
            _tasks.value = _tasks.value + task
        }
    }

    // עדכון משימה
    fun updateTask(updatedTask: Task) {
        viewModelScope.launch {
            _tasks.value = _tasks.value.map { if (it.id == updatedTask.id) updatedTask else it }
        }
    }

    // מחיקת משימה לפי מזהה
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            _tasks.value = _tasks.value.filter { it.id != taskId }
        }
    }

    // קיבוץ משימות לפי קטגוריה
    fun getGroupedTasks(): Map<String, List<Task>> {
        return _tasks.value.groupBy { it.assignCategory() }
    }
}
