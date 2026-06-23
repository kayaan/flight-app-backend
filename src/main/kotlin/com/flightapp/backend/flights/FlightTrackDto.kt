package com.flightapp.backend.flights

import java.time.Instant

data class FlightTrackDto(
    val flightId: String,
    val trackBlobName: String,
    val formatVersion: Int,
    val pointCount: Int,
    val createdAtUtc: Instant,
    val updatedAtUtc: Instant
) {
    companion object {
        fun from(track: FlightTrack): FlightTrackDto {
            return FlightTrackDto(
                flightId = track.flightId,
                trackBlobName = track.trackBlobName,
                formatVersion = track.formatVersion,
                pointCount = track.pointCount,
                createdAtUtc = track.createdAtUtc,
                updatedAtUtc = track.updatedAtUtc
            )
        }
    }
}