package com.flightapp.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "flightapp.frontend")
data class FlightAppFrontendProperties(
    val baseUrl: String
)