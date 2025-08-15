package com.example.smart_taskflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_taskflow.data.model.Task
import com.example.smart_taskflow.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository = TaskRepository()) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> get() = _tasks

    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> get() = _aiResponse

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _tasks.value = repository.getAllTasks()
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
            loadTasks()
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            loadTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task) // עדכון ב-Firebase
            _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
        }
    }

    fun updateTaskInFirestore(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task) // עדכון ב־Firebase
            _tasks.value = _tasks.value.map { if (it.id == task.id) task else it } // עדכון ב‑UI
        }
    }
}
