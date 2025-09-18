package com.milkit.app.presentation.main.share

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.milkit.app.util.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    viewModel: ShareViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.TEXT) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val shareState by viewModel.shareState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Export & Share Data",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Export your milk delivery records to share with others or keep as backup.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Date Range Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Date
                    OutlinedTextField(
                        value = startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        onValueChange = { },
                        label = { Text("From") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select start date")
                            }
                        }
                    )

                    // End Date
                    OutlinedTextField(
                        value = endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        onValueChange = { },
                        label = { Text("To") },
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Select end date")
                            }
                        }
                    )
                }

                // Quick Date Range Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = {
                            startDate = LocalDate.now().minusDays(7)
                            endDate = LocalDate.now()
                        },
                        label = { Text("Last 7 days") },
                        selected = false
                    )
                    FilterChip(
                        onClick = {
                            startDate = LocalDate.now().minusDays(30)
                            endDate = LocalDate.now()
                        },
                        label = { Text("Last 30 days") },
                        selected = false
                    )
                    FilterChip(
                        onClick = {
                            startDate = LocalDate.now().minusDays(90)
                            endDate = LocalDate.now()
                        },
                        label = { Text("Last 3 months") },
                        selected = false
                    )
                }
            }
        }

        // Export Format Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                ExportFormat.values().forEach { format ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = format.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = format.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Export Actions
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Export Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Export Button
                Button(
                    onClick = {
                        viewModel.exportData(
                            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            selectedFormat
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = exportState !is Resource.Loading<String>
                ) {
                    if (exportState is Resource.Loading<String>) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Export Data")
                }

                // Quick Share Button
                OutlinedButton(
                    onClick = {
                        viewModel.generateShareText(
                            startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = shareState !is Resource.Loading<String>
                ) {
                    if (shareState is Resource.Loading<String>) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Quick Share")
                }
            }
        }

        // Status Messages
        when (exportState) {
            is Resource.Success<String> -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export completed successfully!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            is Resource.Error<String> -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exportState.message ?: "Export failed",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            else -> {}
        }

        // Share State Handling
        LaunchedEffect(shareState) {
            if (shareState is Resource.Success<*>) {
                val shareData = shareState.data!!
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareData.shareText)
                    putExtra(Intent.EXTRA_SUBJECT, "My Milk Delivery Summary")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                viewModel.clearShareState()
            }
        }

        // Export Templates
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Export Templates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            startDate = LocalDate.now().withDayOfMonth(1)
                            endDate = LocalDate.now()
                            selectedFormat = ExportFormat.TEXT
                            viewModel.exportData(
                                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                selectedFormat
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            Text("This Month", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            startDate = LocalDate.now().minusMonths(1).withDayOfMonth(1)
                            endDate = LocalDate.now().minusMonths(1).withDayOfMonth(
                                LocalDate.now().minusMonths(1).lengthOfMonth()
                            )
                            selectedFormat = ExportFormat.CSV
                            viewModel.exportData(
                                startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                selectedFormat
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.TableChart, contentDescription = null)
                            Text("Last Month CSV", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

enum class ExportFormat(val displayName: String, val description: String, val apiFormat: String) {
    TEXT("Text Summary", "Human-readable summary with statistics", "text"),
    CSV("CSV File", "Spreadsheet format for data analysis", "csv")
}
