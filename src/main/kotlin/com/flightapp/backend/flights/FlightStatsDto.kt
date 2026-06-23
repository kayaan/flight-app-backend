package com.flightapp.backend.flights

import java.time.Instant

data class FlightStatsDto(
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
    val gainBaroM: Double,

    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,

    val createdAtUtc: Instant,
    val updatedAtUtc: Instant
) {
    companion object {
        fun from(stats: FlightStats): FlightStatsDto {
            return FlightStatsDto(
                flightId = stats.flightId,

                startIndex = stats.startIndex,
                endIndex = stats.endIndex,
                fixCount = stats.fixCount,

                startTimeSec = stats.startTimeSec,
                endTimeSec = stats.endTimeSec,
                durationSec = stats.durationSec,

                distanceM = stats.distanceM,

                minAltGpsM = stats.minAltGpsM,
                maxAltGpsM = stats.maxAltGpsM,
                gainGpsM = stats.gainGpsM,

                minAltBaroM = stats.minAltBaroM,
                maxAltBaroM = stats.maxAltBaroM,
                gainBaroM = stats.gainBaroM,

                avgSpeedKmh = stats.avgSpeedKmh,
                maxSpeedKmh = stats.maxSpeedKmh,

                createdAtUtc = stats.createdAtUtc,
                updatedAtUtc = stats.updatedAtUtc
            )
        }
    }
}