package com.flightapp.backend.storage

class BlobNotFoundException(
    blobName: String
) : RuntimeException("Blob not found: $blobName")