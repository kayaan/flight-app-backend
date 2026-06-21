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
import java.util.UUID

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
        flightId: UUID,
        request: CreateFlightFileRequest
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val existingFile = flightFileRepository.findByFlightId(flightId)

        if (existingFile != null) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Flight file already exists"
            )
        }

        val file = FlightFile(
            id = UUID.randomUUID(),
            flight = flight,
            originalIgcBlobName = request.originalIgcBlobName,
            fileSizeBytes = request.fileSizeBytes,
            contentHash = request.contentHash,
            createdAtUtc = Instant.now()
        )

        val saved = flightFileRepository.save(file)

        return FlightFileDto.from(saved)
    }

    fun getFlightFile(
        oidcUser: OidcUser?,
        flightId: UUID
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val file = flightFileRepository.findByFlightId(flight.id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found"
            )

        return FlightFileDto.from(file)
    }

    fun updateFlightFile(
        oidcUser: OidcUser?,
        flightId: UUID,
        request: UpdateFlightFileRequest
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val file = flightFileRepository.findByFlightId(flight.id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found"
            )

        request.originalIgcBlobName?.let {
            file.originalIgcBlobName = it
        }

        request.fileSizeBytes?.let {
            file.fileSizeBytes = it
        }

        request.contentHash?.let {
            file.contentHash = it
        }

        val saved = flightFileRepository.save(file)

        return FlightFileDto.from(saved)
    }

    fun uploadOriginalIgc(
        oidcUser: OidcUser?,
        flightId: UUID,
        file: MultipartFile
    ): FlightFileDto {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val fileBytes = file.bytes

        validateIgcUpload(file, fileBytes)

        val contentHash = sha256Hex(fileBytes)
        val blobName = "flights/${flight.id}/original.igc"

        flightBlobStorage.save(
            blobName = blobName,
            content = fileBytes,
            contentType = "application/octet-stream"
        )

        val existingFile = flightFileRepository.findByFlightId(flight.id)

        val flightFile = if (existingFile == null) {
            FlightFile(
                id = UUID.randomUUID(),
                flight = flight,
                originalIgcBlobName = blobName,
                fileSizeBytes = file.size,
                contentHash = contentHash,
                createdAtUtc = Instant.now()
            )
        } else {
            existingFile.originalIgcBlobName = blobName
            existingFile.fileSizeBytes = file.size
            existingFile.contentHash = contentHash
            existingFile
        }

        val saved = flightFileRepository.save(flightFile)

        return FlightFileDto.from(saved)
    }

    fun downloadOriginalIgc(
        oidcUser: OidcUser?,
        flightId: UUID
    ): OriginalIgcDownload {
        val user = currentUserService.requireCurrentUser(oidcUser)

        val flight = flightRepository.findByIdAndUserAndDeletedAtUtcIsNull(
            id = flightId,
            user = user
        ) ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Flight not found"
        )

        val flightFile = flightFileRepository.findByFlightId(flight.id)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Flight file not found"
            )

        val originalIgcBlobName = flightFile.originalIgcBlobName
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Original IGC file not found"
            )

        val bytes = flightBlobStorage.load(originalIgcBlobName)

        return OriginalIgcDownload(
            fileName = flight.fileName,
            bytes = bytes
        )
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(bytes)

        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun validateIgcUpload(file: MultipartFile, fileBytes: ByteArray) {
        if (file.isEmpty) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Uploaded file is empty"
            )
        }

        if (file.size > MAX_IGC_FILE_SIZE_BYTES) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "IGC file is too large. Maximum size is 10 MB"
            )
        }

        val originalFileName = file.originalFilename
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing file name"
            )

        if (!originalFileName.lowercase().endsWith(".igc")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only .igc files are allowed"
            )
        }

        val text = String(fileBytes, StandardCharsets.UTF_8)

        val nonEmptyLines = text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val hasARecord = nonEmptyLines.any { it.startsWith("A") }
        val hasBRecord = nonEmptyLines.any { it.startsWith("B") }

        if (!hasARecord || !hasBRecord) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "File does not look like a valid IGC file"
            )
        }
    }
}