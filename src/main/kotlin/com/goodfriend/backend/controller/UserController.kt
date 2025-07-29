package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.*
import com.goodfriend.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(userService.register(request))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(userService.login(request))
    }

    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") token: String): ResponseEntity<UserResponse> {
        // 实际应用中应从token解析用户ID
        // 这里简化为直接使用服务层方法
        val userId = 1L // 伪代码，应从token获取
        return ResponseEntity.ok(userService.getUserProfile(userId))
    }

    @PutMapping("/me")
    fun updateCurrentUser(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserResponse> {
        val userId = 1L // 伪代码，应从token获取
        return ResponseEntity.ok(userService.updateUserProfile(userId, request))
    }
}