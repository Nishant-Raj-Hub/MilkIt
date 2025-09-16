package com.milkit.app.data.repository

import com.milkit.app.data.network.ApiService
import com.milkit.app.data.network.ShareData
import com.milkit.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun exportRecords(
        startDate: String? = null,
        endDate: String? = null,
        format: String = "text"
    ): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.exportRecords(startDate, endDate, format)
            
            if (response.isSuccessful) {
                val exportData = response.body()
                if (exportData != null) {
                    emit(Resource.Success(exportData))
                } else {
                    emit(Resource.Error("Failed to export records: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to export records: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun getShareLink(
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Resource<ShareData>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getShareLink(startDate, endDate)
            
            if (response.isSuccessful) {
                val shareResponse = response.body()
                if (shareResponse?.data != null) {
                    emit(Resource.Success(shareResponse.data))
                } else {
                    emit(Resource.Error("Failed to generate share link: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to generate share link: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }
}
