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
    var  phone: String, // 手机号通常不变，保持为 val

    @field:NotBlank(message = "密码不能为空")
    @Column(nullable = false)
    var password: String,

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

@Entity
@Table(name = "consultants")
data class Consultant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:NotBlank(message = "姓名不能为空")
    var name: String,

    @Column(unique = true, nullable = false)
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

    @field:NotBlank(message = "密码不能为空")
    @Column(nullable = false)
    var password: String,

    @field:NotBlank(message = "级别不能为空")
    var level: String, // 如：初级咨询师、资深咨询师等

    @field:NotBlank(message = "专长不能为空")
    var specialty: String, // 情感关系、职场压力等

    @Enumerated(EnumType.STRING)
    var gender: Gender, // MALE, FEMALE, UNKNOWN

    @field:NotBlank(message = "地点不能为空")
    var location: String, // 地区，如“上海”

    @field:DecimalMin(value = "0.0", inclusive = true, message = "评分不能小于0")
    @field:DecimalMax(value = "5.0", inclusive = true, message = "评分不能超过5")
    var rating: Double = 0.0, // 默认评分为0

    @field:NotBlank(message = "头像链接不能为空")
    var avatar: String = "/images/avatars/default.jpg",

    @field:Min(value = 0, message = "经验年限不能为负数")
    var experienceYears: Int, // 咨询经验年限

    @field:Min(value = 0, message = "咨询次数不能为负数")
    var consultationCount: Int, // 咨询次数

    @field:Min(value = 0, message = "每小时价格不能为负数")
    var pricePerHour: Int, // 每小时价格（单位：元）

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class Gender {
    MALE, FEMALE, UNKNOWN
}