package com.goodfriend.backend.controller

import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.ConsultantOnly
import com.goodfriend.backend.service.ConsultantService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/consultant")
class ConsultantController(
    private val consultantService: ConsultantService,
    private val currentRoleService: CurrentRoleService
) {

    @PutMapping("/update")
    @ConsultantOnly
    fun updateConsultant(@RequestBody @Valid req: UpdateConsultantRequest, request: HttpServletRequest): ResponseEntity<Void> {
        val consultant = currentRoleService.getCurrentConsultant(request)
        consultantService.updateConsultantInfo(consultant.id, req.name, req.location, req.specialty)
        return ResponseEntity.ok().build()
    }
}

data class UpdateConsultantRequest(
    @field:Size(min = 1, max = 20, message = "姓名不能为空")
    val name: String? = null,

    @field:Size(min = 1, max = 50, message = "工作地点不能为空")
    val location: String? = null,

    @field:Size(min = 1, max = 50, message = "专长不能为空")
    val specialty: String? = null
)
