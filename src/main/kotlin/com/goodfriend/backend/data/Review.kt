package com.goodfriend.backend.data

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "reviews",
    indexes = [
        Index(name = "idx_reviews_consultant", columnList = "consultant_id,created_at"),
        Index(name = "idx_reviews_user", columnList = "user_id")
    ],
    uniqueConstraints = [
        // 一个预约只能被评价一次（如果你允许不基于预约的评论，可将此约束去掉）
        UniqueConstraint(name = "uk_review_appointment", columnNames = ["appointment_id"])
    ]
)
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", nullable = false)
    val consultant: Consultant,

    // 可选：基于预约的评价更可靠
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    val appointment: Appointment? = null,

    @Column(nullable = false)
    var rating: Int,                    // 1~5

    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    // 直接沿用你已有的字符串列表转换器，写入 TEXT
    @Convert(converter = StringListConverter::class)
    @Column(columnDefinition = "TEXT")
    var tags: List<String> = emptyList(),

    @Column(name = "created_at", updatable = false, nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
