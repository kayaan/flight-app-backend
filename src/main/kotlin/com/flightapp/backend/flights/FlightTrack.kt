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
@Table(name = "flight_tracks")
class FlightTrack(

    @Id
    @Column(name = "flight_id", nullable = false, length = 64)
    var flightId: String,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "flight_id", nullable = false)
    var flight: Flight,

    @Column(name = "track_blob_name", nullable = false)
    var trackBlobName: String,

    @Column(name = "format_version", nullable = false)
    var formatVersion: Int,

    @Column(name = "point_count", nullable = false)
    var pointCount: Int,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant,

    @Column(name = "updated_at_utc", nullable = false)
    var updatedAtUtc: Instant
)