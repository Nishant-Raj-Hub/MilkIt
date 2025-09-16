package com.milkit.app.data.network

import com.milkit.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication endpoints
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("auth/profile")
    suspend fun getProfile(): Response<ApiResponse<User>>
    
    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<User>>
    
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<String>>
    
    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<String>>
    
    // Milk records endpoints
    @POST("milk/add")
    suspend fun addMilkRecord(@Body request: AddMilkRecordRequest): Response<ApiResponse<MilkRecord>>
    
    @GET("milk/get")
    suspend fun getMilkRecords(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MilkRecordsResponse>
    
    @GET("milk/today")
    suspend fun getTodayRecord(): Response<ApiResponse<MilkRecord>>
    
    @PUT("milk/confirm/{id}")
    suspend fun confirmRecord(@Path("id") id: String): Response<ApiResponse<MilkRecord>>
    
    @DELETE("milk/{id}")
    suspend fun deleteRecord(@Path("id") id: String): Response<ApiResponse<MilkRecord>>
    
    @GET("milk/monthly-stats/{year}/{month}")
    suspend fun getMonthlyStats(
        @Path("year") year: Int,
        @Path("month") month: Int
    ): Response<MonthlyStatsResponse>
    
    // Export endpoints
    @GET("export/records")
    suspend fun exportRecords(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("format") format: String = "text"
    ): Response<String>
    
    @GET("export/share-link")
    suspend fun getShareLink(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<ShareData>>
}

data class ApiResponse<T>(
    val message: String? = null,
    val user: T? = null,
    val record: T? = null,
    val data: T? = null,
    val error: String? = null,
    val details: Any? = null
)

data class ShareData(
    val summary: String,
    val shareText: String,
    val filename: String
)
