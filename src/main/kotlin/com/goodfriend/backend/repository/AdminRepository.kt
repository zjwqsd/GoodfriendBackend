package com.goodfriend.backend.repository

import com.goodfriend.backend.data.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByUsername(username: String): Admin?
}
