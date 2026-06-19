package com.flightapp.backend.users

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserIdentityRepository : JpaRepository<UserIdentity, UUID> {

    @EntityGraph(attributePaths = ["user"])
    fun findByProviderAndProviderUserId(
        provider: String,
        providerUserId: String
    ): UserIdentity?
}