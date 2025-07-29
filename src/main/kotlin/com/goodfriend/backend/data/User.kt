package com.goodfriend.backend.data

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String, // 手机号通常不变，保持为 val

    @field:NotBlank(message = "密码不能为空")
    var password: String, // 密码可能需要修改，改为 var

    @field:NotBlank(message = "姓名不能为空")
    var name: String, // 姓名可以修改，改为 var

    @field:Min(value = 1, message = "年龄必须大于0")
    @field:Max(value = 150, message = "年龄不能超过150")
    var age: Int, // 年龄可以修改，改为 var

    @Enumerated(EnumType.STRING)
    var gender: Gender, // 性别可以修改，改为 var

    @field:NotBlank(message = "地域不能为空")
    var region: String, // 地域可以修改，改为 var

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(), // 创建时间不变，保持 val

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now() // 更新时间需要修改，改为 var
)

enum class Gender {
    MALE, FEMALE, UNKNOWN
}