package com.goodfriend.backend.controller

import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.data.User
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.UserOnly
import com.goodfriend.backend.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import jakarta.validation.constraints.*
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

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

data class UpdateUserRequest(
    @field:Size(min = 1, max = 20, message = "姓名长度必须在1~20个字符之间")
    val name: String? = null,

    @field:Min(value = 1, message = "年龄必须大于0")
    @field:Max(value = 150, message = "年龄不能超过150")
    val age: Int? = null,

    val gender: Gender? = null,

    @field:Size(min = 1, max = 50, message = "地域不能为空")
    val region: String? = null,

    @field:Pattern(
        regexp = "^(user/avatars/)[\\w.-]+\\.(jpg|png|jpeg)$",
        message = "头像路径格式错误"
    )
    val avatar: String? = null,

    val birthday: LocalDate? = null,

    @field:Size(max = 200, message = "兴趣爱好内容过长")
    val hobby: String? = null
)


data class UserProfileResponse(
    val id: Long,
    val phone: String,
    val name: String,
    val avatar: String,
    val birthday: LocalDate?,
    val age: Int,
    val gender: Gender,
    val region: String,
    val hobby: String?
) {
    companion object {
        fun from(user: User): UserProfileResponse {
            return UserProfileResponse(
                id = user.id,
                phone = user.phone,
                name = user.name,
                avatar = user.avatar,
                birthday = user.birthday,
                age = user.age,
                gender = user.gender,
                region = user.region,
                hobby = user.hobby
            )
        }
    }
}

data class ConsultantApplicationRequest(
    @field:NotBlank(message = "真实姓名不能为空")
    val name: String,

    @field:Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    val idCardNumber: String,

    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

    @field:NotBlank(message = "学历不能为空")
    val education: String,  // 如“本科”、“硕士”

    @field:NotBlank(message = "毕业院校不能为空")
    val university: String,

    @field:NotBlank(message = "专业不能为空")
    val major: String,

    val licenseNumber: String? = null,

    @field:Min(value = 0, message = "工作经验不能为负数")
    val experienceYears: Int,

    // specialty 改为数组类型
    @field:Size(min = 1, message = "至少填写一个擅长领域")
    val specialty: List<String>,

    @field:Size(max = 500, message = "个人简介不能超过500字")
    val bio: String,

    @field:Size(max = 300, message = "申请理由不能超过300字")
    val reason: String
)

