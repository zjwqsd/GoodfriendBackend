package com.goodfriend.backend.repository

import com.goodfriend.backend.data.TestResult
import com.goodfriend.backend.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByPhone(phone: String): User? // ✅ 推荐使用 Kotlin 可空类型

    fun existsByPhone(phone: String): Boolean

//    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<TestResult>
}
