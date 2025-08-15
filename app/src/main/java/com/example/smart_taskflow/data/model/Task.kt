package com.example.smart_taskflow.data.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isDone: Boolean = false,
    val category: String = "אחר"
)

