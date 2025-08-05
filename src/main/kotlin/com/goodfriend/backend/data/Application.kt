package com.goodfriend.backend.data
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime


@Entity
@Table(name = "consultant_applications")
data class ConsultantApplication(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @field:NotBlank
    val name: String,

    @field:Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式错误")
    val idCardNumber: String,

    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    val phone: String,

    @field:NotBlank
    val education: String,

    @field:NotBlank
    val university: String,

    @field:NotBlank
    val major: String,

    @field:NotBlank
    val licenseNumber: String,

    @field:Min(0)
    val experienceYears: Int,

    @field:NotBlank
    val specialty: String,

    @field:Size(max = 500)
    val bio: String,

    @field:Size(max = 300)
    val reason: String,

    @Enumerated(EnumType.STRING)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)


enum class ApplicationStatus {
    PENDING, APPROVED, REJECTED
}
