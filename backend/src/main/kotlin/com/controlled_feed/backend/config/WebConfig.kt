package com.controlled_feed.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.io.File

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val baseDir = object {}.javaClass.protectionDomain.codeSource.location
            .toURI().let { java.io.File(it) }
            .parentFile  // target/classes → target
            ?.parentFile // target → backend
            ?: java.io.File(System.getProperty("user.dir"))
        val uploadPath = java.io.File(baseDir, "uploads/").absolutePath
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$uploadPath/")
    }
}