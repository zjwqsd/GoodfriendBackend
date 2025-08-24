package com.goodfriend.backend.dto

data class WishResponse(
    val id: Long,
    val content: String,
    val images: List<String>,
    val anonymous: Boolean,
    val createdAt: String,
    val likeCount: Long,
    val likedByMe: Boolean,
    val mine: Boolean,
    val quoteId: Long?
)




