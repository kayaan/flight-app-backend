package com.flightapp.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlightappBackendApplication

fun main(args: Array<String>) {
	runApplication<FlightappBackendApplication>(*args)
}
