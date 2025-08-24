package com.goodfriend.backend.repository

import com.goodfriend.backend.data.User
import com.goodfriend.backend.data.Wish
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface WishRepository : JpaRepository<Wish, Long> {
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<Wish>
    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): List<Wish>
    //fun countByCreatedAtAfterAndUserNot(createdAt: LocalDateTime, user: User): Long
    fun countByCreatedAtAfterAndUserNot(createdAt: LocalDateTime, user: com.goodfriend.backend.data.User): Long
    @Modifying
    @Query("update Wish w set w.quoteWish = null where w.quoteWish = :target")
    fun clearQuotesOf(@Param("target") target: Wish): Int
}
