package com.flightapp.backend.flights

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class CreateFlightRequest(
    @field:NotBlank
    @field:Size(max = 255)
    val fileName: String,

    @field:NotBlank
    @field:Size(max = 64)
    val fileHash: String,

    val flightDate: LocalDate? = null,

    @field:Size(max = 255)
    val pilot: String? = null,

    @field:Size(max = 255)
    val glider: String? = null,

    val visibility: FlightVisibility = FlightVisibility.PRIVATE
)