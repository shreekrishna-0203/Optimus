package com.example.optimus.ui1

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


val firebaseUser = Firebase.auth.currentUser
val username = firebaseUser?.displayName ?: "defaultUsername" // Replace with actual logic if needed
val gymId = "sampleGymId" // Replace this with actual gym ID logic if available


@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val items = listOf("home", "workouts", "attendance", "analytics", "profile")
        items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (item) {
                            "home" -> Icons.Default.Home
                            "workouts" -> Icons.Default.FitnessCenter
                            "profile" -> Icons.Default.Person
                            "attendance" -> Icons.Default.QrCode
                            "analytics" -> Icons.Default.AutoGraph
                            else -> Icons.Default.Help
                        },
                        contentDescription = item
                    )
                },
                label = { Text(text = item.capitalize()) },
                selected = false,
                onClick = {
                    if (item == "attendance") {
                        // Navigate to attendance with parameters
                        navController.navigate("attendance/$username/$gymId")
                    } else if (item == "analytics") {
                        // Navigate to analytics with parameters
                        navController.navigate("analytics/$username/$gymId")
                    } else {
                        navController.navigate(item)
                    }
                }
            )
        }
    }
}