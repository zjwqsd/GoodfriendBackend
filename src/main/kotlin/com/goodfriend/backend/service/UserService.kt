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
import com.goodfriend.backend.dto.CreateReviewRequest
import com.goodfriend.backend.data.Review
import com.goodfriend.backend.dto.ReviewResponse
import com.goodfriend.backend.dto.UserProfileResponse
import com.goodfriend.backend.repository.ReviewRepository


@Service
class UserService(
    private val userRepo: UserRepository,
    private val applicationRepo: ConsultantApplicationRepository,
    private val staticResourceRepo: StaticResourceRepository,
    private val testResultRepository: TestResultRepository,
    private val consultantRepo: ConsultantRepository,
    private val appointmentRepo: AppointmentRepository,
    private val reviewRepo: ReviewRepository
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

    fun getAllUsers(): List<UserProfileResponse> {
        return userRepo.findAll().map { UserProfileResponse.from(it) }
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
    fun deleteMyTestResult(user: User, resultId: Long) {
        val tr = testResultRepository.findById(resultId)
            .orElseThrow { ApiException(404, "测试结果不存在") }

        if (tr.user.id != user.id) {
            throw ApiException(403, "无权删除他人的测试结果")
        }

        testResultRepository.delete(tr)
    }

    @Transactional
    fun createReview(user: User, req: CreateReviewRequest): Review {
        val consultantId = req.consultantId ?: throw ApiException(400, "consultantId 不能为空")
        val consultant = consultantRepo.findById(consultantId)
            .orElseThrow { ApiException(404, "咨询师不存在") }

        val rating = req.rating ?: throw ApiException(400, "rating 不能为空")
        if (rating !in 1..5) throw ApiException(400, "rating 必须在 1~5 之间")

        var appointment: Appointment? = null
        req.appointmentId?.let { apptId ->
            // 校验预约存在、归属该用户和该咨询师、并且已结束
            appointment = appointmentRepo.findById(apptId)
                .orElseThrow { ApiException(404, "预约不存在") }
            val a = appointment!!
            if (a.user.id != user.id) throw ApiException(403, "无权评价他人的预约")
            if (a.consultant.id != consultantId) throw ApiException(400, "预约不属于该咨询师")
            if (a.status == AppointmentStatus.CANCELLED) throw ApiException(400, "已取消的预约不可评价")
            if (LocalDateTime.now().isBefore(a.endTime)) throw ApiException(400, "预约尚未结束，暂不可评价")

            if (reviewRepo.existsByAppointment_IdAndUser_Id(apptId, user.id)) {
                throw ApiException(409, "该预约已评价")
            }
        }

        // 处理标签（去空白、去重、最多 5 个、小写）
        val tags = req.tags.orEmpty()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(5)
            .map { it.lowercase() }

        val review = Review(
            user = user,
            consultant = consultant,
            appointment = appointment,
            rating = rating,
            content = req.content?.trim(),
            tags = tags
        )
        val saved = reviewRepo.save(review)

        // 更新咨询师平均评分（基于所有评论重算，简单可靠）
        val avg = reviewRepo.avgRatingByConsultant(consultant) ?: 0.0
        consultant.rating = String.format("%.2f", avg).toDouble()
        consultant.updatedAt = LocalDateTime.now()
        consultantRepo.save(consultant)

        return saved
    }

    @Transactional(readOnly = true)
    fun getMyReviews(user: User): List<ReviewResponse> {
        return reviewRepo.findByUserOrderByCreatedAtDesc(user).map { ReviewResponse.from(it) }
    }
}
