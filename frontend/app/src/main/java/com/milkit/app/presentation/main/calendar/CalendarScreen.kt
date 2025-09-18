package com.milkit.app.presentation.main.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milkit.app.data.model.MilkRecord
import com.milkit.app.data.model.MilkStatus
import com.milkit.app.ui.theme.AutoMarkedYellow
import com.milkit.app.util.Resource
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthlyRecords by viewModel.monthlyRecords.collectAsStateWithLifecycle()

    LaunchedEffect(currentMonth) {
        viewModel.loadMonthlyRecords(currentMonth.year, currentMonth.monthValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Month Navigation
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) }
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }

                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) }
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Grid
        when (monthlyRecords) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                CalendarGrid(
                    currentMonth = currentMonth,
                    records = monthlyRecords.data?.records ?: emptyList()
                )
            }
            is Resource.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    val errorMessage = when (monthlyRecords) {
                        is Resource.Error<*> -> monthlyRecords.message ?: "Failed to load calendar data"
                        else -> "Failed to load calendar data"
                    }
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            null -> {
                // Initial state
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Legend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Received"
                    )
                    LegendItem(
                        color = AutoMarkedYellow,
                        label = "Auto-marked"
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.error,
                        label = "Not Received"
                    )
                    LegendItem(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = "Partial"
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    records: List<MilkRecord>
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
    val recordsMap = records.associateBy { 
        LocalDate.parse(it.date.split("T")[0]).dayOfMonth 
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Day headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar days
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(300.dp)
            ) {
                // Empty cells for days before the first day of the month
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.size(40.dp))
                }

                // Days of the month
                items((1..daysInMonth).toList()) { day ->
                    CalendarDay(
                        day = day,
                        record = recordsMap[day],
                        isToday = currentMonth.atDay(day) == LocalDate.now()
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    record: MilkRecord?,
    isToday: Boolean
) {
    val backgroundColor = when {
        record == null -> Color.Transparent
        record.isAutoMarked -> AutoMarkedYellow
        record.status == MilkStatus.RECEIVED -> MaterialTheme.colorScheme.primary
        record.status == MilkStatus.NOT_RECEIVED -> MaterialTheme.colorScheme.error
        record.status == MilkStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
        else -> Color.Transparent
    }

    val textColor = when {
        record == null -> MaterialTheme.colorScheme.onSurface
        record.isAutoMarked -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { /* TODO: Show day details */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
