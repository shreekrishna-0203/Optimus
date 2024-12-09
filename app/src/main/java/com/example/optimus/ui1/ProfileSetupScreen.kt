package com.example.optimus.ui1

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.optimus.GradientBackground
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController, onSetupComplete: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // Location permission
    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLoading = true
            fetchUserLocation(context) { address ->
                location = address
                isLoading = false
            }
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val formEnabled = !isLoading
    val isFormValid = name.isNotBlank() && dob.isNotBlank() && gender.isNotBlank() && location.isNotBlank()

    GradientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heading
                Text(
                    text = "Create Your Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 32.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Name Field
                        ProfileField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Full Name",
                            leadingIcon = Icons.Default.Person,
                            formEnabled = formEnabled
                        )

                        // Date of Birth Field with Calendar Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProfileField(
                                    value = dob,
                                    onValueChange = {},
                                    label = "Date of Birth",
                                    leadingIcon = Icons.Default.CalendarToday,
                                    readOnly = true,
                                    formEnabled = formEnabled
                                )
                            }

                            // Calendar Button
                            IconButton(
                                onClick = {
                                    if (formEnabled) showCalendar = true
                                },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    )
                                    .size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = "Open Calendar",
                                    tint = Color.White
                                )
                            }
                        }

                        // Gender Field
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                enabled = formEnabled,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Gender",
                                        tint = Color.White
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            gender = option
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Location Field
                        ProfileField(
                            value = location.ifBlank { "Tap map icon to select location" },
                            onValueChange = {},
                            label = "Location",
                            leadingIcon = Icons.Default.LocationOn,
                            trailingIcon = {
                                IconButton(onClick = {
                                    locationPermissionRequest.launch(
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                }) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "Detect Location")
                                }
                            },
                            readOnly = true,
                            formEnabled = formEnabled
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                // Save Button
                Button(
                    onClick = {
                        if (!isFormValid) {
                            Toast.makeText(context, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true
                        val userId = auth.currentUser?.uid
                        if (userId == null) {
                            Toast.makeText(context, "User is not authenticated", Toast.LENGTH_SHORT).show()
                            isLoading = false
                            return@Button
                        }

                        val userProfile = hashMapOf(
                            "name" to name,
                            "dob" to dob,
                            "gender" to gender,
                            "location" to location,
                            "isRegistrationComplete" to true  // Add this flag

                        )

                        scope.launch {
                            try {
                                firestore.collection("users").document(userId).set(userProfile).await()
                                navController.navigate("home") {
                                    popUpTo("profileSetup") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Profile setup failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2575FC)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF2575FC),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Complete Setup")
                    }
                }
            }
        }

        // CalendarDialog for DOB
        CalendarDialog(
            showCalendar = showCalendar,
            onDateSelected = { selectedDate ->
                dob = selectedDate
                showCalendar = false
            },
            onDismiss = { showCalendar = false }
        )
    }
}



@SuppressLint("MissingPermission")
fun fetchUserLocation(context: Context, onLocationFetched: (String) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val address = addresses?.get(0)?.getAddressLine(0) // Full address
                    if (address != null) {
                        onLocationFetched(address)
                    }
                } else {
                    onLocationFetched("Unable to fetch address")
                }
            }
        } else {
            onLocationFetched("Location not found")
        }
    }.addOnFailureListener {
        onLocationFetched("Error fetching location: ${it.message}")
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDialog(
    showCalendar: Boolean,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentView by remember { mutableStateOf(CalendarViewType.YEAR) }
    var selectedYear by remember {
        mutableStateOf(
            Calendar.getInstance().get(Calendar.YEAR)
        )
    }
    var selectedMonth by remember {
        mutableStateOf(
            Calendar.getInstance().get(Calendar.MONTH)
        )
    }
    var selectedDay by remember {
        mutableStateOf(
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
    }

    // Get the current date
    val currentDate = Calendar.getInstance()
    val currentYear = currentDate.get(Calendar.YEAR)
    val currentMonth = currentDate.get(Calendar.MONTH)
    val currentDay = currentDate.get(Calendar.DAY_OF_MONTH)

    if (showCalendar) {
        // Overlay to block interactions with the background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss)
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(500.dp)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with year, month, and day selection (Top arrow removed)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left arrow for navigating back between views
                        IconButton(onClick = {
                            currentView = when (currentView) {
                                CalendarViewType.DAY -> CalendarViewType.MONTH
                                CalendarViewType.MONTH -> CalendarViewType.YEAR
                                CalendarViewType.YEAR -> currentView // Don't go back from year view
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous view"
                            )
                        }

                        // Only the text for the selected year, month, or day
                        Text(
                            text = when (currentView) {
                                CalendarViewType.YEAR -> "$selectedYear"
                                CalendarViewType.MONTH -> {
                                    val calendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, selectedMonth)
                                    }
                                    calendar.getDisplayName(
                                        Calendar.MONTH,
                                        Calendar.LONG,
                                        Locale.getDefault()
                                    ) + " $selectedYear"
                                }

                                CalendarViewType.DAY -> {
                                    val calendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, selectedMonth)
                                        set(Calendar.DAY_OF_MONTH, selectedDay)
                                    }
                                    val monthName = calendar.getDisplayName(
                                        Calendar.MONTH,
                                        Calendar.LONG,
                                        Locale.getDefault()
                                    )
                                    "$monthName $selectedDay, $selectedYear"
                                }
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.clickable {
                                currentView = when (currentView) {
                                    CalendarViewType.DAY -> CalendarViewType.MONTH
                                    CalendarViewType.MONTH -> CalendarViewType.YEAR
                                    CalendarViewType.YEAR -> currentView
                                }
                            }
                        )
                    }

                    // Dynamic content based on current view
                    when (currentView) {
                        CalendarViewType.YEAR -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Generate years up to current year
                                items((currentYear - 100 until currentYear + 1).toList()) { year ->
                                    if (year <= currentYear) { // Hide future years
                                        Box(
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .fillMaxWidth()
                                                .background(
                                                    color = if (year == selectedYear)
                                                        MaterialTheme.colorScheme.primary.copy(
                                                            alpha = 0.2f
                                                        )
                                                    else Color.Transparent,
                                                )
                                                .clickable {
                                                    selectedYear = year
                                                    currentView = CalendarViewType.MONTH
                                                }
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = year.toString(),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        CalendarViewType.MONTH -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(12) { monthIndex ->
                                    if (selectedYear == currentYear && monthIndex > currentMonth) {
                                        return@items // Skip months in the future
                                    }

                                    val monthName = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, monthIndex)
                                    }.getDisplayName(
                                        Calendar.MONTH,
                                        Calendar.SHORT,
                                        Locale.getDefault()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                            .background(
                                                color = if (monthIndex == selectedMonth)
                                                    MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.2f
                                                    )
                                                else Color.Transparent,
                                            )
                                            .clickable {
                                                if (selectedYear < currentYear || monthIndex <= currentMonth) {
                                                    selectedMonth = monthIndex
                                                    currentView = CalendarViewType.DAY
                                                }
                                            }
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = monthName,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }

                        CalendarViewType.DAY -> {
                            // Create a calendar instance to calculate days in the month
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }
                            val daysInMonth =
                                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

                            // Days of the week header
                            val weekdayNames =
                                arrayOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                weekdayNames.forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // Generate day grid
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Add empty items for days before the first day of the month
                                items(List(firstDayOfWeek - 1) { Unit }) {
                                    Box(modifier = Modifier.size(48.dp)) // Empty space
                                }

                                // Add days of the month
                                items(daysInMonth) { day ->
                                    val actualDay = day + 1
                                    val isDateInTheFuture = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, selectedYear)
                                        set(Calendar.MONTH, selectedMonth)
                                        set(Calendar.DAY_OF_MONTH, actualDay)
                                    }.timeInMillis > currentDate.timeInMillis

                                    if (selectedYear == currentYear && selectedMonth == currentMonth && actualDay > currentDay) {
                                        return@items // Skip future days
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = if (actualDay == selectedDay)
                                                    MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.2f
                                                    )
                                                else Color.Transparent,
                                            )
                                            .clickable(
                                                onClick = {
                                                    if (selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth) || (selectedYear == currentYear && selectedMonth == currentMonth && actualDay <= currentDay)) {
                                                        selectedDay = actualDay
                                                        val dateFormat = SimpleDateFormat(
                                                            "dd MMM yyyy",
                                                            Locale.getDefault()
                                                        )
                                                        onDateSelected(
                                                            dateFormat.format(
                                                                Calendar
                                                                    .getInstance()
                                                                    .apply {
                                                                        set(
                                                                            Calendar.YEAR,
                                                                            selectedYear
                                                                        )
                                                                        set(
                                                                            Calendar.MONTH,
                                                                            selectedMonth
                                                                        )
                                                                        set(
                                                                            Calendar.DAY_OF_MONTH,
                                                                            selectedDay
                                                                        )
                                                                    }.time
                                                            )
                                                        )
                                                        onDismiss()
                                                    }
                                                },
                                                enabled = !isDateInTheFuture
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = actualDay.toString(),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class CalendarViewType {
    YEAR, MONTH, DAY
}

@Composable
fun ProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    onClick: (() -> Unit)? = null,
    formEnabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color.White
            )
        },
        trailingIcon = trailingIcon, // Optional trailing icon
        readOnly = readOnly,
        enabled = formEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = onClick != null && formEnabled,
                onClick = { onClick?.invoke() }
            ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.7f)
        )
    )
}
