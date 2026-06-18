package com.flightapp.backend.users

import java.time.Instant
import java.util.UUID

data class UserDto(
    val id: UUID,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val createdAtUtc: Instant,
    val lastLoginAtUtc: Instant?
) {
    companion object {
        fun from(user: AppUser): UserDto {
            return UserDto(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                createdAtUtc = user.createdAtUtc,
                lastLoginAtUtc = user.lastLoginAtUtc
            )
        }
    }
}