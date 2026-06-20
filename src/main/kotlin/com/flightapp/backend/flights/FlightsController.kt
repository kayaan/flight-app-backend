package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/flights")
class FlightsController(
    private val flightRepository: FlightRepository,
    private val currentUserService: CurrentUserService,
    private val flightFileRepository: FlightFileRepository
) {
    @GetMapping
    fun getFlights(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): List<FlightDto> {
        val user = currentUserService.requireCurrentUser(oidcUser)

        return flightRepository.findByUserAndDeletedAtUtcIsNullOrderByImportedAtUtcDesc(user)
            .map { FlightDto.from(it) }
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @Valid @RequestBody request: CreateFlightRequest
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


    @GetMapping("/{id}")
    fun getFlight(
        @AuthenticationPrincipal oidcUser: OidcUser,
        @PathVariable id: UUID
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = id,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        return FlightDto.from(flight)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID
    ) {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = id,
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

    @PatchMapping("/{id}/visibility")
    fun updateVisibility(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFlightVisibilityRequest
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = id,
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

    @PostMapping("/{id}/file")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateFlightFileRequest
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = id,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val existingFile = flightFileRepository.findByFlightId(id)

        if (existingFile != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Flight file already exists"
            )
        }

        val file = FlightFile(
            id = UUID.randomUUID(),
            flight = flight,
            originalIgcBlobName = request.originalIgcBlobName,
            trackCacheBlobName = request.trackCacheBlobName,
            fileSizeBytes = request.fileSizeBytes,
            contentHash = request.contentHash,
            createdAtUtc = Instant.now()
        )

        val saved = flightFileRepository.save(file)

        return FlightFileDto.from(saved)
    }

    @GetMapping("/{id}/file")
    fun getFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = id,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val file = flightFileRepository.findByFlightId(flight.id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found"
            )

        return FlightFileDto.from(file)
    }

    @PatchMapping("/{id}/file")
    fun updateFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFlightFileRequest
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = id,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val file = flightFileRepository.findByFlightId(flight.id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found"
            )

        request.originalIgcBlobName?.let {
            file.originalIgcBlobName = it
        }

        request.trackCacheBlobName?.let {
            file.trackCacheBlobName = it
        }

        request.fileSizeBytes?.let {
            file.fileSizeBytes = it
        }

        request.contentHash?.let {
            file.contentHash = it
        }

        val saved = flightFileRepository.save(file)

        return FlightFileDto.from(saved)
    }
}