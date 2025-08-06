package com.goodfriend.backend.security

import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.User
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

@Service
class CurrentRoleService(
    private val userRepo: UserRepository,
    private val consultantRepo: ConsultantRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun getTokenFromRequest(request: HttpServletRequest): String {
        val authHeader = request.getHeader("Authorization")
            ?: throw ApiException(401, "未提供 Authorization 头")

        if (!authHeader.startsWith("Bearer ")) {
            throw ApiException(401, "Authorization 头格式错误，应为 Bearer <token>")
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw ApiException(401, "Token 不能为空")
        }

        return token
    }

    fun getCurrentRole(request: HttpServletRequest): Role =
        jwtTokenProvider.getRoleFromToken(getTokenFromRequest(request))

    fun getCurrentId(request: HttpServletRequest): Long =
        jwtTokenProvider.getIdFromToken(getTokenFromRequest(request))

    fun getCurrentUser(request: HttpServletRequest): User =
        userRepo.findById(getCurrentId(request)).orElseThrow { ApiException(400,"用户不存在") }

    fun getCurrentConsultant(request: HttpServletRequest): Consultant =
        consultantRepo.findById(getCurrentId(request)).orElseThrow { ApiException(400,"咨询师不存在") }

}
