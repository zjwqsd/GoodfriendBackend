package com.goodfriend.backend.repository

import com.goodfriend.backend.data.User
import com.goodfriend.backend.data.WishInboxState
import org.springframework.data.jpa.repository.JpaRepository

interface WishInboxStateRepository : JpaRepository<WishInboxState, Long> {
    fun findByUser(user: User): WishInboxState?
}
