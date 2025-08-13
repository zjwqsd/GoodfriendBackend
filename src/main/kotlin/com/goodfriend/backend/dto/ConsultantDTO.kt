package com.goodfriend.backend.dto

import com.goodfriend.backend.data.ApplicationStatus
import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.Gender
import jakarta.validation.constraints.*
import java.time.LocalDateTime
import com.fasterxml.jackson.annotation.JsonInclude

data class ConsultantApplicationDTO(
    val id: Long,
    val userId: Long,
    val name: String?,
    val phone: String?,
    val specialty: List<String>,
    val reason: String,
    val status: ApplicationStatus,
    val createdAt: LocalDateTime,
    val reviewComment: String? = null
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
            createdAt = app.createdAt,
            reviewComment = app.reviewComment
        )
    }
}

data class UpdateConsultantRequest(

    @field:Size(min = 1, max = 20, message = "姓名不能为空")
    val name: String? = null,

    @field:NotNull(message = "性别不能为空")
    val gender: Gender? = null,

    @field:Size(min = 1, max = 50, message = "工作地点不能为空")
    val location: String? = null,

    @field:Size(min = 1, max = 10, message = "请填写 1~10 个擅长领域")
    val specialty: List<@Size(min = 1, max = 50, message = "每个擅长领域长度应在1~50之间") String>? = null,

    @field:Min(0, message = "从业年限不能为负数")
    val experienceYears: Int? = null,

    @field:Min(0, message = "咨询时长不能为负数")
    val consultationCount: Int? = null,

    @field:Min(0, message = "受训时长不能为负数")
    val trainingHours: Int? = null,

    @field:Min(0, message = "督导时长不能为负数")
    val supervisionHours: Int? = null,

    @field:Size(max = 1000, message = "个人简介不能超过1000字")
    val bio: String? = null,

    @field:Size(min = 1, max = 5, message = "请填写 1~5 种咨询方式")
    val consultationMethods: List<@Size(min = 2, max = 20, message = "每种方式长度应在2~20之间") String>? = null,

    @field:Size(max = 100, message = "接诊时间描述不能超过100字")
    val availability: String? = null,

    @field:Min(0, message = "咨询费用不能为负数")
    val pricePerHour: Int? = null,

    val educationList: List<EducationDTO>? = null,

    val experienceList: List<ExperienceDTO>? = null,

    val certificationList: List<CertificationDTO>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConsultantDTO(
    val id: Long,
    val name: String,
    val gender: Gender?,                 // 可空
    val location: String?,               // "北京·朝阳" 之类
    val level: String?,                  // 可空
    val specialty: List<String>?,        // 可空
    val rating: Double,
    val avatar: String?,                 // 可空
    val pricePerHour: Int,

    // 新增的详细信息
    val consultationHours: Int,          // 对应“个案时长/咨询时长（小时）”，由 consultationCount 映射
    val experienceYears: Int,
    val trainingHours: Int?,             // 受训时长（小时）
    val supervisionHours: Int?,          // 督导时长（小时）
    val bio: String?,                    // 个人简介
    val consultationMethods: List<String>?, // 咨询方式
    val availability: String?,           // 接诊时间

    // 明细列表（可多段）
    val educationList: List<EducationDTO>?,
    val experienceList: List<ExperienceDTO>?,
    val certificationList: List<CertificationDTO>?
) {
    companion object {
        fun from(c: Consultant): ConsultantDTO =
            ConsultantDTO(
                id = c.id,
                name = c.name,
                gender = c.gender,
                location = c.location,
                level = c.level,
                specialty = c.specialty,
                rating = c.rating,
                avatar = c.avatar,
                pricePerHour = c.pricePerHour,

                consultationHours = c.consultationCount,   // 关键映射
                experienceYears = c.experienceYears,
                trainingHours = c.trainingHours,
                supervisionHours = c.supervisionHours,
                bio = c.bio,
                consultationMethods = c.consultationMethods,
                availability = c.availability,

                educationList = c.educationList.map {
                    EducationDTO(
                        degree = it.degree,
                        school = it.school,
                        major = it.major,
                        time = it.time
                    )
                },
                experienceList = c.experienceList.map {
                    ExperienceDTO(
                        company = it.company,
                        position = it.position,
                        duration = it.duration,
                        description = it.description
                    )
                },
                certificationList = c.certificationList.map {
                    CertificationDTO(
                        name = it.name,
                        number = it.number,
                        issuer = it.issuer,
                        date = it.date
                    )
                }
            )
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL) // 可选：序列化时忽略为 null 的字段
data class ConsultantProfileResponse(
    val id: Long,
    val phone: String,
    val name: String,
    val avatar: String?,
    val gender: Gender,
    val location: String?,
    val level: String?,
    val specialty: List<String>?,
    val experienceYears: Int,
    val consultationCount: Int,
    val trainingHours: Int,
    val supervisionHours: Int,
    val bio: String?,
    val consultationMethods: List<String>?,
    val availability: String?,
    val pricePerHour: Int,
    val rating: Double,
    val educationList: List<EducationDTO>?,
    val experienceList: List<ExperienceDTO>?,
    val certificationList: List<CertificationDTO>?
) {
    companion object {
        fun from(c: Consultant): ConsultantProfileResponse {
            return ConsultantProfileResponse(
                id = c.id,
                phone = c.phone,
                name = c.name,
                avatar = c.avatar,                 // 允许为 null
                gender = c.gender,
                location = c.location,             // 允许为 null
                level = c.level,                   // 允许为 null
                specialty = c.specialty,           // 允许为 null
                experienceYears = c.experienceYears,
                consultationCount = c.consultationCount,
                trainingHours = c.trainingHours,
                supervisionHours = c.supervisionHours,
                bio = c.bio,                       // 允许为 null
                consultationMethods = c.consultationMethods,
                availability = c.availability,
                pricePerHour = c.pricePerHour,
                rating = c.rating,
                educationList = c.educationList.map {
                    EducationDTO(
                        degree = it.degree,
                        school = it.school,
                        major = it.major,
                        time = it.time
                    )
                },
                experienceList = c.experienceList.map {
                    ExperienceDTO(
                        company = it.company,
                        position = it.position,
                        duration = it.duration,
                        description = it.description
                    )
                },
                certificationList = c.certificationList.map {
                    CertificationDTO(
                        name = it.name,
                        number = it.number,
                        issuer = it.issuer,
                        date = it.date
                    )
                }
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

data class EducationDTO(
    @field:NotBlank(message = "学历不能为空")
    val degree: String,

    @field:NotBlank(message = "学校名称不能为空")
    val school: String,

    @field:NotBlank(message = "专业不能为空")
    val major: String,

    @field:NotBlank(message = "时间不能为空")
    val time: String  // 示例格式：2014-2018
)

data class ExperienceDTO(
    @field:NotBlank(message = "公司名称不能为空")
    val company: String,

    @field:NotBlank(message = "职位不能为空")
    val position: String,

    @field:NotBlank(message = "工作时间不能为空")
    val duration: String, // 示例格式：2021年至今

    @field:NotBlank(message = "工作描述不能为空")
    val description: String
)


data class CertificationDTO(
    @field:NotBlank(message = "证书名称不能为空")
    val name: String,

    @field:NotBlank(message = "证书编号不能为空")
    val number: String,

    @field:NotBlank(message = "发证机构不能为空")
    val issuer: String,

    @field:NotBlank(message = "获证时间不能为空")
    val date: String  // 示例格式：2021年06月
)
