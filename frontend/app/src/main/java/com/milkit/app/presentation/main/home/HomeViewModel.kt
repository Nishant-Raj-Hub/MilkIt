package com.milkit.app.presentation.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkit.app.data.model.MilkRecord
import com.milkit.app.data.model.MilkRecordsResponse
import com.milkit.app.data.model.MilkStatus
import com.milkit.app.data.model.MilkType
import com.milkit.app.data.repository.MilkRepository
import com.milkit.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val milkRepository: MilkRepository
) : ViewModel() {

    private val _todayRecord = MutableStateFlow<Resource<MilkRecord>?>(null)
    val todayRecord: StateFlow<Resource<MilkRecord>?> = _todayRecord.asStateFlow()

    private val _recentRecords = MutableStateFlow<Resource<MilkRecordsResponse>?>(null)
    val recentRecords: StateFlow<Resource<MilkRecordsResponse>?> = _recentRecords.asStateFlow()

    private val _quickStats = MutableStateFlow<Resource<QuickStats>?>(null)
    val quickStats: StateFlow<Resource<QuickStats>?> = _quickStats.asStateFlow()

    private val _addRecordState = MutableStateFlow<Resource<MilkRecord>?>(null)
    val addRecordState: StateFlow<Resource<MilkRecord>?> = _addRecordState.asStateFlow()

    fun loadTodayRecord() {
        viewModelScope.launch {
            milkRepository.getTodayRecord().collect { result ->
                _todayRecord.value = result
            }
        }
    }

    fun loadRecentRecords() {
        viewModelScope.launch {
            val endDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val startDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
            
            milkRepository.getMilkRecords(startDate, endDate, 1, 10).collect { result ->
                _recentRecords.value = result
            }
        }
    }

    fun loadQuickStats() {
        viewModelScope.launch {
            // This is a simplified version - in a real app, you'd have dedicated endpoints
            val weekStart = LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val monthStart = LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            milkRepository.getMilkRecords(weekStart, today).collect { weekResult ->
                if (weekResult is Resource.Success) {
                    milkRepository.getMilkRecords(monthStart, today).collect { monthResult ->
                        if (monthResult is Resource.Success) {
                            val weeklyLiters = weekResult.data?.statistics?.totalLiters ?: 0.0
                            val monthlyLiters = monthResult.data?.statistics?.totalLiters ?: 0.0
                            
                            // Calculate streak (simplified)
                            val streak = calculateStreak(weekResult.data?.records ?: emptyList())
                            
                            _quickStats.value = Resource.Success(
                                QuickStats(
                                    weeklyLiters = weeklyLiters,
                                    monthlyLiters = monthlyLiters,
                                    streak = streak
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun addRecord(
        date: String,
        liters: Double,
        status: MilkStatus,
        milkType: MilkType,
        notes: String?
    ) {
        viewModelScope.launch {
            milkRepository.addMilkRecord(date, liters, status, milkType, notes).collect { result ->
                _addRecordState.value = result
                if (result is Resource.Success) {
                    // Refresh data
                    loadTodayRecord()
                    loadRecentRecords()
                    loadQuickStats()
                }
            }
        }
    }

    fun confirmRecord(recordId: String) {
        viewModelScope.launch {
            milkRepository.confirmRecord(recordId).collect { result ->
                if (result is Resource.Success) {
                    // Refresh data
                    loadTodayRecord()
                    loadRecentRecords()
                }
            }
        }
    }

    private fun calculateStreak(records: List<MilkRecord>): Int {
        // Simple streak calculation - count consecutive days with received status
        var streak = 0
        val sortedRecords = records.sortedByDescending { it.date }
        
        for (record in sortedRecords) {
            if (record.status == MilkStatus.RECEIVED) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }

    fun clearAddRecordState() {
        _addRecordState.value = null
    }
}
