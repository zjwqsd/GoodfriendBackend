package com.goodfriend.backend.service

import com.goodfriend.backend.data.*
import com.goodfriend.backend.data.AppointmentStatus.*
import com.goodfriend.backend.dto.AppointmentResponse
import com.goodfriend.backend.dto.UpdateConsultantRequest
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.AppointmentRepository
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.repository.ConsultantRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import com.goodfriend.backend.dto.ReviewResponse
import com.goodfriend.backend.dto.ConsultantReviewStatsResponse
import com.goodfriend.backend.dto.TagCount
import com.goodfriend.backend.dto.TestResultResponse
import com.goodfriend.backend.repository.ReviewRepository
import com.goodfriend.backend.repository.TestResultRepository

@Service
class ConsultantService(
    private val consultantRepo: ConsultantRepository,
    private val passwordEncoder: PasswordEncoder,
    private val applicationRepo: ConsultantApplicationRepository,
    private val consultantRepository: ConsultantRepository,
    private val appointmentRepo: AppointmentRepository,
    private val reviewRepo: ReviewRepository,
    private val testResultRepository: TestResultRepository
) {

    fun createConsultantAccount(phone: String, password: String, name: String): Consultant {
        if (consultantRepo.existsByPhone(phone)) {
            throw ApiException(400, "手机号已被使用")
        }
        val consultant = Consultant(
            phone = phone,
            password = passwordEncoder.encode(password), // 密码建议加密，视使用场景决定是否延后处理
            name = name,
            gender = Gender.UNKNOWN,
            location = null,
            level = "普通咨询师",
            specialty = emptyList(),
            rating = 0.0,
            avatar = "consultant/avatars/default.jpg",
            experienceYears = 0,
            consultationCount = 0,
            pricePerHour = 0
        )
        return consultantRepo.save(consultant)
    }

    fun updateConsultantInfo(consultant: Consultant, req: UpdateConsultantRequest) {
        req.name?.let { consultant.name = it }
        req.gender?.let { consultant.gender = it }
        req.location?.let { consultant.location = it }
        // updateConsultantInfo：过滤空白项
        req.specialty?.let { list -> consultant.specialty = list.filter { it.isNotBlank() } }
        req.experienceYears?.let { consultant.experienceYears = it }
        req.consultationCount?.let { consultant.consultationCount = it }
        req.trainingHours?.let { consultant.trainingHours = it }
        req.supervisionHours?.let { consultant.supervisionHours = it }
        req.bio?.let { consultant.bio = it }
        req.consultationMethods?.let { consultant.consultationMethods = it }
        req.availability?.let { consultant.availability = it }
        req.pricePerHour?.let { consultant.pricePerHour = it }

        req.educationList?.let {
            consultant.educationList = it.map { edu ->
                Education(
                    degree = edu.degree,
                    school = edu.school,
                    major = edu.major,
                    time = edu.time
                )
            }
        }

        req.experienceList?.let {
            consultant.experienceList = it.map { exp ->
                Experience(
                    company = exp.company,
                    position = exp.position,
                    duration = exp.duration,
                    description = exp.description
                )
            }
        }

        req.certificationList?.let {
            consultant.certificationList = it.map { cert ->
                Certification(
                    name = cert.name,
                    number = cert.number,
                    issuer = cert.issuer,
                    date = cert.date
                )
            }
        }

        consultant.updatedAt = LocalDateTime.now()
        consultantRepository.save(consultant)
    }




    fun reviewApplication(applicationId: Long, approve: Boolean, rejectReason: String? = null) {
        val application = applicationRepo.findById(applicationId)
            .orElseThrow { ApiException(404, "申请不存在") }

        if (application.status != ApplicationStatus.PENDING) {
            throw ApiException(400, "该申请已被处理")
        }

        if (approve) {
            val consultant = Consultant(
                name = application.name,
                phone = application.phone,
                password = "NULL_PASSWORD",
                level = "初级咨询师",
                specialty = application.specialty,
                gender = Gender.UNKNOWN,
                location = "未知",
                rating = 0.0,
                avatar = "consultant/avatars/default.jpg",
                experienceYears = application.experienceYears,
                consultationCount = 0,
                pricePerHour = 0
            )
            consultantRepo.save(consultant)
            application.status = ApplicationStatus.APPROVED
            application.reviewComment = null
        } else {
            application.status = ApplicationStatus.REJECTED
            application.reviewComment = rejectReason?.trim()
        }

        application.updatedAt = LocalDateTime.now()
        applicationRepo.save(application)


    }

    @Transactional(readOnly = true)
    fun listAppointmentsOf(consultant: Consultant): List<AppointmentResponse> {
        val appts = appointmentRepo.findByConsultantOrderByStartTimeDesc(consultant)
        if (appts.isEmpty()) return emptyList()

        // 收集本次结果中涉及的所有来访者（User）
        val users = appts.map { it.user }.distinctBy { it.id }

        // 为每位用户取最近 3 条测评，并转成 DTO
        val recentMap: Map<Long, List<TestResultResponse>> = users.associate { user ->
            val trs = testResultRepository.findTop3ByUserOrderByCreatedAtDesc(user)
            user.id to trs.map {
                TestResultResponse(
                    id = it.id,
                    testName = it.testName,
                    score = it.score,
                    createdAt = it.createdAt.toString()
                )
            }
        }

        // 组装响应
        return appts.map { a ->
            AppointmentResponse.from(
                a = a,
                testResults = recentMap[a.user.id] ?: emptyList()
            )
        }
    }

    @Transactional
    fun cancelMyAppointment(consultant: Consultant, appointmentId: Long, reason: String?) {
        // 确保只能操作“自己的预约”
        val appt = appointmentRepo.findByIdAndConsultant(appointmentId, consultant)
            .orElseThrow { ApiException(404, "预约不存在") }

        // 业务校验：不可重复取消/已完成不可取消/已开始不可取消
        when (appt.status) {
            AppointmentStatus.CANCELLED_BY_CONSULTANT,
            AppointmentStatus.CANCELLED_BY_USER ->
                throw ApiException(400, "该预约已被取消")
            AppointmentStatus.COMPLETED ->
                throw ApiException(400, "已完成的预约不可取消")
            else -> { /* 继续 */ }
        }

        val now = LocalDateTime.now()
        if (!appt.startTime.isAfter(now)) {
            throw ApiException(400, "已开始或已结束的预约不可取消")
        }

        // 如需强约束“距开始 < 24h 不可取消”，可放开下面一行
        // if (appt.startTime.isBefore(now.plusHours(24))) throw ApiException(400, "距开始不足24小时不可取消")

        appt.status = AppointmentStatus.CANCELLED_BY_CONSULTANT
        // 若实体有以下字段则赋值（没有就删掉这两行或按你们实体名修改）
        appt.cancelReason = reason?.takeIf { it.isNotBlank() }
        appt.updatedAt = now

        appointmentRepo.save(appt)

        // TODO: 如需通知来访者（短信/站内信/邮件）、回收时段、退款等，可在此处触发
    }

    @Transactional
    fun confirmMyAppointment(consultant: Consultant, appointmentId: Long) {
        val appt = appointmentRepo.findByIdAndConsultant(appointmentId, consultant)
            .orElseThrow { ApiException(404, "预约不存在") }

        // 业务校验：只能从 PENDING -> CONFIRMED
        when (appt.status) {
            PENDING -> { /* 允许确认 */ }
            CONFIRMED ->
                throw ApiException(400, "该预约已确认")
            COMPLETED ->
                throw ApiException(400, "已完成的预约不可确认")
            CANCELLED_BY_USER,
            CANCELLED_BY_CONSULTANT ->
                throw ApiException(400, "已取消的预约不可确认")

            CANCELLED -> TODO()
        }

        val now = LocalDateTime.now()
        if (!appt.startTime.isAfter(now)) {
            throw ApiException(400, "已开始或已结束的预约不可确认")
        }

        appt.status = AppointmentStatus.CONFIRMED
        // 如实体存在 confirmedAt/updatedAt 字段可一并维护
        // appt.confirmedAt = now
        appt.updatedAt = now

        appointmentRepo.save(appt)

        // TODO: 如需通知来访者/发站内信/短信、同步日程、锁定时段等，可在此处触发
    }

    @Transactional(readOnly = true)
    fun listMyReviews(consultant: Consultant): List<ReviewResponse> {
        return reviewRepo.findByConsultantOrderByCreatedAtDesc(consultant)
            .map { ReviewResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun reviewStats(consultant: Consultant): ConsultantReviewStatsResponse {
        val reviews = reviewRepo.findByConsultantOrderByCreatedAtDesc(consultant)
        val avg = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
        val tagCounts = reviews.flatMap { it.tags }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { TagCount(it.key, it.value.toLong()) }

        return ConsultantReviewStatsResponse(
            consultantId = consultant.id,
            avgRating = String.format("%.2f", avg).toDouble(),
            totalReviews = reviews.size.toLong(),
            tags = tagCounts
        )
    }
}