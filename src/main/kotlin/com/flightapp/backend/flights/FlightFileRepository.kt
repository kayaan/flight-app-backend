package com.flightapp.backend.flights

import org.springframework.data.jpa.repository.JpaRepository

interface FlightFileRepository : JpaRepository<FlightFile, String> {

    fun findByFlightId(flightId: String): FlightFile?
}