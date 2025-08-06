package com.goodfriend.backend.dto

import com.goodfriend.backend.security.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class AdminLoginRequest(
    @field:NotBlank(message = "管理员用户名不能为空")
    val username: String,

    @field:NotBlank(message = "管理员密码不能为空")
    val password: String
)

data class RegisterRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

    @field:NotBlank(message = "密码不能为空")
    val password: String
)

data class LoginRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

    @field:NotBlank(message = "验证码不能为空")
    val code: String,

    @field:NotNull(message = "角色不能为空")
    val role: Role
)