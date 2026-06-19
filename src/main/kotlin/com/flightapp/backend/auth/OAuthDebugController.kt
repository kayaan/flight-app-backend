package com.flightapp.backend.auth

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class OAuthDebugController {

    @GetMapping("/api/oauth/failure")
    fun failure(): Map<String, String> {
        return mapOf(
            "status" to "oauth_failed",
            "message" to "OAuth login failed. Check Google redirect URI, client id, client secret and backend logs."
        )
    }
}