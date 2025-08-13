package com.goodfriend.backend.dto

import com.goodfriend.backend.data.StaticResource
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

typealias StaticResourceTree = Map<String, Map<String, List<StaticResourceDTO>>>

data class StaticResourceDTO(
    val id: Long,
    val filename: String,
    val valid: Boolean,
    val description: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(entity: StaticResource) = StaticResourceDTO(
            id = entity.id,
            filename = entity.filename,
            valid = entity.valid,
            description = entity.description,
            createdAt = entity.createdAt
        )
    }
}

data class UploadStaticResourceForm(

    @field:Size(max = 20)
    @field:Pattern(regexp = "^[a-z]+$", message = "scope 只能包含小写字母")
    val scope: String,

    @field:Size(max = 20)
    @field:Pattern(regexp = "^[a-z]+$", message = "category 只能包含小写字母")
    val category: String,

    @field:Size(max = 50)
    @field:Pattern(regexp = "^[a-z0-9._-]+$", message = "文件名不合法")
    val filename: String,

    val description: String? = null,

    @field:NotNull(message = "文件不能为空")
    val file: MultipartFile
)

data class AvatarItem(
    val name: String,   // 去掉扩展名的文件名
    val file: String    // 带扩展名的完整文件名（不含 /upload）
)