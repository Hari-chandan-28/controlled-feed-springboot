package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.ConstructorStanding
import com.controlled_feed.backend.content.model.DriverStanding
import com.controlled_feed.backend.content.model.LiveDriverPosition
import com.controlled_feed.backend.content.model.LiveInterval
import com.controlled_feed.backend.content.model.LiveTiming
import com.controlled_feed.backend.content.model.RaceResult
import com.controlled_feed.backend.content.model.RaceSchedule
import com.controlled_feed.backend.content.service.F1DashboardService
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors
import org.springframework.http.MediaType
import java.sql.Driver
import java.util.concurrent.ExecutorService


@RestController
@RequestMapping("/api/f1")
class F1DashboardController(private val dashboardService: F1DashboardService) {
    private val executor: ExecutorService = Executors.newCachedThreadPool()
    @GetMapping("/standings")
    fun getDriverStandings(): ResponseEntity<List<DriverStanding>> {
        return ResponseEntity.ok(dashboardService.getDriverStandings())
    }
    @GetMapping("/constructors")
    fun getConstructorStandings(): ResponseEntity<List<ConstructorStanding>> {
        return ResponseEntity.ok(dashboardService.getConstructorStandings())
    }
    @GetMapping("/results")
    fun getLatestRaceResults(): ResponseEntity<List<RaceResult>> {
        return ResponseEntity.ok(dashboardService.getLatestRaceResults())
    }
    @GetMapping("/schedule")
    fun getRaceSchedule(): ResponseEntity<List<RaceSchedule>> {
        return ResponseEntity.ok(dashboardService.getRaceSchedule())
    }
    @GetMapping("/live/positions")
    fun getLiveDriverPositions(): ResponseEntity<List<LiveDriverPosition>> {
        return ResponseEntity.ok(dashboardService.getLiveDriverPositions())
    }

    @GetMapping("/live/timing")
    fun getLiveTiming(): ResponseEntity<List<LiveTiming>> {
        return ResponseEntity.ok(dashboardService.getLiveTiming())
    }

    @GetMapping("/live/intervals")
    fun getLiveIntervals(): ResponseEntity<List<LiveInterval>> {
        return ResponseEntity.ok(dashboardService.getIntervals())
    }
    @GetMapping("/live/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): SseEmitter {
        val emitter = SseEmitter(Long.MAX_VALUE)

        executor.execute {
            try {
                while (true) {
                    val data = mapOf(
                        "positions" to dashboardService.getLiveDriverPositions(),
                        "timing" to dashboardService.getLiveTiming(),
                        "intervals" to dashboardService.getIntervals()
                    )

                    emitter.send(data)
                    Thread.sleep(20000)
                }
            } catch (e: Exception) {
                emitter.completeWithError(e)
            }
        }

        return emitter
    }

}