package com.goodfriend.backend.data

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "wish_likes",
    uniqueConstraints = [UniqueConstraint(name = "uk_wish_user", columnNames = ["wish_id", "user_id"])],
    indexes = [Index(name = "idx_wish_like_wish", columnList = "wish_id")]
)
class WishLike(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wish_id")
    var wish: Wish,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
