package com.goodfriend.backend.repository

import com.goodfriend.backend.data.User
import com.goodfriend.backend.data.Wish
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface WishRepository : JpaRepository<Wish, Long> {
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): List<Wish>
    fun findByUserOrderByCreatedAtDesc(user: User, pageable: Pageable): List<Wish>
}
