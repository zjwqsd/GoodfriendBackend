package com.goodfriend.backend.service

import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class ConsultantService(
    private val consultantRepo: ConsultantRepository,
    private val passwordEncoder: PasswordEncoder
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
            location = "未知",
            level = "普通咨询师",
            specialty = "未填写",
            rating = 0.0,
            avatar = "consultant/avatars/default.jpg",
            experienceYears = 0,
            consultationCount = 0,
            pricePerHour = 0
        )
        return consultantRepo.save(consultant)
    }

    fun updateConsultantInfo(id: Long, name: String?, location: String?, specialty: String?) {
        val consultant = consultantRepo.findById(id).orElseThrow { ApiException(404, "咨询师不存在") }
        name?.let { consultant.name = it }
        location?.let { consultant.location = it }
        specialty?.let { consultant.specialty = it }
        consultantRepo.save(consultant)
    }
}