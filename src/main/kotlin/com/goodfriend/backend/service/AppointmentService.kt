package com.goodfriend.backend.service

import com.goodfriend.backend.data.*
import com.goodfriend.backend.dto.AppointmentResponse
import com.goodfriend.backend.dto.CreateAppointmentRequest
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.AppointmentRepository
import com.goodfriend.backend.repository.ConsultantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class AppointmentService(
    private val appointmentRepo: AppointmentRepository,
    private val consultantRepo: ConsultantRepository
) {

    @Transactional(readOnly = true)
    fun getMyAppointments(user: User): List<AppointmentResponse> =
        appointmentRepo.findByUserOrderByStartTimeDesc(user).map { AppointmentResponse.from(it) }

    @Transactional
    fun createAppointment(user: User, req: CreateAppointmentRequest): Appointment {
        val consultantId = req.consultantId ?: throw ApiException(400, "consultantId 不能为空")
        val start = req.startTime ?: throw ApiException(400, "startTime 不能为空")
        val end = req.endTime ?: throw ApiException(400, "endTime 不能为空")
        if (!end.isAfter(start)) throw ApiException(400, "结束时间必须晚于开始时间")

        val minutes = Duration.between(start, end).toMinutes()
        if (minutes < 15) throw ApiException(400, "预约时长不能少于 15 分钟")
        if (minutes > 180) throw ApiException(400, "预约时长不能超过 180 分钟")

        val consultant = consultantRepo.findById(consultantId)
            .orElseThrow { ApiException(404, "咨询师不存在") }

        val hasConflict = appointmentRepo.existsConsultantTimeOverlap(consultant, start, end)
        if (hasConflict) throw ApiException(409, "该时间段已被占用，请选择其他时间")

        val appt = Appointment(
            user = user,
            consultant = consultant,
            startTime = start,
            endTime = end,
            status = AppointmentStatus.PENDING,
            note = req.note
        )
        return appointmentRepo.save(appt)
    }

    @Transactional
    fun cancelMyAppointment(user: User, appointmentId: Long) {
        val appt = appointmentRepo.findById(appointmentId)
            .orElseThrow { ApiException(404, "预约不存在") }
        if (appt.user.id != user.id) throw ApiException(403, "无权取消他人预约")
        if (!LocalDateTime.now().isBefore(appt.startTime)) throw ApiException(400, "已开始或已过期的预约不可取消")
        if (appt.status == AppointmentStatus.CANCELLED) return

        appt.status = AppointmentStatus.CANCELLED
        appt.updatedAt = LocalDateTime.now()
        appointmentRepo.save(appt)
    }
}
