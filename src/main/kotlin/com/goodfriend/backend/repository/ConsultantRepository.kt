package com.goodfriend.backend.repository

import com.goodfriend.backend.data.Consultant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsultantRepository : JpaRepository<Consultant, Long> {

    fun findByPhone(phone: String): Consultant?

    fun existsByPhone(phone: String): Boolean
}
