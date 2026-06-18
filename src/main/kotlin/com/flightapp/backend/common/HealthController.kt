package com.flightapp.backend.common

import com.nimbusds.jose.util.health.HealthStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.concurrent.timer
import java.time.Instant

@RestController
@RequestMapping("/api/health")
class HealthController {

    @GetMapping
    fun health() : HealthResponse {
        return HealthResponse(
            status = "ok",
            application = "flightapp-backend",
            timestampUtc = Instant.now().toString()
        )
    }

}

data class HealthResponse(
    val status: String,
    val application: String,
    val timestampUtc: String
)