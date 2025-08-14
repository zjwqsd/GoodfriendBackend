package com.goodfriend.backend.repository

import com.goodfriend.backend.data.TestResult
import com.goodfriend.backend.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TestResultRepository : JpaRepository<TestResult, Long> {
    // 直接使用 User 对象作为查询参数
    @Query("SELECT tr FROM TestResult tr WHERE tr.user = :user ORDER BY tr.createdAt DESC")
    fun findByUserOrderByCreatedAtDesc(@Param("user") user: User): List<TestResult>
}