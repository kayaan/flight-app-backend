package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

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

        if (flightRepository.existsByIdAndUserAndDeletedAtUtcIsNull(request.id, user)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Flight already exists."
            )
        }

        val now = Instant.now()

        val flight = Flight(
            id = request.id,
            user = user,
            fileName = request.fileName,
            flightDate = request.flightDate,
            pilot = request.pilot,
            glider = request.glider,
            visibility = FlightVisibility.PRIVATE,
            importedAtUtc = request.importedAtUtc ?: now,
            createdAtUtc = now,
            updatedAtUtc = now,
            deletedAtUtc = null
        )

        val saved = flightRepository.save(flight)

        return FlightDto.from(saved)
    }

    fun getFlight(
        oidcUser: OidcUser?,
        flightId: String
    ): FlightDto {
        val flight = requireOwnedFlight(oidcUser, flightId)

        return FlightDto.from(flight)
    }

    fun deleteFlight(
        oidcUser: OidcUser?,
        flightId: String
    ) {
        val flight = requireOwnedFlight(oidcUser, flightId)

        val now = Instant.now()
        flight.deletedAtUtc = now
        flight.updatedAtUtc = now

        flightRepository.save(flight)
    }

    fun updateVisibility(
        oidcUser: OidcUser?,
        flightId: String,
        visibility: FlightVisibility
    ): FlightDto {
        val flight = requireOwnedFlight(oidcUser, flightId)

        flight.visibility = visibility
        flight.updatedAtUtc = Instant.now()

        val saved = flightRepository.save(flight)

        return FlightDto.from(saved)
    }

    fun requireOwnedFlight(
        oidcUser: OidcUser?,
        flightId: String
    ): Flight {
        val user = currentUserService.requireCurrentUser(oidcUser)

        return flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found."
        )
    }
}