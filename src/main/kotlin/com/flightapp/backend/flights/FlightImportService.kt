package com.flightapp.backend.flights

import com.fasterxml.jackson.databind.ObjectMapper
import com.flightapp.backend.auth.CurrentUserService
import com.flightapp.backend.storage.FlightBlobStorage
import com.flightapp.backend.users.AppUser
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant

@Service
class FlightImportService(
    private val flightRepository: FlightRepository,
    private val flightStatsRepository: FlightStatsRepository,
    private val flightFileRepository: FlightFileRepository,
    private val flightTrackRepository: FlightTrackRepository,
    private val flightBlobStorage: FlightBlobStorage,
    private val currentUserService: CurrentUserService,
    private val flightSyncEventService: FlightSyncEventService
) {

    private val objectMapper = ObjectMapper()

    companion object {
        private const val MAX_IGC_FILE_SIZE_BYTES = 10L * 1024L * 1024L
        private const val TRACK_FORMAT_VERSION = 1
    }

    @Transactional
    fun importFlight(
        oidcUser: OidcUser?,
        metadata: ImportFlightRequest,
        file: MultipartFile
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        validateIgcFile(file)
        metadata.track.validateSameLength()

        val igcBytes = file.bytes
        val igcText = igcBytes.toString(StandardCharsets.UTF_8)

        validateIgcContent(igcText)

        val contentHash = sha256Hex(igcBytes)

        if (metadata.id != null && metadata.id != contentHash) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Metadata id does not match uploaded IGC hash."
            )
        }

        val now = Instant.now()
        val flightId = contentHash

        val igcBlobName = "flights/$flightId/original.igc"
        val trackBlobName = "flights/$flightId/track-v1.json"

        val trackJsonBytes = objectMapper.writeValueAsBytes(metadata.track)

        flightBlobStorage.save(
            blobName = igcBlobName,
            content = igcBytes
        )

        flightBlobStorage.save(
            blobName = trackBlobName,
            content = trackJsonBytes
        )

        try {
            val flight = saveOrUpdateFlight(
                user = user,
                flightId = flightId,
                metadata = metadata,
                file = file,
                now = now
            )

            saveOrUpdateStats(
                flight = flight,
                request = metadata.stats,
                now = now
            )

            saveOrUpdateFile(
                flight = flight,
                igcBlobName = igcBlobName,
                file = file,
                contentHash = contentHash,
                now = now
            )

            saveOrUpdateTrack(
                flight = flight,
                trackBlobName = trackBlobName,
                request = metadata.track,
                now = now
            )

            flightSyncEventService.notifyFlightChanged(
                user = user,
                flightId = flight.id,
                type = "uploaded"
            )

            return FlightDto.from(flight)
        } catch (exception: Exception) {
            flightBlobStorage.delete(igcBlobName)
            flightBlobStorage.delete(trackBlobName)
            throw exception
        }
    }

    private fun saveOrUpdateFlight(
        user: AppUser,
        flightId: String,
        metadata: ImportFlightRequest,
        file: MultipartFile,
        now: Instant
    ): Flight {
        val existingFlight = flightRepository
            .findByIdAndUserAndDeletedAtUtcIsNull(flightId, user)

        val flight = if (existingFlight != null) {
            existingFlight.apply {
                fileName = metadata.fileName ?: file.originalFilename ?: fileName
                flightDate = metadata.flightDate
                pilot = metadata.pilot
                glider = metadata.glider
                importedAtUtc = metadata.importedAtUtc ?: importedAtUtc
                updatedAtUtc = now
            }
        } else {
            Flight(
                id = flightId,
                user = user,
                fileName = metadata.fileName ?: file.originalFilename ?: "$flightId.igc",
                flightDate = metadata.flightDate,
                pilot = metadata.pilot,
                glider = metadata.glider,
                visibility = FlightVisibility.PRIVATE,
                importedAtUtc = metadata.importedAtUtc ?: now,
                createdAtUtc = now,
                updatedAtUtc = now,
                deletedAtUtc = null
            )
        }

        return flightRepository.save(flight)
    }

    private fun saveOrUpdateStats(
        flight: Flight,
        request: ImportFlightStatsRequest,
        now: Instant
    ) {
        if (request.endIndex < request.startIndex) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Stats endIndex must be greater than or equal to startIndex."
            )
        }

        val stats = flightStatsRepository.findByFlightId(flight.id)
            ?: FlightStats(
                flightId = flight.id,
                startIndex = request.startIndex,
                endIndex = request.endIndex,
                fixCount = request.fixCount,
                startTimeSec = request.startTimeSec,
                endTimeSec = request.endTimeSec,
                durationSec = request.durationSec,
                distanceM = request.distanceM,
                minAltGpsM = request.minAltGpsM,
                maxAltGpsM = request.maxAltGpsM,
                gainGpsM = request.gainGpsM,
                minAltBaroM = request.minAltBaroM,
                maxAltBaroM = request.maxAltBaroM,
                gainBaroM = request.gainBaroM,
                createdAtUtc = now,
                updatedAtUtc = now
            )

        stats.startIndex = request.startIndex
        stats.endIndex = request.endIndex
        stats.fixCount = request.fixCount
        stats.startTimeSec = request.startTimeSec
        stats.endTimeSec = request.endTimeSec
        stats.durationSec = request.durationSec
        stats.distanceM = request.distanceM
        stats.minAltGpsM = request.minAltGpsM
        stats.maxAltGpsM = request.maxAltGpsM
        stats.gainGpsM = request.gainGpsM
        stats.minAltBaroM = request.minAltBaroM
        stats.maxAltBaroM = request.maxAltBaroM
        stats.gainBaroM = request.gainBaroM
        stats.updatedAtUtc = now

        flightStatsRepository.save(stats)
    }

    private fun saveOrUpdateFile(
        flight: Flight,
        igcBlobName: String,
        file: MultipartFile,
        contentHash: String,
        now: Instant
    ) {
        val fileName = file.originalFilename ?: "${flight.id}.igc"

        val flightFile = flightFileRepository.findByFlightId(flight.id)
            ?: FlightFile(
                flightId = flight.id,
                igcBlobName = igcBlobName,
                fileName = fileName,
                fileSizeBytes = file.size,
                contentHash = contentHash,
                createdAtUtc = now,
                updatedAtUtc = now
            )

        flightFile.igcBlobName = igcBlobName
        flightFile.fileName = fileName
        flightFile.fileSizeBytes = file.size
        flightFile.contentHash = contentHash
        flightFile.updatedAtUtc = now

        flightFileRepository.save(flightFile)
    }

    private fun saveOrUpdateTrack(
        flight: Flight,
        trackBlobName: String,
        request: ImportFlightTrackRequest,
        now: Instant
    ) {
        val flightTrack = flightTrackRepository.findByFlightId(flight.id)
            ?: FlightTrack(
                flightId = flight.id,
                trackBlobName = trackBlobName,
                formatVersion = TRACK_FORMAT_VERSION,
                pointCount = request.pointCount(),
                createdAtUtc = now,
                updatedAtUtc = now
            )

        flightTrack.trackBlobName = trackBlobName
        flightTrack.formatVersion = request.formatVersion
        flightTrack.pointCount = request.pointCount()
        flightTrack.updatedAtUtc = now

        flightTrackRepository.save(flightTrack)
    }

    private fun validateIgcFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "IGC file is empty."
            )
        }

        if (file.size > MAX_IGC_FILE_SIZE_BYTES) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "IGC file is too large."
            )
        }

        val originalFilename = file.originalFilename ?: ""

        if (!originalFilename.lowercase().endsWith(".igc")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only .igc files are allowed."
            )
        }
    }

    private fun validateIgcContent(text: String) {
        val hasARecord = text.lineSequence().any { it.startsWith("A") }
        val hasBRecord = text.lineSequence().any { it.startsWith("B") }

        if (!hasARecord || !hasBRecord) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid IGC file."
            )
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(bytes)

        return digest.joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
    }
}