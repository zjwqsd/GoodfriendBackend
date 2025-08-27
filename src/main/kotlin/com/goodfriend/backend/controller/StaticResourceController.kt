package com.goodfriend.backend.controller

import com.goodfriend.backend.dto.StaticResourceDTO
import com.goodfriend.backend.dto.UploadStaticResourceForm
import com.goodfriend.backend.security.annotation.AdminOnly
import com.goodfriend.backend.service.StaticResourceService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile

import java.util.*

typealias StaticResourceTree = Map<String, Map<String, List<StaticResourceDTO>>>

@RestController
@RequestMapping("/api/static")
class StaticResourceController(
    private val staticService: StaticResourceService
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @AdminOnly
    fun upload(
        @RequestHeader("Authorization") authHeader: String?,
        @Valid @ModelAttribute form: UploadStaticResourceForm
    ): ResponseEntity<StaticResourceDTO> {
        val resource = staticService.uploadStaticFile(
            file = form.file,
            scope = form.scope,
            category = form.category,
            filename = form.filename,
            description = form.description
        )
        return ResponseEntity.ok(StaticResourceDTO.from(resource))
    }





    @AdminOnly
    @GetMapping
    fun list(
        @RequestParam(required = false) valid: Boolean?,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<StaticResourceTree> {
        val tree = staticService.getStaticResourceTree(valid)
        return ResponseEntity.ok(tree)
    }

    @AdminOnly
    @PatchMapping("/{id}")
    fun markValid(
        @PathVariable id: Long,
        @RequestParam valid: Boolean,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<StaticResourceDTO> {
        val updated = staticService.markValid(id, valid)
        return ResponseEntity.ok(StaticResourceDTO.from(updated))
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String?
    ): ResponseEntity<Void> {
        staticService.deleteResource(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/upload/wish-image")
    fun uploadWishImage(
        @RequestPart("file") file: MultipartFile
    ): StaticResourceDTO {
        // 生成无点扩展名的基础文件名（StaticResourceService 会自动补扩展名）
        val base = UUID.randomUUID().toString().replace("-", "")
        val res = staticService.uploadStaticFile(
            file = file,
            scope = "wish",
            category = "images",
            filename = base,
            description = "心语图片"
        )
        return StaticResourceDTO.from(res)
    }
}










