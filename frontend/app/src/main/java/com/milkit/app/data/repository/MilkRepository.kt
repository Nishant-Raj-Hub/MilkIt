package com.milkit.app.data.repository

import com.milkit.app.data.model.*
import com.milkit.app.data.network.ApiService
import com.milkit.app.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilkRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getTodayRecord(): Flow<Resource<MilkRecord>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getTodayRecord()
            
            if (response.isSuccessful) {
                val recordResponse = response.body()
                if (recordResponse?.record != null) {
                    emit(Resource.Success(recordResponse.record))
                } else {
                    emit(Resource.Error("Failed to get today's record: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to get today's record: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun getMilkRecords(
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 1,
        limit: Int = 50
    ): Flow<Resource<MilkRecordsResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getMilkRecords(startDate, endDate, page, limit)
            
            if (response.isSuccessful) {
                val recordsResponse = response.body()
                if (recordsResponse != null) {
                    emit(Resource.Success(recordsResponse))
                } else {
                    emit(Resource.Error("Failed to get records: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to get records: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun addMilkRecord(
        date: String,
        liters: Double,
        status: MilkStatus,
        milkType: MilkType,
        notes: String? = null
    ): Flow<Resource<MilkRecord>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = AddMilkRecordRequest(
                date = date,
                liters = liters,
                status = status.toApiString(),
                milkType = milkType.toApiString(),
                notes = notes
            )
            
            val response = apiService.addMilkRecord(request)
            
            if (response.isSuccessful) {
                val recordResponse = response.body()
                if (recordResponse?.record != null) {
                    emit(Resource.Success(recordResponse.record))
                } else {
                    emit(Resource.Error("Failed to add record: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to add record: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun confirmRecord(recordId: String): Flow<Resource<MilkRecord>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.confirmRecord(recordId)
            
            if (response.isSuccessful) {
                val recordResponse = response.body()
                if (recordResponse?.record != null) {
                    emit(Resource.Success(recordResponse.record))
                } else {
                    emit(Resource.Error("Failed to confirm record: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to confirm record: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun deleteRecord(recordId: String): Flow<Resource<MilkRecord>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.deleteRecord(recordId)
            
            if (response.isSuccessful) {
                val recordResponse = response.body()
                if (recordResponse?.record != null) {
                    emit(Resource.Success(recordResponse.record))
                } else {
                    emit(Resource.Error("Failed to delete record: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to delete record: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }

    suspend fun getMonthlyStats(year: Int, month: Int): Flow<Resource<MonthlyStatsResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val response = apiService.getMonthlyStats(year, month)
            
            if (response.isSuccessful) {
                val statsResponse = response.body()
                if (statsResponse != null) {
                    emit(Resource.Success(statsResponse))
                } else {
                    emit(Resource.Error("Failed to get monthly stats: Empty response"))
                }
            } else {
                emit(Resource.Error("Failed to get monthly stats: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Network error: ${e.localizedMessage}"))
        }
    }
}
