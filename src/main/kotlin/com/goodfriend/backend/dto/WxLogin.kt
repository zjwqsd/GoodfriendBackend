package com.goodfriend.backend.dto

import jakarta.validation.constraints.NotBlank

data class WxLoginRequest(
    @field:NotBlank val jsCode: String,      // 小程序 wx.login() 拿到的 code
    @field:NotBlank val phoneCode: String
)

data class WxLoginResponse(
    val token: String,        // 你的 JWT
    val isNewUser: Boolean    // 首登标记，可用于前端引导补充资料
)
