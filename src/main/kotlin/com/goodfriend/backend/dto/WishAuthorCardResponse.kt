package com.goodfriend.backend.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** 头像点击卡片信息：匿名只返回 anonymous=true；非匿名返回 name + jointDate */
data class WishAuthorCardResponse(
    val anonymous: Boolean,
    val name: String? = null,
    @get:JsonProperty("jointDate")
    val joinDate: String? = null
)
