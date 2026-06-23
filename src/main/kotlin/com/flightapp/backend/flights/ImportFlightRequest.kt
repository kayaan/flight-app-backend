package com.flightapp.backend.flights

import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant
import java.time.LocalDate

data class ImportFlightRequest(

    @field:Pattern(regexp = "^[a-f0-9]{64}$")
    val id: String? = null,

    @field:Size(max = 500)
    val fileName: String? = null,

    val flightDate: LocalDate? = null,

    @field:Size(max = 200)
    val pilot: String? = null,

    @field:Size(max = 200)
    val glider: String? = null,

    val importedAtUtc: Instant? = null,

    @field:Valid
    val stats: ImportFlightStatsRequest? = null
)