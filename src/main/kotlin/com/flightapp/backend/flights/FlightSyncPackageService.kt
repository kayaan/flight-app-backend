package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import com.flightapp.backend.storage.FlightBlobStorage
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.Base64

@Service
class FlightSyncPackageService(
    private val currentUserService: CurrentUserService,
    private val flightRepository: FlightRepository,
    private val flightStatsRepository: FlightStatsRepository,
    private val flightFileRepository: FlightFileRepository,
    private val flightTrackRepository: FlightTrackRepository,
    private val flightBlobStorage: FlightBlobStorage
) {

    @Transactional(readOnly = true)
    fun getSyncPackage(
        flightId: String,
        oidcUser: OidcUser?
    ): FlightSyncPackageDto {
        val currentUser = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = currentUser
        ) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found")

        val stats = flightStatsRepository.findByFlightId(flightId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flight stats not found")

        val file = flightFileRepository.findByFlightId(flightId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flight file not found")

        val track = flightTrackRepository.findByFlightId(flightId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Flight track not found")

        val igcBytes = flightBlobStorage.load(file.igcBlobName)
        val trackJsonBytes = flightBlobStorage.load(track.trackBlobName)

        return FlightSyncPackageDto(
            flight = SyncFlightDto(
                id = flight.id,
                fileName = flight.fileName,
                flightDate = flight.flightDate,
                pilot = flight.pilot,
                glider = flight.glider,
                visibility = flight.visibility,
                importedAtUtc = flight.importedAtUtc,
                createdAtUtc = flight.createdAtUtc,
                updatedAtUtc = flight.updatedAtUtc
            ),
            stats = SyncFlightStatsDto(
                flightId = stats.flightId,
                startIndex = stats.startIndex,
                endIndex = stats.endIndex,
                fixCount = stats.fixCount,
                startTimeSec = stats.startTimeSec,
                endTimeSec = stats.endTimeSec,
                durationSec = stats.durationSec,
                distanceM = stats.distanceM,
                minAltGpsM = stats.minAltGpsM,
                maxAltGpsM = stats.maxAltGpsM,
                gainGpsM = stats.gainGpsM,
                minAltBaroM = stats.minAltBaroM,
                maxAltBaroM = stats.maxAltBaroM,
                gainBaroM = stats.gainBaroM
            ),
            igcFile = SyncIgcFileDto(
                flightId = file.flightId,
                fileName = file.fileName,
                contentBase64 = Base64.getEncoder().encodeToString(igcBytes),
                sizeBytes = file.fileSizeBytes
            ),
            trackFile = SyncTrackFileDto(
                flightId = track.flightId,
                contentBase64 = Base64.getEncoder().encodeToString(trackJsonBytes),
                sizeBytes = trackJsonBytes.size.toLong(),
                formatVersion = track.formatVersion,
                pointCount = track.pointCount
            )
        )
    }
}