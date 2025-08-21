package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.*
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.UserOnly
import com.goodfriend.backend.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.goodfriend.backend.dto.AppointmentResponse
import java.net.URI
import com.goodfriend.backend.dto.CreateAppointmentRequest
import com.goodfriend.backend.dto.CreateReviewRequest
import com.goodfriend.backend.dto.ReviewResponse
import com.goodfriend.backend.repository.AppointmentRepository
import com.goodfriend.backend.service.AppointmentService


@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService,
    private val currentRoleService: CurrentRoleService,
    private val appointmentRepo: AppointmentRepository,
    private val appointmentService: AppointmentService
) {

    @PutMapping("/update")
    @UserOnly
    fun updateUser(
        @RequestBody @Valid req: UpdateUserRequest,
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.updateUserInfo(user.id, req)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/profile")
    @UserOnly
    fun getUserProfile(request: HttpServletRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<UserProfileResponse> {
        val user = currentRoleService.getCurrentUser(request)
        return ResponseEntity.ok(UserProfileResponse.from(user))
    }



    @PostMapping("/consultant/apply")
    @UserOnly
    fun applyToConsultant(@RequestBody @Valid req: ConsultantApplicationRequest, request: HttpServletRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.submitConsultantApplication(user.id, req)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/avatars")
    @UserOnly
    fun listAvailableAvatars(
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<List<AvatarItem>> {
        val list = userService.getAvailableUserAvatarItems()
        return ResponseEntity.ok(list)
    }


    @GetMapping("/consultant/applications")
    @UserOnly
    fun listMyConsultantApplications(
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<List<ConsultantApplicationDTO>> {
        val user = currentRoleService.getCurrentUser(request)
        val list = userService.getUserApplications(user.id)
        return ResponseEntity.ok(list)
    }

    @PostMapping("/tests")
    @UserOnly
    fun saveTestResult(
        @RequestBody @Valid req: SaveTestResultRequest,
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.saveTestResult(user, req)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/tests")
    @UserOnly
    fun getTestResults(
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<List<TestResultResponse>> {
        val user = currentRoleService.getCurrentUser(request)
        val results = userService.getUserTestResults(user)
        return ResponseEntity.ok(results)
    }

    @DeleteMapping("/tests/{id}")
    @UserOnly
    fun deleteTestResult(
        request: HttpServletRequest,
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        userService.deleteMyTestResult(user, id)
        return ResponseEntity.noContent().build()
    }


    @GetMapping("/appointments")
    @UserOnly
    fun listMyAppointments(request: HttpServletRequest): ResponseEntity<List<AppointmentResponse>> {
        val user = currentRoleService.getCurrentUser(request)
        return ResponseEntity.ok(appointmentService.getMyAppointments(user))
    }

    @PostMapping("/appointments")
    @UserOnly
    fun createMyAppointment(
        request: HttpServletRequest,
        @RequestHeader("Authorization", required = true) authorization: String,
        @Valid @RequestBody req: CreateAppointmentRequest
    ): ResponseEntity<AppointmentResponse> {
        val user = currentRoleService.getCurrentUser(request)
        val appt = appointmentService.createAppointment(user, req)
        return ResponseEntity.status(201).body(AppointmentResponse.from(appt))
    }

    @DeleteMapping("/appointments/{id}")
    @UserOnly
    fun cancelMyAppointment(
        request: HttpServletRequest,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        appointmentService.cancelMyAppointment(user, id)
        return ResponseEntity.noContent().build()
    }


    @PostMapping("/reviews")
    @UserOnly
    fun createReview(
        @RequestBody @Valid req: CreateReviewRequest,
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        val user = currentRoleService.getCurrentUser(request)
        val saved = userService.createReview(user, req)
        return ResponseEntity.created(URI.create("/api/user/reviews/${saved.id}")).build()
    }

    @GetMapping("/reviews")
    @UserOnly
    fun listMyReviews(
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<List<ReviewResponse>> {
        val user = currentRoleService.getCurrentUser(request)
        return ResponseEntity.ok(userService.getMyReviews(user))
    }


}





