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
    val name: String = "",  // 改为可空类型
    val avatar: String? = null,  // 改为可空类型
    val birthday: LocalDate? = null,
    val age: Int? = null,  // 改为可空类型
    val gender: Gender? = null,  // 改为可空类型
    val region: String? = null,  // 改为可空类型
    val hobby: String? = null  // 改为可空类型
) {
    companion object {
        private const val DEFAULT_NAME = "开发用户"
        fun from(user: User): UserProfileResponse {
            val displayName = user.name
                .takeUnless { it.isBlank() || it == DEFAULT_NAME } ?: ""
            return UserProfileResponse(
                id = user.id,
                phone = user.phone,
                name = displayName,  // 如果为空字符串，返回 null
                avatar = if (user.avatar == "user/avatars/default.jpg") null else user.avatar,  // 如果是默认头像，返回 null
                birthday = user.birthday,
                age = if (user.age <= 0) null else user.age,  // 如果年龄 <= 0，返回 null
                gender = user.gender.takeIf { it != Gender.UNKNOWN },  // 如果性别是未知，返回 null
                region = if (user.region == "未知") null else user.region,  // 如果地域是"未知"，返回 null
                hobby = user.hobby?.takeIf { it.isNotBlank() }  // 如果爱好为空，返回 null
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