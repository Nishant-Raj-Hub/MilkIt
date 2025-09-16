package com.milkit.app.presentation.main.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.milkit.app.data.model.MilkStatus
import com.milkit.app.data.model.MilkType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(
    onDismiss: () -> Unit,
    onSave: (date: String, liters: Double, status: MilkStatus, milkType: MilkType, notes: String?) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var liters by remember { mutableStateOf("1.0") }
    var selectedStatus by remember { mutableStateOf(MilkStatus.RECEIVED) }
    var selectedMilkType by remember { mutableStateOf(MilkType.COW) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Milk Record",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Date Selection
                OutlinedTextField(
                    value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Change")
                        }
                    }
                )

                // Liters Input
                OutlinedTextField(
                    value = liters,
                    onValueChange = { liters = it },
                    label = { Text("Liters") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Status Selection
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Column {
                    MilkStatus.values().forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedStatus == status,
                                    onClick = { selectedStatus = status },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = status.getDisplayName(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Milk Type Selection
                Text(
                    text = "Milk Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Column {
                    MilkType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedMilkType == type,
                                    onClick = { selectedMilkType = type },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMilkType == type,
                                onClick = { selectedMilkType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = type.getDisplayName(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val litersValue = liters.toDoubleOrNull() ?: 1.0
                            val dateString = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val notesText = if (notes.isBlank()) null else notes
                            onSave(dateString, litersValue, selectedStatus, selectedMilkType, notesText)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
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
