package com.goodfriend.backend.repository

import com.goodfriend.backend.data.User
import com.goodfriend.backend.data.Wish
import com.goodfriend.backend.data.WishLike
import org.springframework.data.jpa.repository.JpaRepository

interface WishLikeRepository : JpaRepository<WishLike, Long> {
    fun existsByWishAndUser(wish: Wish, user: User): Boolean
    fun countByWish(wish: Wish): Long
    fun deleteByWishAndUser(wish: Wish, user: User): Long
    fun deleteByWish(wish: Wish): Long
}
