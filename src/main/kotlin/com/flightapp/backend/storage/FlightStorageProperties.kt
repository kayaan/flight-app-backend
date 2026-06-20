package com.flightapp.backend.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "flightapp.storage")
data class FlightStorageProperties(
    val connectionString: String,
    val containerName: String
)