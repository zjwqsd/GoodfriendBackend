package com.goodfriend.backend.service

import com.goodfriend.backend.data.Wish
import com.goodfriend.backend.data.WishLike
import com.goodfriend.backend.data.User
import com.goodfriend.backend.data.WishInboxState
import com.goodfriend.backend.dto.CreateWishRequest
import com.goodfriend.backend.dto.ToggleLikeResponse
import com.goodfriend.backend.dto.WishAuthorCardResponse
import com.goodfriend.backend.dto.WishResponse
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.WishInboxStateRepository
import com.goodfriend.backend.repository.WishLikeRepository
import com.goodfriend.backend.repository.WishRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Service
class WishService(
    private val wishRepo: WishRepository,
    private val wishLikeRepo: WishLikeRepository,
    private val inboxRepo: WishInboxStateRepository
) {

    @Transactional(readOnly = true)
    fun listWishes(current: User, page: Int, size: Int): List<WishResponse> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 50))
        val wishes = wishRepo.findAllByOrderByCreatedAtDesc(pageable)

        return wishes.map { w ->
            val likeCount = wishLikeRepo.countByWish(w)
            val likedByMe = wishLikeRepo.existsByWishAndUser(w, current)
            toDto(w, likeCount, likedByMe, current)
        }
    }

    @Transactional
    fun createWish(current: User, req: CreateWishRequest): WishResponse {
               // 1) 简单业务校验
        val content = (req.content ?: "").trim()


                // 2) 不再支持图片
//                if (req.imageIds.isNotEmpty()) {
//                        throw ApiException(400, "心语暂不支持图片")
//                   }
//                if (content.isBlank() && req.quoteId == null) {
//            throw ApiException(400, "内容或引用至少提供一项")
//        }

        // 3) 引用的心语
        val quoted: Wish? = req.quoteId?.let { id ->
            wishRepo.findById(id).orElseThrow { ApiException(404, "被引用的心语不存在") }
        }

        // 4) 持久化
        val wish = Wish(
            user = current,
            content = content,
           images = mutableListOf(), // 现在固定为空
            anonymous = req.anonymous,
            quoteWish = quoted
        )
        val saved = wishRepo.save(wish)

        return toDto(saved, likeCount = 0, likedByMe = false, current = current)
    }

    /**
     * 点赞/取消点赞（切换）
     */
    @Transactional
    fun toggleLike(current: User, wishId: Long): ToggleLikeResponse {
        val wish = wishRepo.findById(wishId).orElseThrow { ApiException(404, "心语不存在") }

        val liked = wishLikeRepo.existsByWishAndUser(wish, current)
        if (liked) {
            wishLikeRepo.deleteByWishAndUser(wish, current)
        } else {
            wishLikeRepo.save(WishLike(wish = wish, user = current))
        }
        val count = wishLikeRepo.countByWish(wish)
        return ToggleLikeResponse(liked = !liked, likeCount = count)
    }

    @Transactional
    fun deleteWish(current: User?, wishId: Long, isAdmin: Boolean) {
        val wish = wishRepo.findById(wishId).orElseThrow { ApiException(404, "心语不存在") }

        if (!isAdmin) {
            if (current == null || wish.user.id != current.id) {
                throw ApiException(403, "无权删除他人的心语")
            }
        }

        // 先清理点赞记录（避免外键）
        wishLikeRepo.deleteByWish(wish)

        // 解除其他心语对本条的引用（避免自引用外键）
        wishRepo.clearQuotesOf(wish)

        // 删除心语本身
        wishRepo.delete(wish)

        // 不删除静态文件：由资源管理后台决定下线/删除（markValid / deleteResource）
    }

    /** 获取未读数量（不会产生写操作；无 state 时按 0 处理） */
    @Transactional(readOnly = true)
    fun getUnreadCount(current: User): Long {
        val state = inboxRepo.findByUser(current)
        val last = state?.lastCheckedAt ?: LocalDateTime.now()
        return wishRepo.countByCreatedAtAfterAndUserNot(last, current)
    }

    /** 将“心愿流”全部标记为已读（更新/创建 state） */
    @Transactional
    fun markAllRead(current: User) {
        val now = LocalDateTime.now()
        val state = inboxRepo.findByUser(current)
        if (state == null) {
            inboxRepo.save(WishInboxState(user = current, lastCheckedAt = now))
        } else {
            state.lastCheckedAt = now
            inboxRepo.save(state)
        }
    }

    private fun toDto(w: com.goodfriend.backend.data.Wish, likeCount: Long, likedByMe: Boolean, current: User): WishResponse {
        return WishResponse(
            id = w.id,
            content = w.content,
            //images = w.images.toList(),
            anonymous = w.anonymous,
            createdAt = w.createdAt.toString(),
            likeCount = likeCount,
            likedByMe = likedByMe,
            mine = (w.user.id == current.id),
            quoteId = w.quoteWish?.id
        )
    }

    companion object {
        private const val DEFAULT_NAME = "开发用户"
        private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    @Transactional(readOnly = true)
    fun getAuthorCard(current: User, wishId: Long): WishAuthorCardResponse {
        val wish = wishRepo.findById(wishId).orElseThrow { ApiException(404, "心语不存在") }

        // 匿名：不返回任何个人信息
        if (wish.anonymous) {
            return WishAuthorCardResponse(anonymous = true)
        }

        val author = wish.user
        val displayName = author.name.let { n ->
            if (n.isBlank() || n == DEFAULT_NAME) null else n
        }
        val joinDate = author.createdAt.toLocalDate().format(DATE_FMT) // "yyyy-MM-dd"

        return WishAuthorCardResponse(
            anonymous = false,
            name = displayName,
            joinDate = joinDate  // 序列化为 jointDate
        )
    }
}
