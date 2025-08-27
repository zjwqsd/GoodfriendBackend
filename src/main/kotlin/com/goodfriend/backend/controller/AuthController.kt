package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.LoginRequest
import com.goodfriend.backend.dto.RegisterRequest
import com.goodfriend.backend.dto.WxLoginRequest
import com.goodfriend.backend.dto.WxLoginResponse
import com.goodfriend.backend.service.AuthService
import com.goodfriend.backend.service.WxAuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val wxAuthService: WxAuthService
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

    @PostMapping("/wx/login")
    fun wxLogin(@RequestBody @Valid req: WxLoginRequest): ResponseEntity<WxLoginResponse> {
        val (token, isNew) = wxAuthService.wxLogin(req.jsCode, req.phoneCode)
        return ResponseEntity.ok(WxLoginResponse(token = token, isNewUser = isNew))
    }
}



