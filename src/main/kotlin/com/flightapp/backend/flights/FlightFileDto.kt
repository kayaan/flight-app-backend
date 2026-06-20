package com.flightapp.backend.flights

import java.time.Instant
import java.util.UUID

data class FlightFileDto(
    val id: UUID,
    val flightId: UUID,
    val originalIgcBlobName: String?,
    val trackCacheBlobName: String?,
    val fileSizeBytes: Long?,
    val contentHash: String?,
    val createdAtUtc: Instant
) {
    companion object {
        fun from(file: FlightFile): FlightFileDto =
            FlightFileDto(
                id = file.id,
                flightId = file.flight.id,
                originalIgcBlobName = file.originalIgcBlobName,
                trackCacheBlobName = file.trackCacheBlobName,
                fileSizeBytes = file.fileSizeBytes,
                contentHash = file.contentHash,
                createdAtUtc = file.createdAtUtc
            )
    }
}