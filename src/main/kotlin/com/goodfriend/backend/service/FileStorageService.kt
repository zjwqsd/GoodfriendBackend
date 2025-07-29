package com.goodfriend.backend.service

import com.goodfriend.backend.config.FileStorageProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
class FileStorageService(
    private val props: FileStorageProperties
) {
    fun storeFile(file: MultipartFile, scope: String, category: String): String {
        // 生成唯一的文件名，避免重复
        val filename = UUID.randomUUID().toString() + "-" + sanitizeFilename(file.originalFilename)
        // 构建存储路径：范围/类别
        val subPath = "$scope/$category"
        val targetDir = Paths.get(props.basePath, subPath)
        Files.createDirectories(targetDir)  // 确保目标文件夹存在
        val filePath = targetDir.resolve(filename)
        file.transferTo(filePath.toFile())
        return "$subPath/$filename"
    }

    // 用于清理原始文件名中的非法字符
    private fun sanitizeFilename(originalFilename: String?): String {
        return originalFilename?.replace(Regex("[^A-Za-z0-9._-]"), "_") ?: UUID.randomUUID().toString() // 如果文件名为空，使用 UUID 代替
    }
}



