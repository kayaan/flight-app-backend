package com.flightapp.backend.flights

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "flight_files")
class FlightFile(

    @Id
    @Column(name = "flight_id", nullable = false, length = 64)
    var flightId: String,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "flight_id", nullable = false)
    var flight: Flight,

    @Column(name = "original_igc_blob_name")
    var originalIgcBlobName: String? = null,

    @Column(name = "file_size_bytes")
    var fileSizeBytes: Long? = null,

    @Column(name = "content_hash", length = 64)
    var contentHash: String? = null,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant,

    @Column(name = "updated_at_utc", nullable = false)
    var updatedAtUtc: Instant
)