package com.flightapp.backend.flights

import com.flightapp.backend.users.AppUser
import org.springframework.data.jpa.repository.JpaRepository

interface FlightRepository : JpaRepository<Flight, String> {

    fun findByUserAndDeletedAtUtcIsNullOrderByImportedAtUtcDesc(
        user: AppUser
    ): List<Flight>

    fun findByIdAndUserAndDeletedAtUtcIsNull(
        id: String,
        user: AppUser
    ): Flight?

    fun existsByIdAndUserAndDeletedAtUtcIsNull(
        id: String,
        user: AppUser
    ): Boolean
}