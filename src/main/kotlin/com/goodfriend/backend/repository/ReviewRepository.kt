package com.goodfriend.backend.repository

import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.Review
import com.goodfriend.backend.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByUserOrderByCreatedAtDesc(user: User): List<Review>
    fun findByConsultantOrderByCreatedAtDesc(consultant: Consultant): List<Review>

    // 避免同一预约重复评价
    fun existsByAppointment_IdAndUser_Id(appointmentId: Long, userId: Long): Boolean

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.consultant = :consultant")
    fun avgRatingByConsultant(@Param("consultant") consultant: Consultant): Double?
}
