package com.goodfriend.backend.controller

import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.Gender
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.security.CurrentRoleService
import com.goodfriend.backend.security.annotation.ConsultantOnly
import com.goodfriend.backend.service.ConsultantService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Size
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
}

data class UpdateConsultantRequest(
    @field:Size(min = 1, max = 20, message = "姓名不能为空")
    val name: String? = null,

    @field:Size(min = 1, max = 50, message = "工作地点不能为空")
    val location: String? = null,

    // specialty 改为数组，每项长度限制 1-50，总长度限制 1-10
    @field:Size(min = 1, max = 10, message = "请填写 1~10 个擅长领域")
    val specialty: List<@Size(min = 1, max = 50, message = "每个擅长领域长度应在1~50之间") String>? = null
)

// ✅ 固定类型写法（推荐）
data class ConsultantDTO(
    val id: Long,
    val name: String,
    val gender: Gender,
    val location: String,
    val specialty: List<String>, // 改为数组
    val level: String,
    val rating: Double,
    val avatar: String,
    val pricePerHour: Int,
) {
    companion object {
        fun from(c: Consultant): ConsultantDTO = ConsultantDTO(
            id = c.id,
            name = c.name,
            gender = c.gender,
            location = c.location,
            specialty = c.specialty,  // 确保 Consultant 中也是 List<String>
            level = c.level,
            rating = c.rating,
            avatar = c.avatar,
            pricePerHour = c.pricePerHour
        )
    }
}

