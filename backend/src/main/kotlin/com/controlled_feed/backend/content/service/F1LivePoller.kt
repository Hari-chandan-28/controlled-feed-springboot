package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference
import kotlin.text.get

@Component
class F1LivePoller(
    private val dashboardService: F1DashboardService,
    // Toggle via application.properties: f1.mock-mode=true
    @Value("\${f1.mock-mode:false}") private val mockMode: Boolean
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val latestSnapshot = AtomicReference<Map<String, Any?>>(emptyMap())

    // Fake grid — driver number, name, team — used ONLY in mock mode
    private val mockDrivers = listOf(
        Triple(1, "Max Verstappen", "Red Bull Racing"),
        Triple(16, "Charles Leclerc", "Ferrari"),
        Triple(4, "Lando Norris", "McLaren"),
        Triple(44, "Lewis Hamilton", "Mercedes"),
        Triple(63, "George Russell", "Mercedes"),
        Triple(55, "Carlos Sainz", "Williams"),
        Triple(81, "Oscar Piastri", "McLaren"),
        Triple(11, "Sergio Perez", "Red Bull Racing")
    )

    @Scheduled(fixedDelay = 6000)
    fun pollOpenF1() {
        if (mockMode) {
            latestSnapshot.set(generateMockSnapshot())
            return
        }

        try {
            val positions = dashboardService.getLiveDriverPositions()
            val timing = dashboardService.getLiveTiming()
            val intervals = dashboardService.getIntervals()
            val sessionContext = dashboardService.getCurrentSessionContext()

            latestSnapshot.set(mapOf(
                "positions" to positions,
                "timing" to timing,
                "intervals" to intervals,
                "isLive" to positions.isNotEmpty(),
                "circuitKey" to sessionContext["circuitKey"],
                "year" to sessionContext["year"],
                "meetingKey" to sessionContext["meetingKey"],
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            logger.warn("F1 live poll failed (keeping previous snapshot): ${e.message}")
        }
    }

    // TEMPORARY — generates fake cars moving in staggered circles around
    // a center point, so CircuitMap interpolation/colors/names can be
    // verified without waiting for a real race weekend. Remove this
    // method (and mockMode entirely) once real-race testing is possible.
    private fun generateMockSnapshot(): Map<String, Any?> {
        val t = System.currentTimeMillis() / 1000.0
        val positions = mockDrivers.mapIndexed { index, (number, name, team) ->
            // Each car offset by a fraction of the circle so they don't overlap,
            // and slightly different radius/speed so it doesn't look robotic
            val angleOffset = (index * (2 * Math.PI / mockDrivers.size))
            val speed = 0.25 + (index * 0.015) // slight speed variance per car
            val radius = 1800.0 - (index * 40)  // slight radius variance
            val angle = t * speed + angleOffset

            LiveDriverPosition(
                driverNumber = number,
                driverName = name,
                teamName = team,
                position = index + 1,
                x = radius * Math.cos(angle),
                y = radius * Math.sin(angle),
                z = 0.0,
                date = java.time.Instant.now().toString()
            )
        }

        val intervals = mockDrivers.mapIndexed { index, (number, _, _) ->
            LiveInterval(
                driverNumber = number,
                gapToLeader = if (index == 0) "" else String.format("%.1f", index * 1.8),
                interval = if (index == 0) "" else String.format("%.1f", 1.2 + (index * 0.3)),
                date = java.time.Instant.now().toString()
            )
        }

        return mapOf(
            "positions" to positions,
            "timing" to emptyList<LiveTiming>(),
            "intervals" to intervals,
            "isLive" to true,
            "circuitKey" to 14,  // Monza — has real circuit layout data via MultiViewer
            "year" to 2024,
            "meetingKey" to null,
            "timestamp" to System.currentTimeMillis()
        )
    }
    fun getLatestSnapshot(): Map<String, Any?> = latestSnapshot.get()
}
