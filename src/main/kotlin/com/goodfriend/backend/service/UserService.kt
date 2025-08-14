package com.goodfriend.backend.service

import com.goodfriend.backend.data.ApplicationStatus
import com.goodfriend.backend.data.Appointment
import com.goodfriend.backend.data.AppointmentStatus
import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.data.TestResult
import com.goodfriend.backend.data.User
import com.goodfriend.backend.dto.AppointmentResponse
import com.goodfriend.backend.dto.AvatarItem
import com.goodfriend.backend.dto.ConsultantApplicationDTO
import com.goodfriend.backend.dto.ConsultantApplicationRequest
import com.goodfriend.backend.dto.SaveTestResultRequest
import com.goodfriend.backend.dto.TestResultResponse
import com.goodfriend.backend.dto.UpdateUserRequest
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.AppointmentRepository
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.repository.StaticResourceRepository
import com.goodfriend.backend.repository.TestResultRepository
import com.goodfriend.backend.repository.UserRepository

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.goodfriend.backend.dto.CreateAppointmentRequest
import java.net.URI
import java.time.Duration
import java.time.LocalDateTime



@Service
class UserService(
    private val userRepo: UserRepository,
    private val applicationRepo: ConsultantApplicationRepository,
    private val staticResourceRepo: StaticResourceRepository,
    private val testResultRepository: TestResultRepository,
    private val consultantRepo: ConsultantRepository,
    private val appointmentRepo: AppointmentRepository
) {

    fun updateUserInfo(userId: Long, req: UpdateUserRequest) {
        val user = userRepo.findById(userId).orElseThrow { RuntimeException("用户不存在") }

        req.name?.let { user.name = it }
        req.age?.let { user.age = it }
        req.gender?.let { user.gender = it }
        req.region?.let { user.region = it }
        req.birthday?.let { user.birthday = it }
        req.hobby?.let { user.hobby = it }

        // ✅ 设置头像（如传入）
        if (!req.avatar.isNullOrBlank()) {
            val validAvatars = staticResourceRepo.findByScopeAndCategoryAndValid("user", "avatars", true)
            val match = validAvatars.find { it.filename.substringBeforeLast('.') == req.avatar }
                ?: throw ApiException(400,"未找到匹配的头像资源")
            user.avatar = match.getPathSuffix()
        }

        userRepo.save(user)
    }




    fun submitConsultantApplication(userId: Long, req: ConsultantApplicationRequest) {
        if (applicationRepo.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw ApiException(400, "已有申请正在审核中")
        }

        val app = ConsultantApplication(
            userId = userId,
            name = req.name,
            idCardNumber = req.idCardNumber,
            phone = req.phone,
            education = req.education,
            university = req.university,
            major = req.major,
            licenseNumber = req.licenseNumber,
            experienceYears = req.experienceYears,
            specialty = req.specialty,
            bio = req.bio,
            reason = req.reason
        )

        applicationRepo.save(app)
    }




    fun getAvailableUserAvatarItems(): List<AvatarItem> {
        val scope = "user"
        val category = "avatars"
        return staticResourceRepo.findByScopeAndCategoryAndValid(scope, category, true)
            .map { res ->
                val nameWithoutExt = res.filename.substringBeforeLast('.')
                AvatarItem(
                    name = nameWithoutExt,
                    file = "$scope/$category/${res.filename}"
                )
            }
    }

    fun updateAvatar(userId: Long, filenameWithoutExt: String) {
        val candidates = staticResourceRepo.findByScopeAndCategoryAndValid("user", "avatars", true)
        val match = candidates.find { it.filename.substringBeforeLast('.') == filenameWithoutExt }
            ?: throw IllegalArgumentException("未找到匹配的头像资源")

        val user = userRepo.findById(userId).orElseThrow { RuntimeException("用户不存在") }
        user.avatar = match.getPathSuffix()
        userRepo.save(user)
    }


    fun getUserApplications(userId: Long): List<ConsultantApplicationDTO> {
        return applicationRepo.findByUserIdOrderByCreatedAtDesc(userId)
            .map { ConsultantApplicationDTO.from(it) }
    }

    fun getUserById(id: Long): User? {
        return userRepo.findById(id).orElse(null)
    }

    fun saveTestResult(user: User, request: SaveTestResultRequest) {
        // 不再需要根据 userId 再查一次用户
        val testResult = TestResult(
            user = user,
            testName = request.testName,
            score = request.score
        )
        testResultRepository.save(testResult)
    }

    @Transactional(readOnly = true)
    fun getUserTestResults(user: User): List<TestResultResponse> {
        return testResultRepository.findByUserOrderByCreatedAtDesc(user).map {
            TestResultResponse(
                id = it.id,
                testName = it.testName,
                score = it.score,
                createdAt = it.createdAt.toString()
            )
        }
    }

    @Transactional
    fun createAppointment(user: User, req: CreateAppointmentRequest): Appointment {
        val consultantId = req.consultantId ?: throw ApiException(400, "consultantId 不能为空")
        val start = req.startTime ?: throw ApiException(400, "startTime 不能为空")
        val end = req.endTime ?: throw ApiException(400, "endTime 不能为空")

        if (!end.isAfter(start)) throw ApiException(400, "结束时间必须晚于开始时间")

        // 可以按需限制预约时长（例如 30 分钟~3 小时）
        val minutes = Duration.between(start, end).toMinutes()
        if (minutes < 15) throw ApiException(400, "预约时长不能少于 15 分钟")
        if (minutes > 180) throw ApiException(400, "预约时长不能超过 180 分钟")

        val consultant = consultantRepo.findById(consultantId)
            .orElseThrow { ApiException(404, "咨询师不存在") }

        // 冲突检测（仅检查咨询师在该时间段的其他活跃预约）
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

    /**
     * 查询我的预约
     */
    @Transactional(readOnly = true)
    fun getMyAppointments(user: User): List<AppointmentResponse> {
        return appointmentRepo.findByUserOrderByStartTimeDesc(user).map { AppointmentResponse.from(it) }
    }

    /**
     * 取消我的预约（仅允许取消自己的且未开始的预约）
     */
    @Transactional
    fun cancelMyAppointment(user: User, appointmentId: Long) {
        val appt = appointmentRepo.findById(appointmentId).orElseThrow { ApiException(404, "预约不存在") }
        if (appt.user.id != user.id) throw ApiException(403, "无权取消他人预约")
        if (!LocalDateTime.now().isBefore(appt.startTime)) throw ApiException(400, "已开始或已过期的预约不可取消")
        if (appt.status == AppointmentStatus.CANCELLED) return

        appt.status = AppointmentStatus.CANCELLED
        appt.updatedAt = LocalDateTime.now()
        appointmentRepo.save(appt)
    }
}
