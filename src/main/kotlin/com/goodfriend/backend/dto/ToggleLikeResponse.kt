package com.goodfriend.backend.dto

data class ToggleLikeResponse(
    val liked: Boolean,
    val likeCount: Long
)
