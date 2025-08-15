package com.goodfriend.backend.dto

import com.goodfriend.backend.data.Review
import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class CreateReviewRequest(
    @field:NotNull(message = "consultantId 不能为空")
    val consultantId: Long? = null,

    // 可选：如果基于具体预约来评价，传入它
    val appointmentId: Long? = null,

    @field:NotNull(message = "rating 不能为空")
    @field:Min(1)
    @field:Max(5)
    val rating: Int? = null,

    @field:Size(max = 2000, message = "评论内容过长")
    val content: String? = null,

    // 最多 5 个标签，每个 1~10 个字符
    @field:Size(max = 5, message = "标签数量不能超过 5 个")
    val tags: List<@Size(min = 1, max = 10, message = "单个标签长度应为 1~10") String>? = null
)

data class ReviewResponse(
    val id: Long,
    val userId: Long,
    val consultantId: Long,
    val rating: Int,
    val content: String?,
    val tags: List<String>,
    val createdAt: String
) {
    companion object {
        fun from(r: Review) = ReviewResponse(
            id = r.id,
            userId = r.user.id,
            consultantId = r.consultant.id,
            rating = r.rating,
            content = r.content,
            tags = r.tags,
            createdAt = r.createdAt.toString()
        )
    }
}

data class TagCount(val tag: String, val count: Long)

data class ConsultantReviewStatsResponse(
    val consultantId: Long,
    val avgRating: Double,
    val totalReviews: Long,
    val tags: List<TagCount>
)
