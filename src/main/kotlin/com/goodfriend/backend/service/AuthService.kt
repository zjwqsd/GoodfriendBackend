package com.goodfriend.backend.service

import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.User
//import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.UserRepository
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.security.JwtTokenProvider
import com.goodfriend.backend.security.Role
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepo: UserRepository,
    private val consultantRepo: ConsultantRepository,
    private val jwtProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {

    @Value("\${app.admin.password}")
    private lateinit var adminPassword: String

    fun registerUser(phone: String, password: String): String {
        if (userRepo.existsByPhone(phone)) {
            throw ApiException(400, "手机号已注册")
        }
        val user = User(
            phone = phone,
            password = passwordEncoder.encode(password),
            name = "新用户",
            age = 18,
            gender = Gender.UNKNOWN,
            region = "未知"
        )
        userRepo.save(user)
        return jwtProvider.generateToken(user.id, Role.USER)
    }

    fun login(phone: String, password: String, role: Role): String {
        return when (role) {
            Role.USER -> {
                val user = userRepo.findByPhone(phone)
                    ?: throw ApiException(404, "用户不存在")
                if (!passwordEncoder.matches(password, user.password)) {
                    throw ApiException(401, "密码错误")
                }
                jwtProvider.generateToken(user.id, Role.USER)
            }

            Role.CONSULTANT -> {
                val consultant = consultantRepo.findByPhone(phone)
                    ?: throw ApiException(404, "咨询师不存在")
                if (!passwordEncoder.matches(password, consultant.password)) {
                    throw ApiException(401, "密码错误")
                }
                jwtProvider.generateToken(consultant.id, Role.CONSULTANT)
            }

            Role.ADMIN -> {
                throw ApiException(400, "请使用管理员专用登录接口")
            }
        }
    }

    fun loginWithCode(phone: String, code: String, role: Role): String {
        if (code != "123456") {
            throw ApiException(401, "验证码错误")
        }

        return when (role) {
            Role.USER -> {
                val user = userRepo.findByPhone(phone) ?: run {
                    val newUser = User(phone = phone)
                    userRepo.save(newUser)
                }
                jwtProvider.generateToken(user.id, Role.USER)
            }

            Role.CONSULTANT -> {
                val consultant = consultantRepo.findByPhone(phone)
                    ?: throw ApiException(404, "咨询师不存在")
                jwtProvider.generateToken(consultant.id, Role.CONSULTANT)
            }

            Role.ADMIN -> {
                throw ApiException(400, "请使用管理员专用登录接口")
            }
        }
    }


    fun loginAdmin(username: String, password: String): String {
        if (username != "admin" || password != adminPassword) {
            throw ApiException(401, "管理员账号或密码错误")
        }
        return jwtProvider.generateToken(-1, Role.ADMIN)
    }
}
