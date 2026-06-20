package com.flightapp.backend.storage

interface FlightBlobStorage {

    fun save(
        blobName: String,
        content: ByteArray,
        contentType: String? = null
    ): String

    fun load(blobName: String): ByteArray

    fun delete(blobName: String)
}