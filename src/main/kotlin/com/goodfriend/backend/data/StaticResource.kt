package com.goodfriend.backend.data

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class StaticResource(
    @Id @GeneratedValue val id: Long = 0,

    val scope: String,         // 作用对象：user / consultant / app
    val category: String,      // 类型：avatars / cover 等
    val filename: String,      // 文件名：default.jpg

    val description: String? = null, // 可选描述
    val valid: Boolean = false,      // 是否为有效资源（被引用）

    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun getPathSuffix(): String = "$scope/$category/$filename"
}
