package com.flightapp.backend.flights

import com.flightapp.backend.auth.CurrentUserService
import com.flightapp.backend.storage.FlightBlobStorage
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant

@Service
class FlightFileService(
    private val flightRepository: FlightRepository,
    private val flightFileRepository: FlightFileRepository,
    private val flightBlobStorage: FlightBlobStorage,
    private val currentUserService: CurrentUserService
) {

    companion object {
        private const val MAX_IGC_FILE_SIZE_BYTES = 10L * 1024L * 1024L
    }

    fun createFlightFile(
        oidcUser: OidcUser?,
        flightId: String
    ): FlightFileDto {
        val flight = requireOwnedFlight(oidcUser, flightId)

        val existing = flightFileRepository.findByFlightId(flightId)
        if (existing != null) {
            return FlightFileDto.from(existing)
        }

        val now = Instant.now()

        val flightFile = FlightFile(
            flightId = flight.id,
            flight = flight,
            originalIgcBlobName = null,
            fileSizeBytes = null,
            contentHash = null,
            createdAtUtc = now,
            updatedAtUtc = now
        )

        val saved = flightFileRepository.save(flightFile)

        return FlightFileDto.from(saved)
    }

    fun getFlightFile(
        oidcUser: OidcUser?,
        flightId: String
    ): FlightFileDto {
        requireOwnedFlight(oidcUser, flightId)

        val flightFile = flightFileRepository.findByFlightId(flightId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found."
            )

        return FlightFileDto.from(flightFile)
    }

    fun uploadOriginalIgc(
        oidcUser: OidcUser?,
        flightId: String,
        file: MultipartFile
    ): FlightFileDto {
        val flight = requireOwnedFlight(oidcUser, flightId)

        validateIgcFile(file)

        val bytes = file.bytes
        val text = bytes.toString(StandardCharsets.UTF_8)

        validateIgcContent(text)

        val contentHash = sha256Hex(bytes)

        if (contentHash != flight.id) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Uploaded IGC hash does not match flight id."
            )
        }

        val blobName = "flights/${flight.id}/original.igc"

        flightBlobStorage.save(
            blobName = blobName,
            content = bytes
        )

        val now = Instant.now()

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
        flightFile.fileSizeBytes = bytes.size.toLong()
        flightFile.contentHash = contentHash
        flightFile.updatedAtUtc = now

        val saved = flightFileRepository.save(flightFile)

        return FlightFileDto.from(saved)
    }

    fun downloadOriginalIgc(
        oidcUser: OidcUser?,
        flightId: String
    ): ByteArray {
        requireOwnedFlight(oidcUser, flightId)

        val flightFile = flightFileRepository.findByFlightId(flightId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found."
            )

        val blobName = flightFile.originalIgcBlobName
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Original IGC file not uploaded."
            )

        return flightBlobStorage.load(blobName)
    }

    fun deleteOriginalIgc(
        oidcUser: OidcUser?,
        flightId: String
    ): FlightFileDto {
        requireOwnedFlight(oidcUser, flightId)

        val flightFile = flightFileRepository.findByFlightId(flightId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found."
            )

        val blobName = flightFile.originalIgcBlobName

        if (blobName != null) {
            flightBlobStorage.delete(blobName)
        }

        flightFile.originalIgcBlobName = null
        flightFile.fileSizeBytes = null
        flightFile.contentHash = null
        flightFile.updatedAtUtc = Instant.now()

        val saved = flightFileRepository.save(flightFile)

        return FlightFileDto.from(saved)
    }

    private fun requireOwnedFlight(
        oidcUser: OidcUser?,
        flightId: String
    ): Flight {
        val user = currentUserService.requireCurrentUser(oidcUser)

        return flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found."
        )
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