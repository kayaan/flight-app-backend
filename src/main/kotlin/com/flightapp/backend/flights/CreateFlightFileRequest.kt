package com.flightapp.backend.flights

import jakarta.validation.constraints.Size

data class CreateFlightFileRequest(
    @field:Size(max = 500)
    val originalIgcBlobName: String? = null,

    val fileSizeBytes: Long? = null,

    @field:Size(max = 128)
    val contentHash: String? = null
)