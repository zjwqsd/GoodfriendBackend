package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.CreateWishRequest
import com.goodfriend.backend.dto.ToggleLikeResponse
import com.goodfriend.backend.dto.UnreadCountResponse
import com.goodfriend.backend.dto.WishAuthorCardResponse
import com.goodfriend.backend.dto.WishResponse
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.UserOnly
import com.goodfriend.backend.service.WishService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize


@RestController
@RequestMapping("/api/wishes")
class WishController(
    private val currentRoleService: CurrentRoleService,
    private val wishService: WishService
) {

    /**
     * 1) 获取心愿心语列表
     *    示例：GET /api/wishes?page=0&size=20
     */
    @GetMapping
    @UserOnly
    fun list(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<WishResponse>> {
        val user = currentRoleService.getCurrentUser(request)
        val list = wishService.listWishes(user, page, size)
        return ResponseEntity.ok(list)
    }

    /**
     * 2) 发布心愿心语
     */
    @PostMapping
    @UserOnly
    fun create(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @Valid @RequestBody req: CreateWishRequest
    ): ResponseEntity<WishResponse> {
        val user = currentRoleService.getCurrentUser(request)
        val saved = wishService.createWish(user, req)
        return ResponseEntity.status(201).body(saved)
    }

    /**
     * 3) 点赞/取消点赞（切换）
     *    POST /api/wishes/{id}/like
     */
    @PostMapping("/{id}/like")
    @UserOnly
    fun toggleLike(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @PathVariable id: Long
    ): ResponseEntity<ToggleLikeResponse> {
        val user = currentRoleService.getCurrentUser(request)
        val res = wishService.toggleLike(user, id)
        return ResponseEntity.ok(res)
    }

    /**
     * 4) 删除心愿心语（仅作者本人可删）
     */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER','CONSULTANT','ADMIN')")
    fun delete(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @PathVariable id: Long
    ): ResponseEntity<Void> {

        val isAdmin = currentRoleService.isAdmin(request)   // ← 需要在 CurrentRoleService 提供该方法
        val currentUser = if (isAdmin) null else currentRoleService.getCurrentUser(request)

        wishService.deleteWish(currentUser, id, isAdmin)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/unread-count")
    @UserOnly
    fun unreadCount(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String
    ): ResponseEntity<UnreadCountResponse> {
        val user = currentRoleService.getCurrentUser(request)
        val count = wishService.getUnreadCount(user)
        return ResponseEntity.ok(UnreadCountResponse(count))
    }

    /** 将心愿流全部标记为已读 */
    @PostMapping("/mark-read")
    @UserOnly
    fun markAllRead(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        wishService.markAllRead(user)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/author")
    @UserOnly
    fun getAuthorCard(
        request: HttpServletRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @PathVariable id: Long
    ): ResponseEntity<WishAuthorCardResponse> {
        val current = currentRoleService.getCurrentUser(request)
        val res = wishService.getAuthorCard(current, id)
        return ResponseEntity.ok(res)
    }

}
