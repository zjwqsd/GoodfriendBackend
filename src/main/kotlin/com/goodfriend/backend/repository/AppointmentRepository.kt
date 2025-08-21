package com.goodfriend.backend.repository

import com.goodfriend.backend.data.Appointment
import com.goodfriend.backend.data.AppointmentStatus
import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.Optional

interface AppointmentRepository : JpaRepository<Appointment, Long> {

    fun findByUserOrderByStartTimeDesc(user: User): List<Appointment>
    fun findByConsultantOrderByStartTimeDesc(consultant: Consultant): List<Appointment>

    fun findByIdAndConsultant(id: Long, consultant: Consultant): Optional<Appointment>

    @Query(
        """
        SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
        FROM Appointment a
        WHERE a.consultant = :consultant
          AND a.status IN :activeStatuses
          AND (:start < a.endTime AND :end > a.startTime)
        """
    )
    fun existsConsultantTimeOverlap(
        @Param("consultant") consultant: Consultant,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        @Param("activeStatuses") activeStatuses: List<AppointmentStatus> = listOf(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
    ): Boolean
}
