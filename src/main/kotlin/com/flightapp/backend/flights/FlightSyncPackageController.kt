package com.flightapp.backend.flights

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/flights")
class FlightSyncPackageController(
    private val flightSyncPackageService: FlightSyncPackageService
) {

    @GetMapping("/{id}/sync/package")
    fun getSyncPackage(
        @PathVariable id: String,
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): FlightSyncPackageDto {
        return flightSyncPackageService.getSyncPackage(id, oidcUser)
    }
}