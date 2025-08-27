package com.goodfriend.backend.service

import com.goodfriend.backend.data.User
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.data.WxOpenBinding
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.UserRepository
import com.goodfriend.backend.repository.WxOpenBindingRepository
import com.goodfriend.backend.security.JwtTokenProvider
import com.goodfriend.backend.security.Role
import com.goodfriend.backend.thirdparty.WechatApiClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
@Service
class WxAuthService(
    private val userRepo: UserRepository,
    private val wxBindRepo: WxOpenBindingRepository,
    private val jwt: JwtTokenProvider,
    private val wechat: WechatApiClient
) {
    data class Result(val token: String, val isNew: Boolean)

    @Transactional
    fun wxLogin(jsCode: String, phoneCode: String): Result {
        if (phoneCode.isBlank()) throw ApiException(400, "需要先授权手机号")

        // 1) 换 openid / session_key
        val wx = wechat.jscode2session(jsCode)
        val openid = wx.openid ?: throw ApiException(401, "微信未返回 openid")

        // 2) 换取手机号（用户点了授权按钮才会有）
        val rawPhone = wechat.getPhoneByCode(phoneCode)
        val phone = normalizePhone(rawPhone)  // +86 去前缀、去空格等

        // 3) 以“手机号”为唯一身份查找/创建用户
        val user = userRepo.findByPhone(phone) ?: userRepo.save(
            User(phone = phone)   // 只能有带手机号的创建路径
        )
        val isNew = (user.id == userRepo.findByPhone(phone)?.id).not() // 简单判断也可用布尔变量

        // 4) 绑定 openid → userId（若已绑到其他用户则切换到当前手机号对应的用户）
        val binding = wxBindRepo.findByOpenid(openid)
        if (binding == null) {
            wxBindRepo.save(
                WxOpenBinding(
                    openid = openid,
                    unionid = wx.unionid,
                    userId = user.id,
                    sessionKey = wx.session_key
                )
            )
        } else if (binding.userId != user.id) {
            // 同一个 openid 以前绑错人/旧测试号，纠正到当前手机号的用户
            binding.userId = user.id
            binding.sessionKey = wx.session_key
            binding.updatedAt = Instant.now()
            wxBindRepo.save(binding)
        } else {
            binding.sessionKey = wx.session_key
            binding.updatedAt = Instant.now()
            wxBindRepo.save(binding)
        }

        // 5) 发 JWT
        return Result(jwt.generateToken(user.id, Role.USER), isNew)
    }

    private fun normalizePhone(p: String): String =
        p.replace(" ", "").removePrefix("+86").removePrefix("86")
}
