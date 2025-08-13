package com.goodfriend.backend.repository

import com.goodfriend.backend.data.StaticResource
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaticResourceRepository : JpaRepository<StaticResource, Long> {

    fun findByValid(valid: Boolean): List<StaticResource>

    fun findByScopeAndCategoryAndFilename(scope: String, category: String, filename: String): StaticResource?

//    fun findByValidFalseAndCreatedAtBefore(time: LocalDateTime): List<StaticResource>

    fun findByScopeAndCategoryAndValid(scope: String, category: String, valid: Boolean): List<StaticResource>
}
