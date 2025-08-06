package com.goodfriend.backend.repository

import com.goodfriend.backend.data.StaticResource
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface StaticResourceRepository : JpaRepository<StaticResource, Long> {

    fun findByValid(valid: Boolean): List<StaticResource>

    fun findByScopeAndCategoryAndFilename(scope: String, category: String, filename: String): StaticResource?

    fun findByValidFalseAndCreatedAtBefore(time: LocalDateTime): List<StaticResource>
}
