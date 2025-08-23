package com.goodfriend.backend.dto

import com.goodfriend.backend.data.Appointment
import com.goodfriend.backend.data.AppointmentStatus
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateAppointmentRequest(
    @field:NotNull(message = "consultantId 不能为空")
    val consultantId: Long? = null,

    @field:NotNull(message = "startTime 不能为空")
    @field:Future(message = "开始时间必须是将来时间")
    val startTime: LocalDateTime? = null,

    @field:NotNull(message = "endTime 不能为空")
    @field:Future(message = "结束时间必须是将来时间")
    val endTime: LocalDateTime? = null,

    @field:Size(max = 500, message = "备注过长")
    val note: String? = null
)

data class AppointmentResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val userAvatar: String,
    val consultantId: Long,
    val consultantName: String,
    val startTime: String,
    val endTime: String,
    val status: AppointmentStatus,
    val note: String?,
    val createdAt: String,
    val testResults: List<TestResultResponse> = emptyList()
) {
    companion object {
        fun from(a: Appointment,
                 testResults: List<TestResultResponse> = emptyList()
        ): AppointmentResponse {

            return AppointmentResponse(
                id = a.id,
                userId = a.user.id,
                userName = a.user.name,
                userAvatar = a.user.avatar,
                consultantId = a.consultant.id,
                consultantName = a.consultant.name,
                startTime = a.startTime.toString(),
                endTime = a.endTime.toString(),
                status = a.status,
                note = a.note,
                createdAt = a.createdAt.toString(),
                testResults = testResults
            )
        }
    }
}

data class CancelAppointmentRequest(
    @field:Size(max = 200, message = "reason too long")  // 超过 200 触发 422
    val reason: String? = null
)