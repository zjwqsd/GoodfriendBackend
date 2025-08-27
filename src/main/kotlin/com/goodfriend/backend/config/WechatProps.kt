package com.goodfriend.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

// 读取 application.yml 中的 wechat.* 配置
@ConfigurationProperties(prefix = "wechat")
data class WechatProps(
    var appid: String = "",
    var secret: String = ""
)
