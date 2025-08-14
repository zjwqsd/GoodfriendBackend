package com.goodfriend.backend.data

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "appointments",
    indexes = [
        Index(name = "idx_appointments_consultant_time", columnList = "consultant_id,start_time,end_time"),
        Index(name = "idx_appointments_user", columnList = "user_id")
    ]
)
data class Appointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", nullable = false)
    val consultant: Consultant,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AppointmentStatus = AppointmentStatus.PENDING,

    @Column(columnDefinition = "TEXT")
    var note: String? = null,

    @Column(name = "created_at", updatable = false, nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class AppointmentStatus {
    PENDING,      // 待确认（默认）
    CONFIRMED,    // 已确认（可留待后台或咨询师端确认）
    CANCELLED     // 已取消
}