package com.flightapp.backend.common

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController {

    @GetMapping("/")
    fun root(): Map<String, String> {
        return mapOf(
            "application" to "flightapp-backend",
            "status" to "running"
        )
    }
}