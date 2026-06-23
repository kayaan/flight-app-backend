package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import com.flightapp.backend.storage.FlightBlobStorage
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
    private val flightFileRepository: FlightFileRepository,
    private val flightStatsRepository: FlightStatsRepository,
    private val flightBlobStorage: FlightBlobStorage,
    private val currentUserService: CurrentUserService
) {

    companion object {
        private const val MAX_IGC_FILE_SIZE_BYTES = 10L * 1024L * 1024L
    }

    @Transactional
    fun importFlight(
        oidcUser: OidcUser?,
        metadata: ImportFlightRequest,
        file: MultipartFile
    ): FlightDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        validateIgcFile(file)

        val bytes = file.bytes
        val text = bytes.toString(StandardCharsets.UTF_8)

        validateIgcContent(text)

        val contentHash = sha256Hex(bytes)

        if (metadata.id != null && metadata.id != contentHash) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Metadata id does not match uploaded IGC hash."
            )
        }

        val now = Instant.now()
        val flightId = contentHash
        val blobName = "flights/$flightId/original.igc"

        flightBlobStorage.save(
            blobName = blobName,
            content = bytes
        )

        try {
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

            val savedFlight = flightRepository.save(flight)

            saveOrUpdateFlightFile(
                flight = savedFlight,
                blobName = blobName,
                contentHash = contentHash,
                fileSizeBytes = bytes.size.toLong(),
                now = now
            )

            val stats = metadata.stats
            if (stats != null) {
                saveOrUpdateFlightStats(
                    flight = savedFlight,
                    request = stats,
                    now = now
                )
            }

            return FlightDto.from(savedFlight)
        } catch (exception: Exception) {
            flightBlobStorage.delete(blobName)
            throw exception
        }
    }

    private fun saveOrUpdateFlightFile(
        flight: Flight,
        blobName: String,
        contentHash: String,
        fileSizeBytes: Long,
        now: Instant
    ) {
        val flightFile = flightFileRepository.findByFlightId(flight.id)
            ?: FlightFile(
                flightId = flight.id,
                flight = flight,
                originalIgcBlobName = null,
                fileSizeBytes = null,
                contentHash = null,
                createdAtUtc = now,
                updatedAtUtc = now
            )

        flightFile.originalIgcBlobName = blobName
        flightFile.fileSizeBytes = fileSizeBytes
        flightFile.contentHash = contentHash
        flightFile.updatedAtUtc = now

        flightFileRepository.save(flightFile)
    }

    private fun saveOrUpdateFlightStats(
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
                flight = flight,
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
                avgSpeedKmh = request.avgSpeedKmh,
                maxSpeedKmh = request.maxSpeedKmh,
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
        stats.avgSpeedKmh = request.avgSpeedKmh
        stats.maxSpeedKmh = request.maxSpeedKmh
        stats.updatedAtUtc = now

        flightStatsRepository.save(stats)
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