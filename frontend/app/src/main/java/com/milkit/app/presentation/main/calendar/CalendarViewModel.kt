package com.milkit.app.presentation.main.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkit.app.data.model.MilkRecordsResponse
import com.milkit.app.data.repository.MilkRepository
import com.milkit.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val milkRepository: MilkRepository
) : ViewModel() {

    private val _monthlyRecords = MutableStateFlow<Resource<MilkRecordsResponse>?>(null)
    val monthlyRecords: StateFlow<Resource<MilkRecordsResponse>?> = _monthlyRecords.asStateFlow()

    fun loadMonthlyRecords(year: Int, month: Int) {
        viewModelScope.launch {
            val yearMonth = YearMonth.of(year, month)
            val startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            val endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE)

            milkRepository.getMilkRecords(startDate, endDate, 1, 100).collect { result ->
                _monthlyRecords.value = result
            }
        }
    }
}
