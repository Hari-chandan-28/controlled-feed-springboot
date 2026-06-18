package com.controlled_feed.backend.content.controller

import com.controlled_feed.backend.content.model.*
import com.controlled_feed.backend.content.service.F1DashboardService
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
    private val dashboardService: F1DashboardService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // Shared pool for all SSE streams — sized for a handful of concurrent
    // viewers; each stream uses one scheduled task, not one blocked thread
    private val sseExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(4)

    // ── Static / season data (Ergast/Jolpi — already working, unchanged) ──

    @GetMapping("/standings")
    fun getDriverStandings(): ResponseEntity<List<DriverStanding>> =
        ResponseEntity.ok(dashboardService.getDriverStandings())

    @GetMapping("/constructors")
    fun getConstructorStandings(): ResponseEntity<List<ConstructorStanding>> =
        ResponseEntity.ok(dashboardService.getConstructorStandings())

//    @GetMapping("/results/{round}")
//    fun getRaceResults(@PathVariable round: Int): ResponseEntity<List<RaceResult>> =
//        ResponseEntity.ok(dashboardService.getRaceResults(round))
    @GetMapping("/results")
    fun getLatestRaceResults(): ResponseEntity<List<RaceResult>> {
        return ResponseEntity.ok(dashboardService.getLatestRaceResults())
    }
    @GetMapping("/schedule")
    fun getSchedule(): ResponseEntity<List<RaceSchedule>> =
        ResponseEntity.ok(dashboardService.getRaceSchedule())

    // ── Dynamic circuit + driver lookups (no hardcoding) ──

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

    // Tells frontend which circuit/year is live right now — drives the
    // "only refetch circuit layout when this changes" logic on the client
    @GetMapping("/live/session-context")
    fun getSessionContext(): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.ok(dashboardService.getCurrentSessionContext())

    // ── Live data — one-off REST fetch (used outside the live page,
    //     e.g. if some other widget just needs a snapshot) ──

    @GetMapping("/live/positions")
    fun getLiveDriverPositions(): ResponseEntity<List<LiveDriverPosition>> =
        ResponseEntity.ok(dashboardService.getLiveDriverPositions())

    @GetMapping("/live/timing")
    fun getLiveTiming(): ResponseEntity<List<LiveTiming>> =
        ResponseEntity.ok(dashboardService.getLiveTiming())

    @GetMapping("/live/intervals")
    fun getLiveIntervals(): ResponseEntity<List<LiveInterval>> =
        ResponseEntity.ok(dashboardService.getIntervals())

    // ── Live data — SSE stream, this is what F1Live.jsx actually uses ──

    @GetMapping("/live/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(): SseEmitter {
        val emitter = SseEmitter(0L) // no forced timeout; client/server control lifecycle
        val running = AtomicBoolean(true)

        emitter.onCompletion { running.set(false) }
        emitter.onTimeout { running.set(false) }
        emitter.onError { running.set(false) }

        val future = sseExecutor.scheduleWithFixedDelay({
            if (!running.get()) return@scheduleWithFixedDelay
            try {
                val positions = dashboardService.getLiveDriverPositions()
                val timing = dashboardService.getLiveTiming()
                val intervals = dashboardService.getIntervals()
                val sessionContext = dashboardService.getCurrentSessionContext()

                val data = mapOf(
                    "positions" to positions,
                    "timing" to timing,
                    "intervals" to intervals,
                    "isLive" to positions.isNotEmpty(),
                    "circuitKey" to sessionContext["circuitKey"],
                    "year" to sessionContext["year"],
                    "meetingKey" to sessionContext["meetingKey"],
                    "timestamp" to System.currentTimeMillis()
                )
                emitter.send(SseEmitter.event().data(data))
            } catch (e: Exception) {
                logger.warn("SSE send failed, closing stream: ${e.message}")
                running.set(false)
                emitter.completeWithError(e)
            }
        }, 0, 4, TimeUnit.SECONDS)

        emitter.onCompletion { future.cancel(true) }
        emitter.onTimeout { future.cancel(true) }

        return emitter
    }
}