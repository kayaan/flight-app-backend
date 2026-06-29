package com.flightapp.backend.flights

import java.time.Instant
import java.time.LocalDate

data class FlightSyncPackageDto(
    val flight: SyncFlightDto,
    val stats: SyncFlightStatsDto,
    val igcFile: SyncIgcFileDto,
    val trackFile: SyncTrackFileDto
)

data class SyncFlightDto(
    val id: String,
    val fileName: String,
    val flightDate: LocalDate?,
    val pilot: String?,
    val glider: String?,
    val visibility: FlightVisibility,
    val importedAtUtc: Instant,
    val createdAtUtc: Instant,
    val updatedAtUtc: Instant
)

data class SyncFlightStatsDto(
    val flightId: String,
    val startIndex: Int,
    val endIndex: Int,
    val fixCount: Int,
    val startTimeSec: Int,
    val endTimeSec: Int,
    val durationSec: Int,
    val distanceM: Double,
    val minAltGpsM: Double,
    val maxAltGpsM: Double,
    val gainGpsM: Double,
    val minAltBaroM: Double,
    val maxAltBaroM: Double,
    val gainBaroM: Double
)

data class SyncIgcFileDto(
    val flightId: String,
    val fileName: String,
    val contentBase64: String,
    val sizeBytes: Long
)

data class SyncTrackFileDto(
    val flightId: String,
    val contentBase64: String,
    val sizeBytes: Long,
    val formatVersion: Int,
    val pointCount: Int
)