package com.goodfriend.backend.security.annotation

import com.goodfriend.backend.security.CurrentRoleService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AdminOnly

@Component
class AdminOnlyInterceptor(
    private val currentRoleService: CurrentRoleService
) : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val admin = currentRoleService.getCurrentAdmin(request)
        if (admin == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return false
        }
        return true
    }
}
