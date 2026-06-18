package com.flightapp.backend.users

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "app_users")
class AppUser(

    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,

    @Column(name = "email", nullable = false, unique = true, length = 320)
    var email: String,

    @Column(name = "display_name", length = 200)
    var displayName: String? = null,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "created_at_utc", nullable = false)
    var createdAtUtc: Instant,

    @Column(name = "last_login_at_utc")
    var lastLoginAtUtc: Instant? = null
)