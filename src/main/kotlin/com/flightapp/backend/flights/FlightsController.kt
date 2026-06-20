package com.flightapp.backend.flights

import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/flights")
class FlightsController(
    private val flightService: FlightService,
    private val flightFileService: FlightFileService
) {

    @GetMapping
    fun getFlights(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): List<FlightDto> {
        return flightService.getFlights(oidcUser)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @Valid @RequestBody request: CreateFlightRequest
    ): FlightDto {
        return flightService.createFlight(
            oidcUser = oidcUser,
            request = request
        )
    }

    @GetMapping("/{id}")
    fun getFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID
    ): FlightDto {
        return flightService.getFlight(
            oidcUser = oidcUser,
            flightId = id
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID
    ) {
        flightService.deleteFlight(
            oidcUser = oidcUser,
            flightId = id
        )
    }

    @PatchMapping("/{id}/visibility")
    fun updateVisibility(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFlightVisibilityRequest
    ): FlightDto {
        return flightService.updateVisibility(
            oidcUser = oidcUser,
            flightId = id,
            request = request
        )
    }

    @PostMapping("/{id}/file")
    @ResponseStatus(HttpStatus.CREATED)
    fun createFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: CreateFlightFileRequest
    ): FlightFileDto {
        return flightFileService.createFlightFile(
            oidcUser = oidcUser,
            flightId = id,
            request = request
        )
    }

    @GetMapping("/{id}/file")
    fun getFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID
    ): FlightFileDto {
        return flightFileService.getFlightFile(
            oidcUser = oidcUser,
            flightId = id
        )
    }

    @PatchMapping("/{id}/file")
    fun updateFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFlightFileRequest
    ): FlightFileDto {
        return flightFileService.updateFlightFile(
            oidcUser = oidcUser,
            flightId = id,
            request = request
        )
    }

    @PostMapping(
        "/{id}/igc",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadOriginalIgc(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID,
        @RequestParam("file") file: MultipartFile
    ): FlightFileDto {
        return flightFileService.uploadOriginalIgc(
            oidcUser = oidcUser,
            flightId = id,
            file = file
        )
    }

    @GetMapping("/{id}/igc")
    fun downloadOriginalIgc(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: UUID
    ): ResponseEntity<ByteArray> {
        val download = flightFileService.downloadOriginalIgc(
            oidcUser = oidcUser,
            flightId = id
        )

        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"${download.fileName}\""
            )
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(download.bytes.size.toLong())
            .body(download.bytes)
    }
}