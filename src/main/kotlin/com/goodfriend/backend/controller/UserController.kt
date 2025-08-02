package com.goodfriend.backend.controller

import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.UserOnly
import com.goodfriend.backend.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import jakarta.validation.constraints.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val currentRoleService: CurrentRoleService
) {

    @PutMapping("/update")
    @UserOnly
    fun updateUser(@RequestBody @Valid req: UpdateUserRequest, request: HttpServletRequest): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.updateUserInfo(user.id, req.name, req.age, req.gender, req.region)
        return ResponseEntity.ok().build()
    }


    @PostMapping("/consultant/apply")
    @UserOnly
    fun applyToConsultant(@RequestBody @Valid req: ConsultantApplicationRequest, request: HttpServletRequest): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.submitConsultantApplication(user.id, req.specialty, req.reason)
        return ResponseEntity.ok().build()
    }

    data class ConsultantApplicationRequest(
        @field:NotBlank(message = "专长不能为空")
        val specialty: String,

        @field:NotBlank(message = "申请理由不能为空")
        val reason: String
    )

}

data class UpdateUserRequest(
    @field:Size(min = 1, max = 20, message = "姓名长度必须在1~20个字符之间")
    val name: String? = null,

    @field:Min(value = 1, message = "年龄必须大于0")
    @field:Max(value = 150, message = "年龄不能超过150")
    val age: Int? = null,

    val gender: Gender? = null,

    @field:Size(min = 1, max = 50, message = "地域不能为空")
    val region: String? = null
)
