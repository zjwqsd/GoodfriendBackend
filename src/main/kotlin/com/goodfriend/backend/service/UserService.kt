package com.goodfriend.backend.service

import com.goodfriend.backend.data.ApplicationStatus
import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.data.TestResult
import com.goodfriend.backend.data.User
import com.goodfriend.backend.dto.AvatarItem
import com.goodfriend.backend.dto.ConsultantApplicationDTO
import com.goodfriend.backend.dto.ConsultantApplicationRequest
import com.goodfriend.backend.dto.SaveTestResultRequest
import com.goodfriend.backend.dto.TestResultResponse
import com.goodfriend.backend.dto.UpdateUserRequest
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.repository.StaticResourceRepository
import com.goodfriend.backend.repository.TestResultRepository
import com.goodfriend.backend.repository.UserRepository

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepo: UserRepository,
    private val applicationRepo: ConsultantApplicationRepository,
    private val staticResourceRepo: StaticResourceRepository,
    private val testResultRepository: TestResultRepository
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
}
