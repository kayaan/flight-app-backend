package com.flightapp.backend.flights

data class OriginalIgcDownload(
    val fileName: String,
    val bytes: ByteArray
)