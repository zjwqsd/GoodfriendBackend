package com.goodfriend.backend.service

import com.goodfriend.backend.config.FileStorageProperties
import com.goodfriend.backend.data.Consultant
import com.goodfriend.backend.data.StaticResource
import com.goodfriend.backend.dto.StaticResourceDTO
import com.goodfriend.backend.dto.StaticResourceTree
import com.goodfriend.backend.repository.ConsultantRepository
import com.goodfriend.backend.repository.StaticResourceRepository
import jakarta.annotation.PostConstruct
//import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

@Service
class StaticResourceService(
    private val staticRepo: StaticResourceRepository,
    private val storageProps: FileStorageProperties,
    private val consultantRepo: ConsultantRepository,
) {
    private lateinit var rootPath: Path
    @PostConstruct
    fun init() {
        rootPath = Paths.get(storageProps.basePath).toAbsolutePath().normalize()
        Files.createDirectories(rootPath)
    }

    fun uploadStaticFile(
        file: MultipartFile,
        scope: String,
        category: String,
        filename: String,
        description: String?
    ): StaticResource {
        require(scope.matches(Regex("^[a-z]{1,20}$"))) { "scope 不合法" }
        require(category.matches(Regex("^[a-z]{1,20}$"))) { "category 不合法" }
        require(filename.matches(Regex("^[a-z0-9._-]{1,50}$"))) { "文件名不合法" }

        val pathSuffix = "$scope/$category/$filename"
        val targetPath = rootPath.resolve(pathSuffix).normalize()
        Files.createDirectories(targetPath.parent)

        // 保存文件（会覆盖原文件）
        try {
            file.transferTo(targetPath.toFile())
        } catch (e: IOException) {
            throw RuntimeException("文件保存失败", e)
        }

        val existing = staticRepo.findByScopeAndCategoryAndFilename(scope, category, filename)
        return if (existing != null) {
            // 覆盖已有资源：只更新 description 和 valid 字段
            val updated = existing.copy(
                description = description,
                valid = existing.valid,
                createdAt = existing.createdAt // 保持原创建时间
            )
            staticRepo.save(updated)
        } else {
            // 创建新资源
            val resource = StaticResource(
                scope = scope,
                category = category,
                filename = filename,
                description = description,
                valid = true,
                createdAt = LocalDateTime.now()
            )
            staticRepo.save(resource)
        }
    }


    fun markValid(id: Long, valid: Boolean): StaticResource {
        val resource = staticRepo.findById(id).orElseThrow { RuntimeException("资源不存在") }
        val updated = resource.copy(valid = valid)
        return staticRepo.save(updated)
    }

    fun getStaticResourceTree(valid: Boolean? = null): StaticResourceTree {
        val resources = when (valid) {
            null -> staticRepo.findAll()
            else -> staticRepo.findByValid(valid)
        }

        return resources.groupBy { it.scope }.mapValues { (_, byScope) ->
            byScope.groupBy { it.category }.mapValues { (_, list) ->
                list.map { StaticResourceDTO.from(it) }
            }
        }
    }

    fun deleteResource(id: Long) {
        val resource = staticRepo.findById(id).orElseThrow { RuntimeException("资源不存在") }

        val filePath = rootPath.resolve(resource.getPathSuffix()).normalize()
        require(filePath.startsWith(rootPath)) { "路径越界，拒绝删除" }
        Files.deleteIfExists(filePath)

        staticRepo.deleteById(id)
    }

    fun uploadConsultantAvatarAndSet(
        file: MultipartFile,
        consultant: Consultant
    ) {
        // 1. 自动推断文件扩展名
        val contentType = file.contentType ?: "image/jpeg"
        val extension = when (contentType) {
            "image/png" -> "png"
            "image/jpeg", "image/jpg" -> "jpg"
            else -> throw IllegalArgumentException("不支持的图片类型")
        }

        // 2. 文件名固定为 {id}.{ext}
        val filename = "${consultant.id}.$extension"

        // 3. 上传文件
        val resource = uploadStaticFile(
            file = file,
            scope = "consultant",
            category = "avatars",
            filename = filename,
            description = "咨询师 ${consultant.name} 的头像"
        )

        // 4. 更新头像路径并保存数据库
        consultant.avatar = resource.getPathSuffix()
        consultantRepo.save(consultant)
    }

}
