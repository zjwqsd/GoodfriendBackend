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
    fun getTokenFromRequest(request: HttpServletRequest): String =
        request.getHeader("Authorization")?.removePrefix("Bearer ") ?: throw ApiException(400,"缺少Token")

    fun getCurrentRole(request: HttpServletRequest): Role =
        jwtTokenProvider.getRoleFromToken(getTokenFromRequest(request))

    fun getCurrentId(request: HttpServletRequest): Long =
        jwtTokenProvider.getIdFromToken(getTokenFromRequest(request))

    fun getCurrentUser(request: HttpServletRequest): User =
        userRepo.findById(getCurrentId(request)).orElseThrow { ApiException(400,"用户不存在") }

    fun getCurrentConsultant(request: HttpServletRequest): Consultant =
        consultantRepo.findById(getCurrentId(request)).orElseThrow { ApiException(400,"咨询师不存在") }

//    fun getCurrentEntity(request: HttpServletRequest): Any = when (getCurrentRole(request)) {
//        Role.USER -> getCurrentUser(request)
//        Role.CONSULTANT -> getCurrentConsultant(request)
//        else -> throw RuntimeException("管理员不支持实体访问")
//    }
}
