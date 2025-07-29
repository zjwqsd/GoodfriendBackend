package com.goodfriend.backend.service

import com.goodfriend.backend.data.User
import com.goodfriend.backend.dto.*
import com.goodfriend.backend.exception.ResourceNotFoundException
import com.goodfriend.backend.repository.UserRepository
import com.goodfriend.backend.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByPhone(request.phone)) {
            throw IllegalArgumentException("手机号已被注册")
        }

        val user = User(
            phone = request.phone,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            age = request.age,
            gender = request.gender,
            region = request.region
        )

        val savedUser = userRepository.save(user)
        return createAuthResponse(savedUser)
    }

    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByPhone(request.phone)
            .orElseThrow { IllegalArgumentException("用户不存在") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("密码错误")
        }

        return createAuthResponse(user)
    }

    fun getUserProfile(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("用户不存在") }
        return convertToResponse(user)
    }

    fun updateUserProfile(userId: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("用户不存在") }

        request.name?.let { user.name = it }
        request.age?.let { user.age = it }
        request.gender?.let { user.gender = it }
        request.region?.let { user.region = it }
        user.updatedAt = LocalDateTime.now()

        val updatedUser = userRepository.save(user)
        return convertToResponse(updatedUser)
    }

    private fun createAuthResponse(user: User): AuthResponse {
        val token = jwtTokenProvider.generateToken(user.id.toString())
        return AuthResponse(token, convertToResponse(user))
    }

    private fun convertToResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id,
            phone = user.phone,
            name = user.name,
            age = user.age,
            gender = user.gender,
            region = user.region
        )
    }
}