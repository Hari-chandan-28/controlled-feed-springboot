package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.*
import com.controlled_feed.backend.content.service.F1DashboardService
import com.controlled_feed.backend.content.service.F1LivePoller
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RestController
@RequestMapping("/api/f1")
class F1DashboardController(
    private val dashboardService: F1DashboardService,
    private val livePoller: F1LivePoller
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val sseExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(4)

    @GetMapping("/standings")
    fun getDriverStandings(): ResponseEntity<List<DriverStanding>> =
        ResponseEntity.ok(dashboardService.getDriverStandings())

    @GetMapping("/constructors")
    fun getConstructorStandings(): ResponseEntity<List<ConstructorStanding>> =
        ResponseEntity.ok(dashboardService.getConstructorStandings())

    @GetMapping("/results")
    fun getLatestRaceResults(): ResponseEntity<List<RaceResult>> =
        ResponseEntity.ok(dashboardService.getLatestRaceResults())

    @GetMapping("/schedule")
    fun getRaceSchedule(): ResponseEntity<List<RaceSchedule>> =
        ResponseEntity.ok(dashboardService.getRaceSchedule())

    @GetMapping("/circuits/{year}")
    fun getAllCircuits(@PathVariable year: Int): ResponseEntity<List<Map<String, Any?>>> =
        ResponseEntity.ok(dashboardService.getAllMeetingsForYear(year))

    @GetMapping("/circuit/{circuitKey}/{year}")
    fun getCircuitLayout(
        @PathVariable circuitKey: Int,
        @PathVariable year: Int
    ): ResponseEntity<CircuitLayout> =
        ResponseEntity.ok(dashboardService.getCircuitLayout(circuitKey, year))

    @GetMapping("/drivers/current")
    fun getCurrentDrivers(): ResponseEntity<List<Map<String, Any?>>> =
        ResponseEntity.ok(dashboardService.getCurrentDrivers())

    @GetMapping("/live/session-context")
    fun getSessionContext(): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.ok(dashboardService.getCurrentSessionContext())

    @GetMapping("/live/positions")
    fun getLiveDriverPositions(): ResponseEntity<List<LiveDriverPosition>> =
        ResponseEntity.ok(dashboardService.getLiveDriverPositions())

    @GetMapping("/live/timing")
    fun getLiveTiming(): ResponseEntity<List<LiveTiming>> =
        ResponseEntity.ok(dashboardService.getLiveTiming())

    @GetMapping("/live/intervals")
    fun getLiveIntervals(): ResponseEntity<List<LiveInterval>> =
        ResponseEntity.ok(dashboardService.getIntervals())

    private val activeStreams = java.util.concurrent.atomic.AtomicInteger(0)

    // ... your other @GetMapping endpoints stay exactly as they are ...

    @GetMapping("/live/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): SseEmitter {
        val emitter = SseEmitter(0L)
        val running = AtomicBoolean(true)
        val alreadyCleanedUp = AtomicBoolean(false)

        val cleanup = {
            if (alreadyCleanedUp.compareAndSet(false, true)) {
                running.set(false)
            }
        }

        emitter.onCompletion(cleanup)
        emitter.onTimeout(cleanup)
        emitter.onError { cleanup() }

        val future = sseExecutor.scheduleWithFixedDelay({
            if (!running.get()) return@scheduleWithFixedDelay
            try {
                emitter.send(SseEmitter.event().data(livePoller.getLatestSnapshot()))
            } catch (e: Exception) {
                // ANY send failure means this connection is dead — doesn't
                // matter if it's IOException, IllegalStateException, or
                // anything else. Stop trying immediately, every time.
                logger.warn("SSE send failed, stopping this stream: ${e.message}")
                cleanup()
            }
        }, 0, 2, TimeUnit.SECONDS)

        emitter.onCompletion { future.cancel(true) }
        emitter.onTimeout { future.cancel(true) }

        return emitter
    }
    //schedule
    @GetMapping("/race/{season}/{round}")
    fun getRaceDetail(
        @PathVariable season: Int,
        @PathVariable round: Int
    ): ResponseEntity<RaceDetail> {
        return ResponseEntity.ok(dashboardService.getRaceDetail(season, round))
    }
}