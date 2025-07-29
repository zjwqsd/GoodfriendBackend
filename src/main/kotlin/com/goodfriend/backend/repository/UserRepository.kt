package com.goodfriend.backend.repository

import com.goodfriend.backend.data.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByPhone(phone: String): Optional<User>
    fun existsByPhone(phone: String): Boolean
}