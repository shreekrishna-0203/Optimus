package com.example.optimus.ui1

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.optimus.GradientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    GradientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                )
            }
        ) { paddingValues ->
            // Making the content scrollable in case the screen has a lot of content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(state = rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Welcome to your profile!",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Trial Membership Card
                ProfileCard(
                    title = "Trial Membership",
                    content = "Access to up to 6 gyms for a limited period.",
                    price = "₹399",
                    slashedPrice = "₹799",
                    onClick = {
                        // Navigate to Trial Membership details
                        navController.navigate("membership_details/trial")
                    },
                    icon = Icons.Default.FitnessCenter // Optional: Add an icon to make it stand out
                )

                // Permanent Membership Card
                ProfileCard(
                    title = "Permanent Membership",
                    content = "Unlimited access to all gyms and premium features.",
                    price = "₹899",
                    slashedPrice = "₹1299",
                    onClick = {
                        // Navigate to Permanent Membership details
                        navController.navigate("membership_details/permanent")
                    },
                    icon = Icons.Default.Star // Optional: Add a premium icon
                )

                // Purchase History Card
                ProfileCard(
                    title = "Purchase History",
                    content = "View your purchase history",
                    onClick = {
                        // Navigate to Purchase History screen
                        navController.navigate("purchase_history")
                    },
                    icon = Icons.Default.History
                )

                // Rewards Card
                ProfileCard(
                    title = "Rewards",
                    content = "Coming soon...",
                    onClick = {
                        // Optional: Navigate to Rewards page when it's available
                    },
                    isComingSoon = true,
                    icon = Icons.Default.StarBorder
                )

                // Contact Us Card
                ProfileCard(
                    title = "Contact Us",
                    content = "Get in touch with support",
                    onClick = {
                        // Navigate to Contact Us screen
                        navController.navigate("contact_us")
                    },
                    icon = Icons.Default.HelpOutline
                )

                // Sign Out Button with improved design
                Button(
                    onClick = {
                        // Sign out logic
                        onSignOut()
                        // Navigate to LoginScreen
                        navController.navigate("login") {
                            // Clear back stack to prevent returning to ProfileScreen
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sign Out",
                        color = MaterialTheme.colorScheme.onError,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Enhanced ProfileCard with rounded corners, shadow effects, and icons
@Composable
fun ProfileCard(
    title: String,
    content: String,
    onClick: () -> Unit,
    isComingSoon: Boolean = false,
    price: String? = null,
    slashedPrice: String? = null,
    icon: ImageVector? = null // Optional icon for better visual hierarchy
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
            .shadow(elevation = 8.dp, shape = MaterialTheme.shapes.medium), // Add shadow for depth
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isComingSoon) "Coming soon..." else content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isComingSoon) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
            )

            // Displaying price information if available
            if (price != null && slashedPrice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = slashedPrice,
                        style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}




