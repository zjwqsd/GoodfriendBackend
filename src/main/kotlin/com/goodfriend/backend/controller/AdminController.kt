package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.AdminLoginRequest
import com.goodfriend.backend.dto.ConsultantApplicationDTO
import com.goodfriend.backend.repository.ConsultantApplicationRepository
import com.goodfriend.backend.security.annotation.AdminOnly
import com.goodfriend.backend.service.AuthService
import com.goodfriend.backend.service.ConsultantService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import jakarta.validation.constraints.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val consultantService: ConsultantService,
    private val authService: AuthService,
    private val applicationRepo: ConsultantApplicationRepository,
) {

    @PostMapping("/consultant/create")
    @AdminOnly
    fun createConsultant(@RequestBody @Valid req: CreateConsultantRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<Any> {
        val consultant = consultantService.createConsultantAccount(req.phone, req.password, req.name)
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
        @RequestParam approve: Boolean,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Any> {
        consultantService.reviewApplication(id, approve)
        return ResponseEntity.ok().build()
    }

    @AdminOnly
    @GetMapping("/consultant/applications")
    fun getAllConsultantApplications(@RequestHeader("Authorization") authHeader: String?): ResponseEntity<List<ConsultantApplicationDTO>> {
        val applications = applicationRepo.findAll()
        return ResponseEntity.ok(applications.map { ConsultantApplicationDTO.from(it) })
    }
}

data class CreateConsultantRequest(
    @field:NotBlank(message = "手机号不能为空")
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String,

    @field:NotBlank(message = "密码不能为空")
    val password: String,

    @field:NotBlank(message = "姓名不能为空")
    val name: String
)






