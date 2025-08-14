package com.goodfriend.backend.dto

import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.data.User
import jakarta.validation.constraints.*
import java.time.LocalDate

data class UpdateUserRequest(
    @field:NotBlank(message = "姓名不能为空")
    @field:Size(min = 1, max = 20, message = "姓名长度必须在1~20个字符之间")
    val name: String? = null,

    @field:Min(value = 1, message = "年龄必须大于0")
    @field:Max(value = 150, message = "年龄不能超过150")
    val age: Int? = null,

    val gender: Gender? = null,

    @field:NotBlank(message = "地域不能为空")
    @field:Size(min = 1, max = 50, message = "地域长度不能超过50")
    val region: String? = null,

    val avatar: String?, // 可空，不验证，只在 Service 内处理逻辑

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

data class SaveTestResultRequest(
    val testName: String,
    val score: Double
)

data class TestResultResponse(
    val id: Long,
    val testName: String,
    val score: Double,
    val createdAt: String
)