package com.flightapp.backend.flights

import java.time.Instant
import java.time.LocalDate

data class FlightDto(
    val id: String,
    val fileName: String,
    val flightDate: LocalDate?,
    val pilot: String?,
    val glider: String?,
    val visibility: FlightVisibility,
    val importedAtUtc: Instant,
    val createdAtUtc: Instant,
    val updatedAtUtc: Instant
) {
    companion object {
        fun from(flight: Flight): FlightDto {
            return FlightDto(
                id = flight.id,
                fileName = flight.fileName,
                flightDate = flight.flightDate,
                pilot = flight.pilot,
                glider = flight.glider,
                visibility = flight.visibility,
                importedAtUtc = flight.importedAtUtc,
                createdAtUtc = flight.createdAtUtc,
                updatedAtUtc = flight.updatedAtUtc
            )
        }
    }
}