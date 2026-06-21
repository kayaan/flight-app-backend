package com.flightapp.backend

import com.flightapp.backend.config.FlightAppFrontendProperties
import com.flightapp.backend.storage.FlightStorageProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    FlightStorageProperties::class,
    FlightAppFrontendProperties::class
)
class FlightappBackendApplication

fun main(args: Array<String>) {
    runApplication<FlightappBackendApplication>(*args)
}