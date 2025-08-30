package com.example.smart_taskflow.ui.screen

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smart_taskflow.ui.screen.HomeScreen
import com.example.smart_taskflow.viewmodel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.smart_taskflow.R

// ---- Navigation ----
@Composable
fun AppNavigation(viewModel: TaskViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("dashboard") { DashboardScreen(viewModel = viewModel, navController = navController) }
        composable("home/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            HomeScreen(
                viewModel = viewModel,
                navController = navController,
                category = category
            )
        }
    }
}


// ---- Splash ----
@Composable
fun SplashScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    var alphaAnim by remember { mutableStateOf(0f) }

    // אנימציית fade-in
    LaunchedEffect(Unit) {
        // אנימציה של fade-in
        androidx.compose.animation.core.animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 1000)
        ) { value, _ ->
            alphaAnim = value
        }

        // השהייה קצרה כדי שהמשתמש יראה את המסך
        kotlinx.coroutines.delay(1500)

        // ניווט בהתאם למשתמש
        if (user != null) {
            navController.navigate("dashboard") { popUpTo("splash") { inclusive = true } }
        } else {
            navController.navigate("login") { popUpTo("splash") { inclusive = true } }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EE)), // רקע יפה
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TaskFlow",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

// ---- Login ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // רקע
        Image(
            painter = painterResource(id = R.drawable.taskflow_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        // תוכן
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TaskFlow",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("אימייל") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("סיסמה") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "אנא מלא אימייל וסיסמה", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    navController.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "שגיאה: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text("התחבר", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { navController.navigate("register") }) {
                        Text("עדיין אין לך חשבון? הירשם כאן", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.taskflow_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "הרשמה",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("שם מלא") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("אימייל") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("סיסמה") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF1E88E5),
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener { result ->
                                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(displayName)
                                        .build()
                                    result.user?.updateProfile(profileUpdates)
                                        ?.addOnCompleteListener {
                                            navController.navigate("dashboard") { popUpTo("register") { inclusive = true } }
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "שגיאה: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text("הרשם", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("כבר יש לך חשבון? התחבר כאן", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: TaskViewModel = viewModel(), navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val username = user?.displayName ?: "משתמש"

    val tasks by viewModel.tasks.collectAsState(initial = emptyList())
    val totalTasks = tasks.size

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // --- רקע ---
        Image(
            painter = painterResource(id = R.drawable.taskflow_bg), // ודא שהתמונה קיימת ב-drawable
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // --- תוכן המסך ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle, // אייקון של משתמש
                    contentDescription = "User Icon",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "שלום, $username 👋",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // אם הרקע כהה, צבע לבן
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "יש לך $totalTasks משימות",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            // כאן ה-LazyColumn עם המשימות
            val categories = listOf("כל המשימות", "בית", "עבודה", "לימודים", "חשבונות", "אחר")
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val count = when (category) {
                        "כל המשימות" -> tasks.size
                        "אחר" -> tasks.count {
                            val cat = it.assignCategory()
                            cat != "בית" && cat != "עבודה" && cat != "לימודים" && cat != "חשבונות"
                        }
                        else -> tasks.count { it.assignCategory() == category }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val route = when (category) {
                                    "כל המשימות" -> "all"
                                    "אחר" -> "other"
                                    else -> category
                                }
                                navController.navigate("home/$route")
                            }
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (category) {
                                        "בית" -> "🏠"
                                        "עבודה" -> "💼"
                                        "לימודים" -> "🎓"
                                        "חשבונות" -> "🧾"
                                        else -> "🏷️"
                                    },
                                    fontSize = MaterialTheme.typography.headlineMedium.fontSize
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Text("$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(48.dp)) }
            }

            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") { popUpTo("dashboard") { inclusive = true } }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("התנתק", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
