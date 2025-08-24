package com.goodfriend.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateWishRequest(
    val content: String,
    val images: List<String> = emptyList(),
    val anonymous: Boolean = true,
    val quoteId: Long? = null
)
