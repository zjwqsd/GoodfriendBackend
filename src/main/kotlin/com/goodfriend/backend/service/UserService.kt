package com.goodfriend.backend.service

import com.goodfriend.backend.data.ApplicationStatus
import com.goodfriend.backend.data.ConsultantApplication
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.repository.UserRepository

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepo: UserRepository,
    private val applicationRepo: ConsultantApplicationRepository,
) {

    fun updateUserInfo(id: Long, name: String?, age: Int?, gender: Gender?, region: String?) {
        val user = userRepo.findById(id).orElseThrow { ApiException(404, "用户不存在") }
        name?.let { user.name = it }
        age?.let { user.age = it }
        gender?.let { user.gender = it }
        region?.let { user.region = it }
        userRepo.save(user)
    }

    fun submitConsultantApplication(userId: Long, specialty: String, reason: String) {
        if (applicationRepo.existsByUserIdAndStatus(userId, ApplicationStatus.PENDING)) {
            throw ApiException(400, "已有申请正在审核中")
        }
        val app = ConsultantApplication(userId = userId, specialty = specialty, reason = reason)
        applicationRepo.save(app)
    }
}
