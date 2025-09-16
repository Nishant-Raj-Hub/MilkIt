package com.milkit.app.presentation.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milkit.app.data.model.MilkRecord
import com.milkit.app.data.model.MilkStatus
import com.milkit.app.data.model.MilkType
import com.milkit.app.ui.theme.AutoMarkedYellow
import com.milkit.app.ui.theme.AutoMarkedYellowDark
import com.milkit.app.util.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val todayRecord by viewModel.todayRecord.collectAsStateWithLifecycle()
    val recentRecords by viewModel.recentRecords.collectAsStateWithLifecycle()
    val quickStats by viewModel.quickStats.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTodayRecord()
        viewModel.loadRecentRecords()
        viewModel.loadQuickStats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Status Card
        TodayStatusCard(
            todayRecord = todayRecord,
            onAddClick = { showAddDialog = true },
            onConfirmClick = { record ->
                viewModel.confirmRecord(record.id)
            }
        )

        // Quick Stats Row
        QuickStatsRow(quickStats = quickStats)

        // Recent Records
        Text(
            text = "Recent Records",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        when (recentRecords) {
            is Resource.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentRecords.data?.records ?: emptyList()) { record ->
                        MilkRecordCard(
                            record = record,
                            onConfirmClick = { viewModel.confirmRecord(record.id) }
                        )
                    }
                }
            }
            is Resource.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = recentRecords.message ?: "Failed to load recent records",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            null -> {
                // Initial state
            }
        }
    }

    // Add/Edit Record Dialog
    if (showAddDialog) {
        AddRecordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { date, liters, status, milkType, notes ->
                viewModel.addRecord(date, liters, status, milkType, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TodayStatusCard(
    todayRecord: Resource<MilkRecord>?,
    onAddClick: () -> Unit,
    onConfirmClick: (MilkRecord) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                todayRecord is Resource.Success && todayRecord.data?.isAutoMarked == true -> AutoMarkedYellow
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Milk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add record"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (todayRecord) {
                is Resource.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading today's record...")
                    }
                }
                is Resource.Success -> {
                    val record = todayRecord.data!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${record.liters}L ${record.milkType.getDisplayName()}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = record.status.getDisplayName(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = when (record.status) {
                                    MilkStatus.RECEIVED -> MaterialTheme.colorScheme.primary
                                    MilkStatus.NOT_RECEIVED -> MaterialTheme.colorScheme.error
                                    MilkStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
                                }
                            )
                        }

                        if (record.isAutoMarked) {
                            Button(
                                onClick = { onConfirmClick(record) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AutoMarkedYellowDark
                                )
                            ) {
                                Text("Confirm")
                            }
                        }
                    }

                    if (record.isAutoMarked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Auto-marked - Please confirm",
                            style = MaterialTheme.typography.bodySmall,
                            color = AutoMarkedYellowDark
                        )
                    }
                }
                is Resource.Error -> {
                    Text(
                        text = "Failed to load today's record",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                null -> {
                    Text("No record for today")
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    quickStats: Resource<QuickStats>?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (quickStats) {
            is Resource.Success -> {
                val stats = quickStats.data!!
                QuickStatCard(
                    title = "This Week",
                    value = "${stats.weeklyLiters}L",
                    icon = Icons.Default.CalendarWeek,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "This Month",
                    value = "${stats.monthlyLiters}L",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Streak",
                    value = "${stats.streak} days",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }
            else -> {
                repeat(3) {
                    Card(
                        modifier = Modifier.weight(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (quickStats is Resource.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("--")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MilkRecordCard(
    record: MilkRecord,
    onConfirmClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (record.isAutoMarked) AutoMarkedYellow else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = LocalDate.parse(record.date.split("T")[0]).format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${record.liters}L ${record.milkType.getDisplayName()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = record.status.getDisplayName(),
                    style = MaterialTheme.typography.bodySmall,
                    color = when (record.status) {
                        MilkStatus.RECEIVED -> MaterialTheme.colorScheme.primary
                        MilkStatus.NOT_RECEIVED -> MaterialTheme.colorScheme.error
                        MilkStatus.PARTIAL -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }

            if (record.isAutoMarked) {
                TextButton(onClick = onConfirmClick) {
                    Text("Confirm")
                }
            }
        }
    }
}

data class QuickStats(
    val weeklyLiters: Double,
    val monthlyLiters: Double,
    val streak: Int
)
