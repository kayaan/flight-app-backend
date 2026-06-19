package com.flightapp.backend.auth

import com.flightapp.backend.users.UserDto
import com.flightapp.backend.users.UserIdentityRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/me")
class MeController(
    private val identities: UserIdentityRepository
) {

    @GetMapping
    fun me(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): UserDto {
        if (oidcUser == null) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated")
        }

        val provider = "google"
        val providerUserId = oidcUser.subject
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing OIDC subject")

        val identity = identities.findByProviderAndProviderUserId(
            provider = provider,
            providerUserId = providerUserId
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "FlightApp user identity not found"
        )

        return UserDto.from(identity.user)
    }
}