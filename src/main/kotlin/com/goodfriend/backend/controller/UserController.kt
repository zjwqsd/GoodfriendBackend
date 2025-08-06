package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.ConsultantApplicationRequest
import com.goodfriend.backend.dto.UpdateUserRequest
import com.goodfriend.backend.dto.UserProfileResponse
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.UserOnly
import com.goodfriend.backend.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val currentRoleService: CurrentRoleService
) {

    @PutMapping("/update")
    @UserOnly
    fun updateUser(@RequestBody @Valid req: UpdateUserRequest, request: HttpServletRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.updateUserInfo(
            user.id,
            req.name,
            req.age,
            req.gender,
            req.region,
            req.avatar,
            req.birthday,
            req.hobby
        )

        return ResponseEntity.ok().build()
    }

    @GetMapping("/profile")
    @UserOnly
    fun getUserProfile(request: HttpServletRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<UserProfileResponse> {
        val user = currentRoleService.getCurrentUser(request)
        return ResponseEntity.ok(UserProfileResponse.from(user))
    }



    @PostMapping("/consultant/apply")
    @UserOnly
    fun applyToConsultant(@RequestBody @Valid req: ConsultantApplicationRequest, request: HttpServletRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.submitConsultantApplication(user.id, req)
        return ResponseEntity.ok().build()
    }

}





