package com.example.optimus

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.optimus.ui.theme.OptimusTheme
import com.example.optimus.ui1.AnalyticsScreen
import com.example.optimus.ui1.AttendanceScreen
import com.example.optimus.ui1.HomeScreen
import com.example.optimus.ui1.LoginScreen
import com.example.optimus.ui1.MembershipDetailsScreen
import com.example.optimus.ui1.MembershipScreen
import com.example.optimus.ui1.OnboardingScreen
import com.example.optimus.ui1.PaymentScreen
import com.example.optimus.ui1.PermanentMembershipScreen
import com.example.optimus.ui1.ProfileScreen
import com.example.optimus.ui1.ProfileSetupScreen
import com.example.optimus.ui1.RegistrationScreen
import com.example.optimus.ui1.TrialMembershipScreen
import com.example.optimus.ui1.WorkoutsScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate


class MainActivity : ComponentActivity() {
    companion object {
        const val DEFAULT_LAT = 37.7749
        const val DEFAULT_LNG = -122.4194
        const val DEFAULT_ZOOM = 12f
        const val PREFS_NAME = "MyAppPrefs"
        const val KEY_FIRST_LAUNCH = "isFirstLaunch"
        const val KEY_PROFILE_COMPLETE = "isProfileComplete"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OptimusTheme {
                val navController = rememberNavController()

                // Call MainApp with navigation controller
                MainApp(navController = navController)
            }
        }
    }

    private fun determineStartDestination(): String {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentUser = FirebaseAuth.getInstance().currentUser

        return when {
            prefs.getBoolean(KEY_FIRST_LAUNCH, true) -> "onboarding"
            currentUser != null -> {
                if (prefs.getBoolean(KEY_PROFILE_COMPLETE, false)) "home"
                else "profileSetup"
            }

            else -> "login"
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var isAuthenticated by remember { mutableStateOf(false) }
    var isRegistered by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Determine start destination
    val startDestination = remember {
        val prefs = context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(MainActivity.KEY_FIRST_LAUNCH, true)
        if (isFirstLaunch) {
            prefs.edit().putBoolean(MainActivity.KEY_FIRST_LAUNCH, false).apply()
            "onboarding"
        } else if (auth.currentUser != null) {
            val isProfileComplete = prefs.getBoolean(MainActivity.KEY_PROFILE_COMPLETE, false)
            if (isProfileComplete) "home" else "profileSetup"
        } else {
            "login"
        }
    }

    // Monitor authentication and registration
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            isAuthenticated = true
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    isRegistered = document.getBoolean("isRegistrationComplete") == true
                    context.setRegistrationStatus(isRegistered)
                    isLoading = false
                }
                .addOnFailureListener {
                    isRegistered = context.isRegistered() // Use local storage as fallback
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        AppNavHost(
            navController = navController,
            startDestination = when {
                isAuthenticated && isRegistered -> "home"
                isAuthenticated -> "profileSetup"
                else -> startDestination
            }
        )
    }
}

@OptIn(ExperimentalGetImage::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onGetStarted = {
                    navController.navigateAndClear("login")
                }
            )
        }

        composable("login") {
            LoginScreen(
                navController = navController,
                onNavigateToRegister = {
                    navController.navigate("registration")
                },
                onLoginSuccess = { isRegistered ->
                    val destination = if (isRegistered) "home" else "profileSetup"
                    navController.navigateAndClear(destination)
                }
            )
        }

        composable("registration") {
            RegistrationScreen(
                navController = navController,
                onRegistrationSuccess = { isProfileComplete ->
                    val destination = if (isProfileComplete) "home" else "profileSetup"
                    navController.navigateAndClear(destination)
                }
            )
        }

        composable("profileSetup") {
            ProfileSetupScreen(
                navController = navController,
                onSetupComplete = {
                    navController.navigateAndClear("home")
                }
            )
        }

        composable("home") {
            HomeScreen(navController = navController)
        }

        composable("workouts") {
            WorkoutsScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigateAndClear("login")
                }
            )
        }

        composable("membership") {
            MembershipScreen(navController = navController)
        }

        composable("membership_details/{membershipType}") { backStackEntry ->
            val membershipType = backStackEntry.arguments?.getString("membershipType") ?: "trial"
            MembershipDetailsScreen(navController = navController, membershipType = membershipType)
        }

        composable("membership_details/trial") {
            TrialMembershipScreen(navController = navController)
        }

        composable("membership_details/permanent") {
            PermanentMembershipScreen(navController = navController)
        }

        composable("payment") {
            PaymentScreen(navController = navController)
        }

        composable("analytics/{username}/{gymId}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val gymId = backStackEntry.arguments?.getString("gymId") ?: ""

            // Use remember for state management
            val attendanceDataState = remember { mutableStateOf<Map<String, List<LocalDate>>>(emptyMap()) }
            val isLoading = remember { mutableStateOf(true) }

            LaunchedEffect(username, gymId) {
                if (username.isNotEmpty() && gymId.isNotEmpty()) {
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val docRef = db.collection("attendance").document("$username-$gymId")
                        docRef.get()
                            .addOnSuccessListener { document ->
                                if (document != null) {
                                    val attendanceMap = document.data?.mapValues { entry ->
                                        @Suppress("UNCHECKED_CAST")
                                        (entry.value as List<String>).map { LocalDate.parse(it) }
                                    } ?: emptyMap()
                                    attendanceDataState.value = attendanceMap
                                }
                                isLoading.value = false
                            }
                            .addOnFailureListener {
                                // Handle failure (e.g., show an error message)
                                isLoading.value = false
                            }
                    } catch (e: Exception) {
                        // Handle exceptions
                        isLoading.value = false
                    }
                }
            }

            if (isLoading.value) {
                LoadingScreen()
            } else {
                AnalyticsScreen(navController = navController, attendanceData = attendanceDataState.value)
            }
        }


        // **Dynamic Attendance Screen**
        composable("attendance/{username}/{gymId}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username")
            val gymId = backStackEntry.arguments?.getString("gymId")
            AttendanceScreen(username = username ?: "", gymId = gymId ?: "")
        }

    }
}


@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}



fun Context.setRegistrationStatus(isRegistered: Boolean) {
    getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(MainActivity.KEY_PROFILE_COMPLETE, isRegistered)
        .apply()
}

fun Context.isRegistered(): Boolean {
    return getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(MainActivity.KEY_PROFILE_COMPLETE, false)
}

fun NavHostController.navigateAndClear(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { inclusive = true }
        launchSingleTop = true
    }
}


@Composable
fun GradientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF2B55BB),
                        Color(0xFF000000)

                    )
                )
            )
    ) {
        content()
    }
}

private fun Context.updateProfileCompletionStatus() {
    getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(MainActivity.KEY_PROFILE_COMPLETE, true)
        .apply()
}
