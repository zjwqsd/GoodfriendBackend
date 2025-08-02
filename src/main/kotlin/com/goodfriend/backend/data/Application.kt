package com.goodfriend.backend.data
import jakarta.persistence.*
import jakarta.validation.constraints.*
import java.time.LocalDateTime


@Entity
@Table(name = "consultant_applications")
data class ConsultantApplication(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,  // 关联用户

    @field:NotBlank(message = "专长不能为空")
    val specialty: String,

    @field:NotBlank(message = "申请理由不能为空")
    val reason: String,

    @Enumerated(EnumType.STRING)
    var status: ApplicationStatus = ApplicationStatus.PENDING,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ApplicationStatus {
    PENDING, APPROVED, REJECTED
}
