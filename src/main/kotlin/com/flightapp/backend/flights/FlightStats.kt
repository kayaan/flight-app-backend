package com.flightapp.backend.flights

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "flight_stats")
class FlightStats(

    @Id
    @Column(name = "flight_id", nullable = false, length = 64)
    var flightId: String,

    @Column(name = "start_index", nullable = false)
    var startIndex: Int,

    @Column(name = "end_index", nullable = false)
    var endIndex: Int,

    @Column(name = "fix_count", nullable = false)
    var fixCount: Int,

    @Column(name = "start_time_sec", nullable = false)
    var startTimeSec: Int,

    @Column(name = "end_time_sec", nullable = false)
    var endTimeSec: Int,

    @Column(name = "duration_sec", nullable = false)
    var durationSec: Int,

    @Column(name = "distance_m", nullable = false)
    var distanceM: Double,

    @Column(name = "min_alt_gps_m", nullable = false)
    var minAltGpsM: Double,

    @Column(name = "max_alt_gps_m", nullable = false)
    var maxAltGpsM: Double,

    @Column(name = "gain_gps_m", nullable = false)
    var gainGpsM: Double,

    @Column(name = "min_alt_baro_m", nullable = false)
    var minAltBaroM: Double,

    @Column(name = "max_alt_baro_m", nullable = false)
    var maxAltBaroM: Double,

    @Column(name = "gain_baro_m", nullable = false)
    var gainBaroM: Double,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant,

    @Column(name = "updated_at_utc", nullable = false)
    var updatedAtUtc: Instant
)