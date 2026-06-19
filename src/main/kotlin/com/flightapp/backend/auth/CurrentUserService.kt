package com.flightapp.backend.auth

import com.flightapp.backend.users.AppUser
import com.flightapp.backend.users.UserIdentityRepository
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class CurrentUserService
    (
    private val identities: UserIdentityRepository
) {
    @Transactional(readOnly = true)
    fun requireCurrentUser(oidcUser: OidcUser?): AppUser {
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
        
        return identity.user
    }
}