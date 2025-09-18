package com.milkit.app.presentation.main.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milkit.app.data.model.MilkStatistics
import com.milkit.app.util.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    val monthlyStats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val yearlyStats by viewModel.yearlyStats.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadAllStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Your Milk Statistics",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Weekly Stats
        StatsCard(
            title = "This Week",
            subtitle = getWeekRange(),
            stats = weeklyStats,
            icon = Icons.Default.CalendarToday
        )

        // Monthly Stats
        StatsCard(
            title = "This Month",
            subtitle = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            stats = monthlyStats,
            icon = Icons.Default.CalendarMonth
        )

        // Yearly Stats
        StatsCard(
            title = "This Year",
            subtitle = LocalDate.now().year.toString(),
            stats = yearlyStats,
            icon = Icons.Default.CalendarToday
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    subtitle: String,
    stats: Resource<MilkStatistics>?,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (stats) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    StatsContent(stats.data!!)
                }
                is Resource.Error -> {
                    Text(
                        text = stats.message ?: "Failed to load statistics",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                null -> {
                    Text("Loading...")
                }
            }
        }
    }
}

@Composable
private fun StatsContent(stats: MilkStatistics) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Liters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total Milk Received",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stats.totalLiters}L",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Average per day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Average per Day",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${String.format("%.1f", stats.averageLiters)}L",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Divider()

        // Delivery Status Breakdown
        Text(
            text = "Delivery Status",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Received",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stats.receivedCount} days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Not Received",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stats.notReceivedCount} days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Partial Delivery",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stats.partialCount} days",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        if (stats.autoMarkedCount > 0) {
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Auto-marked Records",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${stats.autoMarkedCount} days",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

private fun getWeekRange(): String {
    val today = LocalDate.now()
    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)
    
    return "${startOfWeek.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${endOfWeek.format(DateTimeFormatter.ofPattern("MMM dd"))}"
}
