package com.flightapp.backend.flights

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "flight_files")
class FlightFile(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    var flight: Flight,

    @Column(name = "original_igc_blob_name")
    var originalIgcBlobName: String? = null,

    @Column(name = "file_size_bytes")
    var fileSizeBytes: Long? = null,

    @Column(name = "content_hash", length = 128)
    var contentHash: String? = null,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant
)