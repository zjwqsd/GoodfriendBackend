package com.goodfriend.backend.data

import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var phone: String = "",

    var password: String = "",

    var name: String = "开发用户",

    var age: Int = 18,

    @Enumerated(EnumType.STRING)
    var gender: Gender = Gender.UNKNOWN,

    var region: String = "未知",

    var avatar: String = "user/avatars/default.jpg",  // 相对路径
    var birthday: LocalDate? = null,
    var hobby: String? = null,


    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "consultants")
data class Consultant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var name: String = "开发咨询师",

    @Column(unique = true, nullable = false)
    var phone: String = "",

    var password: String = "",

    var level: String = "初级咨询师",

    var specialty: String = "情感关系",

    @Enumerated(EnumType.STRING)
    var gender: Gender = Gender.UNKNOWN,

    var location: String = "未知",

    var rating: Double = 0.0,

    var avatar: String = "/images/avatars/default.jpg",

    var experienceYears: Int = 0,

    var consultationCount: Int = 0,

    var pricePerHour: Int = 0,

    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)


enum class Gender {
    MALE, FEMALE, UNKNOWN
}