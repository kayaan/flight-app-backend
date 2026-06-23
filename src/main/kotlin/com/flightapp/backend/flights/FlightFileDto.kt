package com.flightapp.backend.flights

import java.time.Instant

data class FlightFileDto(
    val flightId: String,
    val originalIgcBlobName: String?,
    val fileSizeBytes: Long?,
    val contentHash: String?,
    val createdAtUtc: Instant,
    val updatedAtUtc: Instant
) {
    companion object {
        fun from(file: FlightFile): FlightFileDto {
            return FlightFileDto(
                flightId = file.flightId,
                originalIgcBlobName = file.originalIgcBlobName,
                fileSizeBytes = file.fileSizeBytes,
                contentHash = file.contentHash,
                createdAtUtc = file.createdAtUtc,
                updatedAtUtc = file.updatedAtUtc
            )
        }
    }
}