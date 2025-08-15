package com.goodfriend.backend.dto

import com.goodfriend.backend.data.Appointment
import com.goodfriend.backend.data.AppointmentStatus

data class AdminAppointmentResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val consultantId: Long,
    val consultantName: String,
    val startTime: String,
    val endTime: String,
    val status: AppointmentStatus,
    val note: String?,
    val createdAt: String
) {
    companion object {
        fun from(a: Appointment): AdminAppointmentResponse =
            AdminAppointmentResponse(
                id = a.id,
                userId = a.user.id,
                userName = a.user.name,
                consultantId = a.consultant.id,
                consultantName = a.consultant.name,
                startTime = a.startTime.toString(),
                endTime = a.endTime.toString(),
                status = a.status,
                note = a.note,
                createdAt = a.createdAt.toString()
            )
    }
}
