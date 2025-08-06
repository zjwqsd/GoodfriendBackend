package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.ConsultantDTO
import com.goodfriend.backend.dto.ConsultantProfileResponse
import com.goodfriend.backend.dto.UpdateConsultantRequest
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.ConsultantOnly
import com.goodfriend.backend.service.ConsultantService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/consultant")
class ConsultantController(
    private val consultantService: ConsultantService,
    private val currentRoleService: CurrentRoleService,
    private val consultantRepo: ConsultantRepository
) {

    @PutMapping("/update")
    @ConsultantOnly
    fun updateConsultant(@RequestBody @Valid req: UpdateConsultantRequest, request: HttpServletRequest, @RequestHeader("Authorization") authHeader: String?): ResponseEntity<Void> {
        val consultant = currentRoleService.getCurrentConsultant(request)
        consultantService.updateConsultantInfo(consultant.id, req.name, req.location, req.specialty)
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
}




