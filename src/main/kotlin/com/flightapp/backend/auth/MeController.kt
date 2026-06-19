package com.flightapp.backend.auth

import com.flightapp.backend.users.UserDto
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/me")
class MeController(
    private val currentUserService: CurrentUserService
) {

    @GetMapping
    fun me(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): UserDto {
        
        val user = currentUserService.requireCurrentUser(oidcUser)

        return UserDto.from(user)
    }
}