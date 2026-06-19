package com.flightapp.backend.users

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_identities")
class UserIdentity(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: AppUser,

    @Column(name = "provider", nullable = false, length = 50)
    var provider: String,

    @Column(name = "provider_user_id", nullable = false, length = 255)
    var providerUserId: String,

    @Column(name = "email", length = 320)
    var email: String? = null,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant
)