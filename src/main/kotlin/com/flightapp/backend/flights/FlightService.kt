package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import org.springframework.transaction.annotation.Transactional

@Service
class FlightService(
    private val flightRepository: FlightRepository,
    private val currentUserService: CurrentUserService,
    private val flightSyncEventService: FlightSyncEventService
) {

    fun getFlights(oidcUser: OidcUser?): List<FlightDto> {
        val user = currentUserService.requireCurrentUser(oidcUser)

        return flightRepository
            .findByUserAndDeletedAtUtcIsNullOrderByImportedAtUtcDesc(user)
            .map { FlightDto.from(it) }
    }

    fun getFlight(
        oidcUser: OidcUser?,
        flightId: String
    ): FlightDto {
        val flight = requireOwnedFlight(oidcUser, flightId)

        return FlightDto.from(flight)
    }

    @Transactional
    fun deleteFlight(
        oidcUser: OidcUser?,
        flightId: String
    ) {
        val flight = requireOwnedFlight(oidcUser, flightId)

        val now = Instant.now()
        flight.deletedAtUtc = now
        flight.updatedAtUtc = now

        val saved =  flightRepository.save(flight)

        flightSyncEventService.notifyFlightChanged(
            user = saved.user,
            flightId = saved.id,
            type = "deleted"
        )
    }

    @Transactional
    fun updateVisibility(
        oidcUser: OidcUser?,
        flightId: String,
        visibility: FlightVisibility
    ): FlightDto {
        val flight = requireOwnedFlight(oidcUser, flightId)

        flight.visibility = visibility
        flight.updatedAtUtc = Instant.now()

        val saved = flightRepository.save(flight)

        flightSyncEventService.notifyFlightChanged(
            user = saved.user,
            flightId = saved.id,
            type = "visibilityChanged"
        )

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