package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.ConsultantDTO
import com.goodfriend.backend.dto.ConsultantProfileResponse
import com.goodfriend.backend.dto.StaticResourceDTO
import com.goodfriend.backend.dto.UpdateConsultantRequest
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.ConsultantOnly
import com.goodfriend.backend.service.ConsultantService
import com.goodfriend.backend.service.StaticResourceService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/consultant")
class ConsultantController(
    private val consultantService: ConsultantService,
    private val currentRoleService: CurrentRoleService,
    private val consultantRepo: ConsultantRepository,
    private val staticService: StaticResourceService
) {

    @PutMapping("/update")
    @ConsultantOnly
    fun updateConsultant(
        @RequestBody @Valid req: UpdateConsultantRequest,
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        val consultant = currentRoleService.getCurrentConsultant(request)
        consultantService.updateConsultantInfo(consultant, req)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/all")
    fun getAllConsultants(): ResponseEntity<List<ConsultantDTO>> {
        val consultants = consultantRepo.findAll()
        return ResponseEntity.ok(consultants.map { ConsultantDTO.from(it) })
    }

    @GetMapping("/profile")
    @ConsultantOnly
    fun getConsultantProfile(
        request: HttpServletRequest,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<ConsultantProfileResponse> {
        val consultant = currentRoleService.getCurrentConsultant(request)
        return ResponseEntity.ok(ConsultantProfileResponse.from(consultant))
    }


    @PostMapping("/consultant/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ConsultantOnly
    fun uploadConsultantAvatar(
        request: HttpServletRequest,
        @RequestParam file: MultipartFile,
        @RequestHeader("Authorization") authHeader: String?
    ):ResponseEntity<Void> {
        val consultant = currentRoleService.getCurrentConsultant(request)
        staticService.uploadConsultantAvatarAndSet(file, consultant)
        return ResponseEntity.ok().build()
    }

}




