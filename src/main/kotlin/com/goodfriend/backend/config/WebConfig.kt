package com.goodfriend.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig(
    private val fileStorageProperties: FileStorageProperties
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val basePath = Paths.get(fileStorageProperties.basePath).toAbsolutePath().toUri().toString()
        println("File storage base path: $basePath")

        registry.addResourceHandler("/static/**") // 仅映射 covers 子目录
            .addResourceLocations(basePath)
    }
}
