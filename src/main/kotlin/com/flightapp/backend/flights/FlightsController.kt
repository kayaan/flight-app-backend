package com.flightapp.backend.flights

import jakarta.validation.Valid
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/flights")
class FlightsController(
    private val flightService: FlightService,
    private val flightFileService: FlightFileService,
    private val flightImportService: FlightImportService
) {

    @GetMapping
    fun getFlights(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): List<FlightDto> {
        return flightService.getFlights(oidcUser)
    }

    @PostMapping(
        "/import",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun importFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @Valid @RequestPart("metadata") metadata: ImportFlightRequest,
        @RequestPart("file") file: MultipartFile
    ): FlightDto {
        return flightImportService.importFlight(
            oidcUser = oidcUser,
            metadata = metadata,
            file = file
        )
    }

    @GetMapping("/{id}")
    fun getFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: String
    ): FlightDto {
        return flightService.getFlight(
            oidcUser = oidcUser,
            flightId = id
        )
    }

    @DeleteMapping("/{id}")
    fun deleteFlight(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: String
    ) {
        flightService.deleteFlight(
            oidcUser = oidcUser,
            flightId = id
        )
    }

    @PatchMapping("/{id}/visibility")
    fun updateVisibility(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: String,
        @Valid @RequestBody request: UpdateFlightVisibilityRequest
    ): FlightDto {
        return flightService.updateVisibility(
            oidcUser = oidcUser,
            flightId = id,
            visibility = request.visibility
        )
    }

    @GetMapping("/{id}/file")
    fun getFlightFile(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: String
    ): FlightFileDto {
        return flightFileService.getFlightFile(
            oidcUser = oidcUser,
            flightId = id
        )
    }

    @GetMapping("/{id}/igc")
    fun downloadOriginalIgc(
        @AuthenticationPrincipal oidcUser: OidcUser?,
        @PathVariable id: String
    ): ResponseEntity<ByteArrayResource> {
        val content = flightFileService.downloadOriginalIgc(
            oidcUser = oidcUser,
            flightId = id
        )

        val resource = ByteArrayResource(content)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(content.size.toLong())
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("$id.igc")
                    .build()
                    .toString()
            )
            .body(resource)
    }
}