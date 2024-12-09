package com.example.optimus.ui1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.optimus.GradientBackground
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(navController: NavController, attendanceData: Map<String, List<LocalDate>>) {
    // State to track the selected month

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val attendanceDays = attendanceData[currentYearMonth.toString()] ?: emptyList()


GradientBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Attendance Analytics",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        Row(
                            modifier = Modifier.padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                            }) {
                                Text("< Prev", style = MaterialTheme.typography.labelLarge)
                            }
                            TextButton(onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                            }) {
                                Text("Next >", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Stats
                QuickStatsSection(
                    weeklyVisits = attendanceDays.count {
                        it.isAfter(
                            LocalDate.now().minusWeeks(1)
                        )
                    },
                    totalMinutes = attendanceDays.size * 45, // Example: assume each session is 45 mins
                    activeDays = attendanceDays.distinct().size
                )

                // Heatmap Section
                HeatmapSection(
                    yearMonth = currentYearMonth,
                    daysInMonth = daysInMonth,
                    attendanceDays = attendanceDays
                )
            }
        }
    }
}

@Composable
fun QuickStatsSection(weeklyVisits: Int, totalMinutes: Int, activeDays: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatCard("Weekly Visits", weeklyVisits.toString())
        StatCard("Total Minutes", totalMinutes.toString())
        StatCard("Active Days", activeDays.toString())
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        shape = RoundedCornerShape(5.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun HeatmapSection(
    yearMonth: YearMonth,
    daysInMonth: Int,
    attendanceDays: List<LocalDate>
) {
    Text(
        text = "Attendance for ${yearMonth.month.name} ${yearMonth.year}",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(7), // Representing 7 days of the week
        modifier = Modifier.fillMaxWidth()
    ) {
        items(daysInMonth) { index ->
            val day = index + 1
            val isPresent = attendanceDays.any { it.dayOfMonth == day }
            val intensity = if (isPresent) (attendanceDays.count { it.dayOfMonth == day } * 50).coerceAtMost(255) else 0
            val dayColor = if (isPresent) Color(0, 128, 0, intensity) else Color.LightGray

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(dayColor, shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$day",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}
