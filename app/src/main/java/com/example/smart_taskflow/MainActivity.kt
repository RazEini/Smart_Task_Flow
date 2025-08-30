package com.example.smart_taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smart_taskflow.ui.screen.AppNavigation
import com.example.smart_taskflow.ui.theme.Smart_taskflowTheme // אם יש לך Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Smart_taskflowTheme {
                AppNavigation()
            }
        }
    }
}
