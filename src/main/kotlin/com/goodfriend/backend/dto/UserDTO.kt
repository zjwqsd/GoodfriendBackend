package com.goodfriend.backend.dto

import com.goodfriend.backend.data.Gender
import jakarta.validation.constraints.*

data class RegisterRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 20, message = "密码长度需在6-20字符之间")
    val password: String,

    @field:NotBlank(message = "姓名不能为空")
    val name: String,

    @field:Min(value = 1, message = "年龄必须大于0")
    @field:Max(value = 150, message = "年龄不能超过150")
    val age: Int,

    val gender: Gender,

    @field:NotBlank(message = "地域不能为空")
    val region: String
)

data class LoginRequest(
    @field:NotBlank val phone: String,
    @field:NotBlank val password: String
)

data class UserResponse(
    val id: Long,
    val phone: String,
    val name: String,
    val age: Int,
    val gender: Gender,
    val region: String
)

data class AuthResponse(
    val token: String,
    val user: UserResponse
)

data class UpdateUserRequest(
    val name: String?,
    val age: Int?,
    val gender: Gender?,
    val region: String?
)