package com.goodfriend.backend.data

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "wishes", indexes = [Index(name = "idx_wish_created_at", columnList = "created_at DESC")])
class Wish(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "wish_images", joinColumns = [JoinColumn(name = "wish_id")])
    @Column(name = "image_path", nullable = false)
    var images: MutableList<String> = mutableListOf(),

    @Column(name = "anonymous", nullable = false)
    var anonymous: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
