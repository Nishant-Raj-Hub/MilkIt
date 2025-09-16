package com.milkit.app.presentation.main.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.milkit.app.data.network.ShareData
import com.milkit.app.data.repository.ShareRepository
import com.milkit.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareRepository: ShareRepository
) : ViewModel() {

    private val _exportState = MutableStateFlow<Resource<String>?>(null)
    val exportState: StateFlow<Resource<String>?> = _exportState.asStateFlow()

    private val _shareState = MutableStateFlow<Resource<ShareData>?>(null)
    val shareState: StateFlow<Resource<ShareData>?> = _shareState.asStateFlow()

    fun exportData(startDate: String, endDate: String, format: ExportFormat) {
        viewModelScope.launch {
            shareRepository.exportRecords(startDate, endDate, format.apiFormat).collect { result ->
                _exportState.value = result
            }
        }
    }

    fun generateShareText(startDate: String, endDate: String) {
        viewModelScope.launch {
            shareRepository.getShareLink(startDate, endDate).collect { result ->
                _shareState.value = result
            }
        }
    }

    fun clearExportState() {
        _exportState.value = null
    }

    fun clearShareState() {
        _shareState.value = null
    }
}
