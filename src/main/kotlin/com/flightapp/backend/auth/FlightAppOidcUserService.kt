package com.flightapp.backend.auth

import com.flightapp.backend.users.AppUser
import com.flightapp.backend.users.AppUserRepository
import com.flightapp.backend.users.UserIdentity
import com.flightapp.backend.users.UserIdentityRepository
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class FlightAppOidcUserService(
    private val users: AppUserRepository,
    private val identities: UserIdentityRepository
) : OAuth2UserService<OidcUserRequest, OidcUser> {

    private val defaultOidcUserService = OidcUserService()

    @Transactional
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oidcUser = defaultOidcUserService.loadUser(userRequest)

        val provider = userRequest.clientRegistration.registrationId
        val providerUserId = oidcUser.subject
            ?: throw OAuth2AuthenticationException("Missing OIDC subject")

        val email = oidcUser.email
        val displayName = oidcUser.fullName
            ?: oidcUser.givenName
            ?: email
            ?: "Unknown User"

        val avatarUrl = oidcUser.picture
        val now = Instant.now()

        val existingIdentity = identities.findByProviderAndProviderUserId(
            provider = provider,
            providerUserId = providerUserId
        )

        if (existingIdentity != null) {
            val user = existingIdentity.user
            user.email = email ?: user.email
            user.displayName = displayName
            user.avatarUrl = avatarUrl
            user.lastLoginAtUtc = now
            users.save(user)

            return oidcUser
        }

        val existingUserByEmail =
            if (email != null) users.findByEmail(email) else null

        val user = existingUserByEmail ?: users.save(
            AppUser(
                id = UUID.randomUUID(),
                email = email ?: "$provider:$providerUserId",
                displayName = displayName,
                avatarUrl = avatarUrl,
                createdAtUtc = now,
                lastLoginAtUtc = now
            )
        )

        if (existingUserByEmail != null) {
            existingUserByEmail.displayName = displayName
            existingUserByEmail.avatarUrl = avatarUrl
            existingUserByEmail.lastLoginAtUtc = now
            users.save(existingUserByEmail)
        }

        identities.save(
            UserIdentity(
                id = UUID.randomUUID(),
                user = user,
                provider = provider,
                providerUserId = providerUserId,
                email = email,
                createdAtUtc = now
            )
        )

        return oidcUser
    }
}