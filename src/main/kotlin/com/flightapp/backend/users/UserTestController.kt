package com.flightapp.backend.users

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/test/users")
class UserTestController(
    private val users: AppUserRepository
) {

    @PostMapping
    fun createTestUser(): UserDto {
        val user = AppUser(
            id = UUID.randomUUID(),
            email = "test@example.com",
            displayName = "Test User",
            avatarUrl = null,
            createdAtUtc = Instant.now(),
            lastLoginAtUtc = null
        )

        val saved = users.save(user)

        return UserDto.from(saved)
    }

    @GetMapping
    fun getUsers(): List<UserDto> {
        return users.findAll().map { UserDto.from(it) }
    }
}