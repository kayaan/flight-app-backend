package com.flightapp.backend.flights

import com.flightapp.backend.users.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface FlightRepository : JpaRepository<Flight, UUID> {

    fun findByUserAndDeletedAtUtcIsNullOrderByImportedAtUtcDesc(
        user: AppUser
    ): List<Flight>

    fun findByIdAndUserAndDeletedAtUtcIsNull(
        id: UUID,
        user: AppUser
    ): Flight?

    fun existsByUserAndFileHashAndDeletedAtUtcIsNull(
        user: AppUser,
        fileHash: String
    ): Boolean
}