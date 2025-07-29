package com.goodfriend.backend.service

import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.UserRepository

import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepo: UserRepository
) {

    fun updateUserInfo(id: Long, name: String?, age: Int?, gender: Gender?, region: String?) {
        val user = userRepo.findById(id).orElseThrow { ApiException(404, "用户不存在") }
        name?.let { user.name = it }
        age?.let { user.age = it }
        gender?.let { user.gender = it }
        region?.let { user.region = it }
        userRepo.save(user)
    }
}
