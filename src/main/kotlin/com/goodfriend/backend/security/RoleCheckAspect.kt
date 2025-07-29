package com.goodfriend.backend.security

import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.security.annotation.AdminOnly
import com.goodfriend.backend.security.annotation.ConsultantOnly
import com.goodfriend.backend.security.annotation.UserOnly
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Aspect
@Component
class RoleCheckAspect(
    private val currentRoleService: CurrentRoleService
) {

    @Before("@annotation(userOnly)")
    fun checkUser(joinPoint: JoinPoint, userOnly: UserOnly) {
        val request = getCurrentRequest()
        if (currentRoleService.getCurrentRole(request) != Role.USER) {
            throw ApiException(403, "仅限用户访问")
        }
    }

    @Before("@annotation(consultantOnly)")
    fun checkConsultant(joinPoint: JoinPoint, consultantOnly: ConsultantOnly) {
        val request = getCurrentRequest()
        if (currentRoleService.getCurrentRole(request) != Role.CONSULTANT) {
            throw ApiException(403, "仅限咨询师访问")
        }
    }

    @Before("@annotation(adminOnly)")
    fun checkAdmin(joinPoint: JoinPoint, adminOnly: AdminOnly) {
        val request = getCurrentRequest()
        if (currentRoleService.getCurrentRole(request) != Role.ADMIN) {
            throw ApiException(403, "仅限管理员访问")
        }
    }

    private fun getCurrentRequest(): HttpServletRequest {
        return (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
    }
}
