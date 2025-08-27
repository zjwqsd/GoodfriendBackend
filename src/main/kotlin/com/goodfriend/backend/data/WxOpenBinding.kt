package com.goodfriend.backend.data

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "wx_open_binding",
    uniqueConstraints = [UniqueConstraint(columnNames = ["openid"])]
)
data class WxOpenBinding(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val openid: String,

    val unionid: String? = null,

    @Column(nullable = false)
    var userId: Long,

    var sessionKey: String? = null,

    var updatedAt: Instant = Instant.now()
)
