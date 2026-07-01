package com.flightapp.backend.flights

import com.flightapp.backend.users.AppUser
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class FlightSyncEventService {

    /**
     * Stores all open SSE connections per user.
     *
     * Why per user?
     * - One user can have multiple browser tabs or devices open.
     * - All devices of the same user should receive sync events.
     * - Other users must not receive these events.
     *
     * Key: AppUser.id
     * Value: all open SSE connections for this user
     */
    private val emittersByUserId =
        ConcurrentHashMap<UUID, MutableSet<SseEmitter>>()

    /**
     * Called when a browser/device opens the SSE endpoint:
     *
     * GET /api/flights/sync/events
     *
     * The HTTP request stays open. The backend can later send events
     * to the browser over this open connection.
     */
    fun subscribe(user: AppUser): SseEmitter {
        /**
         * 0L means no timeout from SseEmitter itself.
         *
         * Note:
         * Browsers, proxies, servers, or networks can still close the connection.
         * That is normal and should be handled.
         */
        val emitter = SseEmitter(0L)

        /**
         * Get the existing emitter set for this user.
         * If none exists yet, create a thread-safe set.
         */
        val emitters = emittersByUserId.computeIfAbsent(user.id) {
            ConcurrentHashMap.newKeySet()
        }

        /**
         * Register this new connection for the user.
         */
        emitters.add(emitter)

        /**
         * If the browser closes the connection normally,
         * remove the emitter from the registry.
         */
        emitter.onCompletion {
            removeEmitter(user.id, emitter)
        }

        /**
         * If a timeout happens, remove the emitter as well.
         */
        emitter.onTimeout {
            removeEmitter(user.id, emitter)
        }

        /**
         * If a network or connection error happens,
         * remove the emitter as well.
         */
        emitter.onError {
            removeEmitter(user.id, emitter)
        }

        /**
         * Optional initial event.
         *
         * Benefit:
         * - We immediately see in the browser that the connection works.
         * - Useful for debugging.
         *
         * The frontend should not trigger a reload from this event.
         */
        try {
            emitter.send(
                SseEmitter.event()
                    .name("connected")
                    .data(
                        FlightSyncEventDto(
                            type = "connected",
                            flightId = null
                        )
                    )
            )
        } catch (_: IOException) {
            /**
             * If even the first send fails, the connection is unusable.
             */
            removeEmitter(user.id, emitter)
        }

        return emitter
    }

    /**
     * Called when a flight has changed on the backend.
     *
     * Examples:
     * - Upload from another device
     * - Remote delete
     * - Visibility change
     *
     * All open tabs/devices of the same user receive the event.
     */
    fun notifyFlightChanged(
        user: AppUser,
        flightId: String,
        type: String
    ) {
        val emitters = emittersByUserId[user.id] ?: return

        /**
         * Collect broken connections first.
         * We do not remove them while iterating over the set.
         */
        val deadEmitters = mutableListOf<SseEmitter>()

        /**
         * Payload sent to the frontend.
         *
         * type examples:
         * - uploaded
         * - deleted
         * - visibilityChanged
         */
        val event = FlightSyncEventDto(
            type = type,
            flightId = flightId
        )

        for (emitter in emitters) {
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("flightChanged")
                        .data(event)
                )
            } catch (_: Exception) {
                /**
                 * If sending fails, the connection is probably dead.
                 * We remove it after the loop.
                 */
                deadEmitters.add(emitter)
            }
        }

        /**
         * Remove broken connections.
         */
        for (deadEmitter in deadEmitters) {
            removeEmitter(user.id, deadEmitter)
        }
    }

    /**
     * Removes one SSE connection from the user's connection set.
     * If the user has no open connections left, remove the map entry too.
     */
    private fun removeEmitter(
        userId: UUID,
        emitter: SseEmitter
    ) {
        val emitters = emittersByUserId[userId] ?: return

        emitters.remove(emitter)

        if (emitters.isEmpty()) {
            emittersByUserId.remove(userId)
        }
    }
}

/**
 * Small DTO for SSE events.
 *
 * It is serialized as JSON inside the SSE data field.
 */
data class FlightSyncEventDto(
    val type: String,
    val flightId: String?
)