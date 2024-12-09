package com.example.optimus.ui1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.optimus.GradientBackground
import com.example.optimus.R
import com.example.optimus.data.Gym
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import kotlin.random.Random


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    GradientBackground {
        val context = LocalContext.current
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        var userName by remember { mutableStateOf("") }
        var selectedLocation by remember { mutableStateOf("") }
        val nearbyGyms = remember { mutableStateListOf<Gym>() }

        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                fetchUserLocation(context) { latitude, longitude ->
                    fetchNearbyGyms(context, nearbyGyms, latitude, longitude)
                    updateLocationName(context, latitude, longitude) {
                        selectedLocation = it
                    }
                }
            } else {
                Toast.makeText(context, "Location permission is required.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Fetch user data and location
        LaunchedEffect(Unit) {
            val userId = auth.currentUser?.uid
            userId?.let {
                firestore.collection("users").document(it).get().addOnSuccessListener { document ->
                    userName = document.getString("name") ?: "User"
                }
            }

            // Check for location permission
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fetchUserLocation(context) { latitude, longitude ->
                    fetchNearbyGyms(context, nearbyGyms, latitude, longitude)
                    updateLocationName(context, latitude, longitude) {
                        selectedLocation = it
                    }
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // Refresh Location Logic
        val refreshLocation: () -> Unit = {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fetchUserLocation(context) { latitude, longitude ->
                    nearbyGyms.clear() // Clear existing gyms before fetching new data
                    fetchNearbyGyms(context, nearbyGyms, latitude, longitude)
                    updateLocationName(context, latitude, longitude) {
                        selectedLocation = it
                    }
                }
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigationBar(navController) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Wrap content in a scrollable column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Enable vertical scrolling
                ) {
                    TopStatusBar()

                    TrialMembershipWidget()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Do you want to be",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OPTIMUS?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LocationSection(selectedLocation, onRefreshLocation = refreshLocation)

                    Text(
                        text = "These are our current centres! GO BUILD GAINS!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(nearbyGyms) { gym ->
                            GymCard(
                                gym,
                                Modifier.width(300.dp)
                            ) // Adjust width for horizontal cards
                        }
                    }

                    // Add Contact Section
                    ContactSection()
                }
            }
        }

    }
}


@Composable
private fun GymCard(gym: Gym, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(vertical = 8.dp)
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Dynamic Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = gym.imageUrl,
                    contentDescription = "Gym Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.calendar),
                    placeholder = painterResource(id = R.drawable.calendar)
                )
            }

            // Gym Details Section
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = gym.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1
                )

                Text(
                    text = gym.address,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    ),
                    maxLines = 2
                )

                Button(
                    onClick = { /* Handle Try Free Action */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "TRY FOR FREE",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Composable
fun TopStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun LocationSection(selectedLocation: String, onRefreshLocation: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = selectedLocation,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
        Button(
            onClick = { onRefreshLocation() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC))
        ) {
            Text("Refresh Location", color = Color.White)
        }
    }
}




fun fetchUserLocation(
    context: Context,
    onLocationFetched: (latitude: Double, longitude: Double) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationFetched(it.latitude, it.longitude)
            } ?: run {
                Toast.makeText(
                    context,
                    "Unable to fetch location. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

fun fetchNearbyGyms(
    context: Context,
    nearbyGyms: MutableList<Gym>,
    latitude: Double,
    longitude: Double
) {
    val location = "$latitude,$longitude"
    val apiKey = context.getString(R.string.google_maps_key)
    // Add 'gym' as a keyword to ensure only gym-related places are returned
    val url =
        "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=$location&radius=2000&type=gym&keyword=gym&key=$apiKey"

    val requestQueue = Volley.newRequestQueue(context)
    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET, url, null,
        { response ->
            nearbyGyms.clear() // Clear previous data to avoid duplicates
            val results = response.getJSONArray("results")
            for (i in 0 until results.length()) {
                val gym = results.getJSONObject(i)
                val id = gym.getString("place_id")
                val name = gym.getString("name")
                val address = gym.optString("vicinity", "Address not available")
                val distance = "Approx. ${Random.nextInt(1, 2)} km"

                // Extract image URL from `photos`
                val photos = gym.optJSONArray("photos")
                val imageUrl = if (photos != null && photos.length() > 0) {
                    val photoReference = photos.getJSONObject(0).getString("photo_reference")
                    "https://maps.googleapis.com/maps/api/place/photo" +
                            "?maxwidth=400" +
                            "&photoreference=$photoReference" +
                            "&key=$apiKey"
                } else {
                    "https://via.placeholder.com/400" // Fallback placeholder image
                }

                // Add the Gym object to the list
                nearbyGyms.add(Gym(id, name, address, distance, imageUrl))
            }
        },
        { error ->
            Log.e("FetchGyms", "Error fetching gyms: ${error.message}")
        }
    )
    requestQueue.add(jsonObjectRequest)
}


fun updateLocationName(
    context: Context,
    latitude: Double,
    longitude: Double,
    onLocationUpdated: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        val locationName = addresses?.firstOrNull()?.subLocality ?: "Your Location"
        onLocationUpdated(locationName)
    } catch (e: Exception) {
        onLocationUpdated("Your Location")
    }
}

//Trial membership
@Composable
fun TrialMembershipWidget(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Trial Membership",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹799",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹399",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2575FC)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Available for 6 gyms",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }

            Button(
                onClick = { /* Handle Purchase */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "BUY NOW",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

//Contact

@Composable
fun ContactSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Needed to get the context for starting the intents

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2B55BB), Color(0xFF000000)),
                    startY = 0f,
                    endY = 1000f
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Contact Us",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "We'd love to hear from you! Reach us via WhatsApp or Email.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // WhatsApp Button
                ContactButton(
                    icon = painterResource(id = R.drawable.whatsapp), // WhatsApp icon drawable
                    text = "WhatsApp",
                    onClick = {
                        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                            data =
                                Uri.parse("https://api.whatsapp.com/send?phone=+917204178445") // Replace with your number
                            setPackage("com.whatsapp")
                        }
                        context.startActivity(whatsappIntent)
                    }
                )

                // Email Button
                ContactButton(
                    icon = painterResource(id = R.drawable.email), // Email icon drawable
                    text = "Email",
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data =
                                Uri.parse("mailto:skhebbarkd@gmail.com") // Replace with your email
                            putExtra(Intent.EXTRA_SUBJECT, "Inquiry/Sales/Contact")
                            putExtra(Intent.EXTRA_TEXT, "Type your issue")
                        }
                        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                    }
                )
            }
        }
    }
}


@Composable
fun ContactButton(icon: Painter, text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color.White)
        }
    }
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = Color.White)
        }
    }
}




