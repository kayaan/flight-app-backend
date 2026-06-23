package com.flightapp.backend.flights

import java.time.Instant

data class FlightFileDto(
    val flightId: String,
    val igcBlobName: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val contentHash: String,
    val createdAtUtc: Instant,
    val updatedAtUtc: Instant
) {
    companion object {
        fun from(file: FlightFile): FlightFileDto {
            return FlightFileDto(
                flightId = file.flightId,
                igcBlobName = file.igcBlobName,
                fileName = file.fileName,
                fileSizeBytes = file.fileSizeBytes,
                contentHash = file.contentHash,
                createdAtUtc = file.createdAtUtc,
                updatedAtUtc = file.updatedAtUtc
            )
        }
    }
}