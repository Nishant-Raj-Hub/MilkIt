package com.milkit.app.presentation.main.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkit.app.data.model.MilkStatistics
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
class StatsViewModel @Inject constructor(
    private val milkRepository: MilkRepository
) : ViewModel() {

    private val _weeklyStats = MutableStateFlow<Resource<MilkStatistics>?>(null)
    val weeklyStats: StateFlow<Resource<MilkStatistics>?> = _weeklyStats.asStateFlow()

    private val _monthlyStats = MutableStateFlow<Resource<MilkStatistics>?>(null)
    val monthlyStats: StateFlow<Resource<MilkStatistics>?> = _monthlyStats.asStateFlow()

    private val _yearlyStats = MutableStateFlow<Resource<MilkStatistics>?>(null)
    val yearlyStats: StateFlow<Resource<MilkStatistics>?> = _yearlyStats.asStateFlow()

    fun loadAllStats() {
        loadWeeklyStats()
        loadMonthlyStats()
        loadYearlyStats()
    }

    private fun loadWeeklyStats() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val startDate = startOfWeek.format(DateTimeFormatter.ISO_LOCAL_DATE)

            milkRepository.getMilkRecords(startDate, endDate).collect { result ->
                _weeklyStats.value = when (result) {
                    is Resource.Success -> Resource.Success(result.data?.statistics ?: MilkStatistics(0.0, 0.0, 0, 0, 0, 0))
                    is Resource.Error -> Resource.Error(result.message ?: "Failed to load weekly stats")
                    is Resource.Loading -> Resource.Loading()
                }
            }
        }
    }

    private fun loadMonthlyStats() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfMonth = today.withDayOfMonth(1)
            val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val startDate = startOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE)

            milkRepository.getMilkRecords(startDate, endDate).collect { result ->
                _monthlyStats.value = when (result) {
                    is Resource.Success -> Resource.Success(result.data?.statistics ?: MilkStatistics(0.0, 0.0, 0, 0, 0, 0))
                    is Resource.Error -> Resource.Error(result.message ?: "Failed to load monthly stats")
                    is Resource.Loading -> Resource.Loading()
                }
            }
        }
    }

    private fun loadYearlyStats() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startOfYear = today.withDayOfYear(1)
            val endDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val startDate = startOfYear.format(DateTimeFormatter.ISO_LOCAL_DATE)

            milkRepository.getMilkRecords(startDate, endDate).collect { result ->
                _yearlyStats.value = when (result) {
                    is Resource.Success -> Resource.Success(result.data?.statistics ?: MilkStatistics(0.0, 0.0, 0, 0, 0, 0))
                    is Resource.Error -> Resource.Error(result.message ?: "Failed to load yearly stats")
                    is Resource.Loading -> Resource.Loading()
                }
            }
        }
    }
}
