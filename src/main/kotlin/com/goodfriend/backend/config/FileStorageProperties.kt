package com.goodfriend.backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "file.local")
class FileStorageProperties {
    lateinit var basePath: String
}