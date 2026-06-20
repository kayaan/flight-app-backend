package com.flightapp.backend.flights

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FlightFileRepository : JpaRepository<FlightFile, UUID> {

    fun findByFlightId(flightId: UUID): FlightFile?
}