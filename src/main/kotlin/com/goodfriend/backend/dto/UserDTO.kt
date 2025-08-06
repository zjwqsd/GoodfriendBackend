package com.goodfriend.backend.dto

import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.data.User
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

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