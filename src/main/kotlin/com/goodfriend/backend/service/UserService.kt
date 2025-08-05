package com.goodfriend.backend.service

import com.goodfriend.backend.controller.ConsultantApplicationRequest
import com.goodfriend.backend.data.ApplicationStatus
import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.repository.UserRepository

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepo: UserRepository,
    private val applicationRepo: ConsultantApplicationRepository,
) {

    fun updateUserInfo(
        userId: Long,
        name: String?,
        age: Int?,
        gender: Gender?,
        region: String?,
        avatar: String?,
        birthday: LocalDate?,
        hobby: String?
    ) {
        val user = userRepo.findById(userId).orElseThrow { ApiException(404, "用户不存在") }

        name?.let { user.name = it }
        age?.let { user.age = it }
        gender?.let { user.gender = it }
        region?.let { user.region = it }
        avatar?.let { user.avatar = it }
        birthday?.let { user.birthday = it }
        hobby?.let { user.hobby = it }

        user.updatedAt = LocalDateTime.now()
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
}
