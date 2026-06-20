package com.flightapp.backend.common

import java.time.Instant

data class ApiErrorResponse(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String?
)