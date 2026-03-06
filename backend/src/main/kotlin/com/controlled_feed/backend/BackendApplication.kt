package com.controlled_feed.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.cache.annotation.EnableCaching
@SpringBootApplication
@EnableScheduling
@EnableCaching
class BackendApplication

fun main(args: Array<String>) {
	runApplication<BackendApplication>(*args)
}
