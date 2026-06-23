package com.flightapp.backend.flights

import org.springframework.data.jpa.repository.JpaRepository

interface FlightTrackRepository : JpaRepository<FlightTrack, String> {

    fun findByFlightId(flightId: String): FlightTrack?
}