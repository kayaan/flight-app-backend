package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/flights/sync")
class FlightSyncEventsController(
    private val currentUserService: CurrentUserService,
    private val flightSyncEventService: FlightSyncEventService
) {

    /**
     * SSE endpoint for live sync events.
     *
     * Angular will later open:
     *
     * new EventSource('/api/flights/sync/events', { withCredentials: true })
     *
     * The request stays open.
     * Whenever backend flights change, the backend sends events over this connection.
     */
    @GetMapping(
        "/events",
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    fun events(
        @AuthenticationPrincipal oidcUser: OidcUser?
    ): SseEmitter {
        /**
         * Important:
         * We do not subscribe globally. We subscribe the currently logged-in user.
         *
         * This prevents user A from receiving events that belong to user B.
         */
        val user = currentUserService.requireCurrentUser(oidcUser)

        /**
         * Register and return the open SSE connection.
         */
        return flightSyncEventService.subscribe(user)
    }
}