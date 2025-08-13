package com.goodfriend.backend.repository

import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.ApplicationStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ConsultantApplicationRepository : JpaRepository<ConsultantApplication, Long> {
    fun existsByUserIdAndStatus(userId: Long, status: ApplicationStatus): Boolean
//    fun findAllByStatus(status: ApplicationStatus): List<ConsultantApplication>
//    fun findByUserId(userId: Long): List<ConsultantApplication>
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<ConsultantApplication>

}
