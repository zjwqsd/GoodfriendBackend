package com.goodfriend.backend.dto

import com.goodfriend.backend.data.ApplicationStatus
import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.Gender
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ConsultantApplicationDTO(
    val id: Long,
    val userId: Long,
    val name: String?,
    val phone: String?,
    val specialty: List<String>,
    val reason: String,
    val status: ApplicationStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(app: ConsultantApplication) = ConsultantApplicationDTO(
            id = app.id,
            userId = app.userId,
            name = app.name,
            phone = app.phone,
            specialty = app.specialty,
            reason = app.reason,
            status = app.status,
            createdAt = app.createdAt
        )
    }
}

data class UpdateConsultantRequest(
    @field:Size(min = 1, max = 20, message = "姓名不能为空")
    val name: String? = null,

    @field:Size(min = 1, max = 50, message = "工作地点不能为空")
    val location: String? = null,

    // specialty 改为数组，每项长度限制 1-50，总长度限制 1-10
    @field:Size(min = 1, max = 10, message = "请填写 1~10 个擅长领域")
    val specialty: List<@Size(min = 1, max = 50, message = "每个擅长领域长度应在1~50之间") String>? = null
)

// ✅ 固定类型写法（推荐）
data class ConsultantDTO(
    val id: Long,
    val name: String,
    val gender: Gender,
    val location: String,
    val specialty: List<String>, // 改为数组
    val level: String,
    val rating: Double,
    val avatar: String,
    val pricePerHour: Int,
) {
    companion object {
        fun from(c: Consultant): ConsultantDTO = ConsultantDTO(
            id = c.id,
            name = c.name,
            gender = c.gender,
            location = c.location,
            specialty = c.specialty,  // 确保 Consultant 中也是 List<String>
            level = c.level,
            rating = c.rating,
            avatar = c.avatar,
            pricePerHour = c.pricePerHour
        )
    }
}

data class ConsultantProfileResponse(
    val id: Long,
    val phone: String,
    val name: String,
    val avatar: String,
    val gender: Gender,
    val location: String,
    val level: String,
    val specialty: List<String>,
    val experienceYears: Int,
    val consultationCount: Int,
    val pricePerHour: Int,
    val rating: Double
) {
    companion object {
        fun from(c: Consultant): ConsultantProfileResponse {
            return ConsultantProfileResponse(
                id = c.id,
                phone = c.phone,
                name = c.name,
                avatar = c.avatar,
                gender = c.gender,
                location = c.location,
                level = c.level,
                specialty = c.specialty,
                experienceYears = c.experienceYears,
                consultationCount = c.consultationCount,
                pricePerHour = c.pricePerHour,
                rating = c.rating
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