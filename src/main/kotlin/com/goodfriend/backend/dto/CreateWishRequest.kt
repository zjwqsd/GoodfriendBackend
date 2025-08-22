package com.goodfriend.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateWishRequest(
    @field:NotBlank(message = "内容不能为空")
    @field:Size(min = 1, max = 1000, message = "内容长度需在 1~1000 之间")
    val content: String,

    // 静态资源相对路径，可空
    @field:Size(max = 9, message = "最多允许 9 张图片")
    val images: List<String>? = null,

    // 是否匿名（树洞默认匿名）
    val anonymous: Boolean = true
)
