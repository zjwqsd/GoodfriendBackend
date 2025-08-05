package com.goodfriend.backend.controller

import com.goodfriend.backend.security.Role
import com.goodfriend.backend.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.constraints.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/register")
    fun register(@RequestBody @Valid req: RegisterRequest): ResponseEntity<String> {
        val token = authService.registerUser(req.phone, req.password)
        return ResponseEntity.ok(token)
    }


    @PostMapping("/login")
    fun login(@RequestBody @Valid req: LoginRequest): ResponseEntity<String> {
        val token = authService.loginWithCode(req.phone, req.code, req.role)
        return ResponseEntity.ok(token)
    }
}


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
