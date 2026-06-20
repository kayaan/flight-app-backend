package com.flightapp.backend.storage

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.models.BlobHttpHeaders
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream

@Service
class AzureFlightBlobStorage(
    properties: FlightStorageProperties
) : FlightBlobStorage {

    private val containerClient: BlobContainerClient =
        BlobServiceClientBuilder()
            .connectionString(properties.connectionString)
            .buildClient()
            .getBlobContainerClient(properties.containerName)

    init {
        containerClient.createIfNotExists()
    }

    override fun save(
        blobName: String,
        content: ByteArray,
        contentType: String?
    ): String {
        val blobClient = containerClient.getBlobClient(blobName)

        ByteArrayInputStream(content).use { input ->
            blobClient.upload(input, content.size.toLong(), true)
        }

        if (contentType != null) {
            blobClient.setHttpHeaders(
                BlobHttpHeaders().setContentType(contentType)
            )
        }

        return blobName
    }

    override fun load(blobName: String): ByteArray {
        val blobClient = containerClient.getBlobClient(blobName)

        if (!blobClient.exists()) {
            throw BlobNotFoundException(blobName)
        }

        val output = ByteArrayOutputStream()
        blobClient.downloadStream(output)

        return output.toByteArray()
    }

    override fun delete(blobName: String) {
        val blobClient = containerClient.getBlobClient(blobName)
        blobClient.deleteIfExists()
    }
}