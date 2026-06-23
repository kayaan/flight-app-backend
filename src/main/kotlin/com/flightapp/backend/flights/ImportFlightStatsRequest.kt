package com.flightapp.backend.flights

import jakarta.validation.constraints.Min

data class ImportFlightStatsRequest(

    @field:Min(0)
    val startIndex: Int,

    @field:Min(0)
    val endIndex: Int,

    @field:Min(0)
    val fixCount: Int,

    val startTimeSec: Int,
    val endTimeSec: Int,

    @field:Min(0)
    val durationSec: Int,

    @field:Min(0)
    val distanceM: Double,

    val minAltGpsM: Double,
    val maxAltGpsM: Double,
    val gainGpsM: Double,

    val minAltBaroM: Double,
    val maxAltBaroM: Double,
    val gainBaroM: Double
)