package com.milkit.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MilkRecord(
    val id: String,
    val userId: String,
    val date: String,
    val liters: Double,
    val status: MilkStatus,
    val isAutoMarked: Boolean,
    val notes: String? = null,
    val milkType: MilkType,
    val createdAt: String,
    val updatedAt: String
) : Parcelable

enum class MilkStatus {
    RECEIVED,
    NOT_RECEIVED,
    PARTIAL;

    companion object {
        fun fromString(value: String): MilkStatus {
            return when (value.lowercase()) {
                "received" -> RECEIVED
                "not_received" -> NOT_RECEIVED
                "partial" -> PARTIAL
                else -> RECEIVED
            }
        }
    }

    fun toApiString(): String {
        return when (this) {
            RECEIVED -> "received"
            NOT_RECEIVED -> "not_received"
            PARTIAL -> "partial"
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            RECEIVED -> "Received"
            NOT_RECEIVED -> "Not Received"
            PARTIAL -> "Partial"
        }
    }
}

enum class MilkType {
    COW,
    BUFFALO,
    PACKET,
    OTHER;

    companion object {
        fun fromString(value: String): MilkType {
            return when (value.lowercase()) {
                "cow" -> COW
                "buffalo" -> BUFFALO
                "packet" -> PACKET
                "other" -> OTHER
                else -> COW
            }
        }
    }

    fun toApiString(): String {
        return when (this) {
            COW -> "cow"
            BUFFALO -> "buffalo"
            PACKET -> "packet"
            OTHER -> "other"
        }
    }

    fun getDisplayName(): String {
        return when (this) {
            COW -> "Cow Milk"
            BUFFALO -> "Buffalo Milk"
            PACKET -> "Packet Milk"
            OTHER -> "Other"
        }
    }
}

@Parcelize
data class AddMilkRecordRequest(
    val date: String,
    val liters: Double,
    val status: String,
    val notes: String? = null,
    val milkType: String
) : Parcelable

@Parcelize
data class MilkRecordsResponse(
    val records: List<MilkRecord>,
    val pagination: Pagination,
    val statistics: MilkStatistics
) : Parcelable

@Parcelize
data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalRecords: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean
) : Parcelable

@Parcelize
data class MilkStatistics(
    val totalLiters: Double,
    val averageLiters: Double,
    val receivedCount: Int,
    val notReceivedCount: Int,
    val partialCount: Int,
    val autoMarkedCount: Int
) : Parcelable

@Parcelize
data class MonthlyStatsResponse(
    val year: Int,
    val month: Int,
    val dailyStats: List<DailyStat>,
    val monthlyOverview: MilkStatistics
) : Parcelable

@Parcelize
data class DailyStat(
    val day: Int,
    val statuses: List<StatusStat>,
    val dailyTotal: Double
) : Parcelable

@Parcelize
data class StatusStat(
    val status: String,
    val count: Int,
    val totalLiters: Double
) : Parcelable
