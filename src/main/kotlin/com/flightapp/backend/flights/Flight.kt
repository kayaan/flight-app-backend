package com.flightapp.backend.flights

import com.flightapp.backend.users.AppUser
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "flights")
class Flight(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: AppUser,

    @Column(name = "file_name", nullable = false, length = 500)
    var fileName: String,

    @Column(name = "file_hash", nullable = false, length = 128)
    var fileHash: String,

    @Column(name = "flight_date")
    var flightDate: LocalDate? = null,

    @Column(name = "pilot", length = 200)
    var pilot: String? = null,

    @Column(name = "glider", length = 200)
    var glider: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 30)
    var visibility: FlightVisibility = FlightVisibility.PRIVATE,

    @Column(name = "imported_at_utc", nullable = false)
    var importedAtUtc: Instant,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant,

    @Column(name = "updated_at_utc", nullable = false)
    var updatedAtUtc: Instant,

    @Column(name = "deleted_at_utc")
    var deletedAtUtc: Instant? = null
)