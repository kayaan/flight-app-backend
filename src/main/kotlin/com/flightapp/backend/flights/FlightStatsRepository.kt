package com.flightapp.backend.flights

import org.springframework.data.jpa.repository.JpaRepository

interface FlightStatsRepository : JpaRepository<FlightStats, String> {

    fun findByFlightId(flightId: String): FlightStats?
}