package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class FlightService(
    private val flightRepository: FlightRepository,
    private val currentUserService: CurrentUserService
) {

    fun getFlights(oidcUser: OidcUser?): List<FlightDto> {
        val user = currentUserService.requireCurrentUser(oidcUser)

        return flightRepository
            .findByUserAndDeletedAtUtcIsNullOrderByImportedAtUtcDesc(user)
            .map { FlightDto.from(it) }
    }

    fun createFlight(
        oidcUser: OidcUser?,
        request: CreateFlightRequest
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val duplicateExists = flightRepository
            .existsByUserAndFileHashAndDeletedAtUtcIsNull(
                user = user,
                fileHash = request.fileHash
            )

        if (duplicateExists) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Flight with same file hash already exists"
            )
        }

        val now = Instant.now()

        val flight = Flight(
            id = UUID.randomUUID(),
            user = user,
            fileName = request.fileName,
            fileHash = request.fileHash,
            flightDate = request.flightDate,
            pilot = request.pilot,
            glider = request.glider,
            visibility = request.visibility,
            importedAtUtc = now,
            createdAtUtc = now,
            updatedAtUtc = now
        )

        val saved = flightRepository.save(flight)

        return FlightDto.from(saved)
    }

    fun getFlight(
        oidcUser: OidcUser?,
        flightId: UUID
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        return FlightDto.from(flight)
    }

    fun deleteFlight(
        oidcUser: OidcUser?,
        flightId: UUID
    ) {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val now = Instant.now()

        flight.deletedAtUtc = now
        flight.updatedAtUtc = now

        flightRepository.save(flight)
    }

    fun updateVisibility(
        oidcUser: OidcUser?,
        flightId: UUID,
        request: UpdateFlightVisibilityRequest
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        flight.visibility = request.visibility
        flight.updatedAtUtc = Instant.now()

        val saved = flightRepository.save(flight)

        return FlightDto.from(saved)
    }
}