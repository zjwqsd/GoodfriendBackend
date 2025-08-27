package com.goodfriend.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateWishRequest(
    val content: String = "",
    //@Deprecated("心语暂不支持图片")
    //val imageIds: List<Long> = emptyList(),   // 仍然保留，服务端会拒绝非空
    val quoteId: Long? = null,
    val anonymous: Boolean = false
)