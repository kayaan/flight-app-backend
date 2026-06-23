package com.flightapp.backend.flights

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "flight_files")
class FlightFile(

    @Id
    @Column(name = "flight_id", nullable = false, length = 64)
    var flightId: String,

    @Column(name = "igc_blob_name", nullable = false)
    var igcBlobName: String,

    @Column(name = "file_name", nullable = false, length = 500)
    var fileName: String,

    @Column(name = "file_size_bytes", nullable = false)
    var fileSizeBytes: Long,

    @Column(name = "content_hash", nullable = false, length = 64)
    var contentHash: String,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant,

    @Column(name = "updated_at_utc", nullable = false)
    var updatedAtUtc: Instant
)