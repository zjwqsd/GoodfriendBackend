package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.CreateWishRequest
import com.goodfriend.backend.dto.ToggleLikeResponse
import com.goodfriend.backend.dto.WishResponse
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.UserOnly
import com.goodfriend.backend.service.WishService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    @UserOnly
    fun delete(
        request: HttpServletRequest,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        wishService.deleteWish(user, id)
        return ResponseEntity.noContent().build()
    }
}
