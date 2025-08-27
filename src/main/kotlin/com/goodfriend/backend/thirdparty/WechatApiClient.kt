package com.goodfriend.backend.thirdparty

import com.goodfriend.backend.config.WechatProps
import com.goodfriend.backend.exception.ApiException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WechatApiClient(
    private val props: WechatProps,
    private val objectMapper: ObjectMapper
) {
    private val rest = RestTemplate()

    // —— jscode2session 返回体 —— //
    data class Js2SessionResp(
        val openid: String?,
        val session_key: String?,
        val unionid: String?,
        val errcode: Int? = 0,
        val errmsg: String? = null
    )

    @Volatile private var cachedToken: Pair<String, Long>? = null  // (token, expireEpochSec)

    /** 用 jsCode 换 openid/session_key（对方 content-type 可能是 text/plain，所以先取 String 再解析） */
    fun jscode2session(jsCode: String): Js2SessionResp {
        val url = "https://api.weixin.qq.com/sns/jscode2session" +
                "?appid=${props.appid}&secret=${props.secret}" +
                "&js_code=$jsCode&grant_type=authorization_code"
        val resp = rest.getForObject(url, String::class.java)
            ?: throw ApiException(502, "微信登录失败：空响应")
        val node = parseJsonOrThrow(resp, "微信登录失败")
        val code = node.path("errcode").asInt(0)
        if (code != 0) throw ApiException(401, "微信登录失败：${node.path("errmsg").asText()}($code)")
        return objectMapper.treeToValue(node, Js2SessionResp::class.java)
    }

    /** 获取/缓存全局 access_token */
    fun getAccessToken(): String {
        val now = System.currentTimeMillis() / 1000
        cachedToken?.let { (tk, exp) -> if (exp - now > 60) return tk }
        val url = "https://api.weixin.qq.com/cgi-bin/token" +
                "?grant_type=client_credential&appid=${props.appid}&secret=${props.secret}"
        val resp = rest.getForObject(url, String::class.java)
            ?: throw ApiException(502, "获取 access_token 失败：空响应")
        val node = parseJsonOrThrow(resp, "获取 access_token 失败")
        val code = node.path("errcode").asInt(0)
        if (code != 0) throw ApiException(502, "获取 access_token 失败：${node.path("errmsg").asText()}($code)")
        val token = node.path("access_token").asText("")
        val expiresIn = node.path("expires_in").asLong(7000)
        cachedToken = token to (now + expiresIn)
        return token
    }

    /** 用前端 phoneCode 换手机号 */
    fun getPhoneByCode(phoneCode: String): String {
        if (phoneCode.isBlank()) throw ApiException(400, "缺少 phoneCode")
        val url = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=${getAccessToken()}"
        val body = mapOf("code" to phoneCode)
        val resp = rest.postForObject(url, body, String::class.java)
            ?: throw ApiException(502, "获取手机号失败：空响应")
        val node = parseJsonOrThrow(resp, "获取手机号失败")
        val code = node.path("errcode").asInt(0)
        if (code != 0) throw ApiException(400, "获取手机号失败：${node.path("errmsg").asText()}($code)")
        val phone = node.path("phone_info").path("phoneNumber").asText("")
        if (phone.isBlank()) throw ApiException(400, "获取手机号失败：返回为空")
        return phone
    }

    private fun parseJsonOrThrow(text: String, scene: String): JsonNode =
        try { objectMapper.readTree(text) }
        catch (e: Exception) { throw ApiException(502, "$scene：解析响应失败，原文：$text") }
}
