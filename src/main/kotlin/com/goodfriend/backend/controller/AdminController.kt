package com.goodfriend.backend.controller

import com.goodfriend.backend.exception.ApiException
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.security.annotation.AdminOnly
import com.goodfriend.backend.service.AuthService
import com.goodfriend.backend.service.ConsultantService
import com.goodfriend.backend.service.FileStorageService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import jakarta.validation.constraints.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val consultantService: ConsultantService,
    private val authService: AuthService,
    private val fileStorageService: FileStorageService,
    private val consultantRepo: ConsultantRepository
) {

    @PostMapping("/consultant/create")
    @AdminOnly
    fun createConsultant(@RequestBody @Valid req: CreateConsultantRequest): ResponseEntity<Any> {
        val consultant = consultantService.createConsultantAccount(req.phone, req.password, req.name)
        return ResponseEntity.ok(mapOf("id" to consultant.id))
    }

    @PostMapping("/login")
    fun login(@RequestBody req: AdminLoginRequest): ResponseEntity<String> {
        val token = authService.loginAdmin(req.username, req.password)
        return ResponseEntity.ok(token)
    }

//    @PutMapping("/consultants/{id}/avatar")
//    @AdminOnly
//    fun updateAvatar(
//        @PathVariable id: Long,
//        @RequestParam file: MultipartFile
//    ): ResponseEntity<Map<String, String>> {
//        val consultant = consultantRepo.findById(id)
//            .orElseThrow { ApiException(404, "咨询师不存在") }
//
//        val relativePath = fileStorageService.storeAvatar(file)
//        consultant.avatar = relativePath
//        consultantRepo.save(consultant)
//
//        return ResponseEntity.ok(mapOf("avatar" to relativePath))
//    }

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


data class AdminLoginRequest(
    @field:NotBlank(message = "管理员用户名不能为空")
    val username: String,

    @field:NotBlank(message = "管理员密码不能为空")
    val password: String
)