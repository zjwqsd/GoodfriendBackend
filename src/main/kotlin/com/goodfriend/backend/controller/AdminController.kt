package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.AdminAppointmentResponse
import com.goodfriend.backend.dto.AdminLoginRequest
import com.goodfriend.backend.dto.ConsultantApplicationDTO
import com.goodfriend.backend.dto.UserProfileResponse
import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.security.annotation.AdminOnly
import com.goodfriend.backend.service.AuthService
import com.goodfriend.backend.service.ConsultantService
import com.goodfriend.backend.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import jakarta.validation.constraints.*
import org.springframework.web.bind.annotation.*
import com.goodfriend.backend.repository.AppointmentRepository
import org.springframework.data.domain.Sort


@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val consultantService: ConsultantService,
    private val authService: AuthService,
    private val applicationRepo: ConsultantApplicationRepository,
    private val userService: UserService,
    private val appointmentRepo: AppointmentRepository
) {

    @PostMapping("/consultant/create")
    @AdminOnly
    fun createConsultant(@RequestBody @Valid req: CreateConsultantRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<Any> {
        val consultant = consultantService.createConsultantAccount(req.phone, "NO_PASSWORD", req.name)
        return ResponseEntity.ok(mapOf("id" to consultant.id))
    }

    @PostMapping("/login")
    fun login(@RequestBody req: AdminLoginRequest): ResponseEntity<String> {
        val token = authService.loginAdmin(req.username, req.password)
        return ResponseEntity.ok(token)
    }

    @PutMapping("/consultant/application/{id}/review")
    @AdminOnly
    fun reviewConsultantApplication(
        @PathVariable id: Long,
        @RequestBody @Valid req: ReviewApplicationRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Any> {
        if (!req.approve && req.rejectReason.isNullOrBlank()) {
            throw ApiException(400, "拒绝申请时必须填写原因")
        }
        consultantService.reviewApplication(id, req.approve, req.rejectReason)
        return ResponseEntity.ok().build()
    }

    @AdminOnly
    @GetMapping("/consultant/applications")
    fun getAllConsultantApplications(@RequestHeader("Authorization") authHeader: String?): ResponseEntity<List<ConsultantApplicationDTO>> {
        val applications = applicationRepo.findAll()
        return ResponseEntity.ok(applications.map { ConsultantApplicationDTO.from(it) })
    }

    @GetMapping("/users")
    @AdminOnly
    fun getAllUserProfiles(
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<List<UserProfileResponse>> {
        val users = userService.getAllUsers()
        return ResponseEntity.ok(users)
    }


    @GetMapping("/user/{id}")
    @AdminOnly
    fun getUserProfileById(
        @PathVariable id: Long
    ): ResponseEntity<UserProfileResponse> {
        val user = userService.getUserById(id)
            ?: throw ApiException(404, "用户不存在")
        return ResponseEntity.ok()
            .header("message", "查询成功")  // 添加自定义 message 头
            .body(UserProfileResponse.from(user))
    }

    @GetMapping("/appointments")
    @AdminOnly
    fun listAllAppointments(
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<List<AdminAppointmentResponse>> {
        val list = appointmentRepo
            .findAll(Sort.by(Sort.Direction.DESC, "startTime"))
            .map { AdminAppointmentResponse.from(it) }
        return ResponseEntity.ok(list)
    }

}

data class CreateConsultantRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

//    @field:NotBlank(message = "密码不能为空")
//    val password: String,

    @field:NotBlank(message = "姓名不能为空")
    val name: String
)

data class ReviewApplicationRequest(
    val approve: Boolean,

    @field:Size(max = 300, message = "拒绝原因不能超过300字")
    val rejectReason: String? = null
)






