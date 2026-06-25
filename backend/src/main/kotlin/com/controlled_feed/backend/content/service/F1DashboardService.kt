package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.*
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class F1DashboardService {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val ergastClient = WebClient.builder()
        .baseUrl("https://api.jolpi.ca/ergast/f1")
        .build()
    private val openF1Client = WebClient.builder()
        .baseUrl("https://api.openf1.org/v1")
        .build()
    private val multiViewerClient = WebClient.builder()
        .baseUrl("https://api.multiviewer.app/api/v1")
        .build()

    // ───────────── Static / season data (unchanged, working) ─────────────

    @Cacheable(value = ["f1-standings"], key = "'standings'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackDriverStandings")
    fun getDriverStandings(): List<DriverStanding> {
        logger.info("Fetching F1 driver standings")
        return try {
            val response = ergastClient.get()
                .uri("/current/driverStandings.json")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
            val podiums = calculateDriverPodiums()
            extractStandings(response, podiums)
        } catch (e: Exception) {
            logger.error("Error fetching standings: ${e.message}")
            emptyList()
        }
    }

    @Cacheable(value = ["f1-constructors"], key = "'constructors'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackConstructorStandings")
    fun getConstructorStandings(): List<ConstructorStanding> {
        logger.info("Fetching F1 constructor standings...")
        return try {
            val response = ergastClient.get()
                .uri("/current/constructorStandings.json")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
            val podiums = calculateConstructorPodiums()
            extractConstructorStandings(response, podiums)
        } catch (e: Exception) {
            logger.error("Error fetching constructor standings: ${e.message}")
            emptyList()
        }
    }

    @Cacheable(value = ["f1-results"], key = "'results'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackRaceResults")
    fun getLatestRaceResults(): List<RaceResult> {
        logger.info("Fetching latest F1 race results")
        return try {
            val response = ergastClient.get()
                .uri("/current/last/results.json")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
            extractRaceResults(response)
        } catch (e: Exception) {
            logger.error("Error fetching race results: ${e.message}")
            emptyList()
        }
    }

    @Cacheable(value = ["f1-schedule"], key = "'schedule'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackRaceSchedule")
    fun getRaceSchedule(): List<RaceSchedule> {
        logger.info("Fetching F1 race schedule")
        return try {
            val response = ergastClient.get()
                .uri("/current.json")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()
            extractRaceSchedule(response)
        } catch (e: Exception) {
            logger.error("Error fetching schedule: ${e.message}")
            emptyList()
        }
    }

    // ───────────── Live data (unchanged logic, caching now correct) ─────────────

    @Cacheable(value = ["f1-live-positions"], key = "'positions'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackLivePositions")
    fun getLiveDriverPositions(): List<LiveDriverPosition> {
        logger.info("Fetching Live driver positions")
        return try {
            val positions = openF1Client.get()
                .uri("/position?session_key=latest")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block() ?: emptyList()

            val drivers = openF1Client.get()
                .uri("/drivers?session_key=latest")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block() ?: emptyList()

            val driverMap = drivers.associate {
                (it["driver_number"] as? Int ?: 0) to mapOf(
                    "name" to (it["full_name"] as? String ?: ""),
                    "team" to (it["team_name"] as? String ?: "")
                )
            }

            val latestPositions = positions
                .groupBy { it["driver_number"] as? Int ?: 0 }
                .mapValues { it.value.last() }

            latestPositions.map { (driverNumber, pos) ->
                LiveDriverPosition(
                    driverNumber = driverNumber,
                    driverName = driverMap[driverNumber]?.get("name") ?: "",
                    teamName = driverMap[driverNumber]?.get("team") ?: "",
                    position = pos["position"] as? Int ?: 0,
                    x = (pos["x"] as? Number)?.toDouble() ?: 0.0,
                    y = (pos["y"] as? Number)?.toDouble() ?: 0.0,
                    z = (pos["z"] as? Number)?.toDouble() ?: 0.0,
                    date = pos["date"] as? String ?: ""
                )
            }.sortedBy { it.position }
        } catch (e: Exception) {
            logger.error("Error fetching live positions: ${e.message}")
            emptyList()
        }
    }

    @Cacheable(value = ["f1-live-timing"], key = "'timing'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackLiveTiming")
    fun getLiveTiming(): List<LiveTiming> {
        logger.info("Fetching Live timing")
        return try {
            val laps = openF1Client.get()
                .uri("/laps?session_key=latest")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block() ?: emptyList()

            val latestLaps = laps
                .groupBy { it["driver_number"] as? Int ?: 0 }
                .mapValues { it.value.last() }

            latestLaps.map { (driverNumber, lap) ->
                LiveTiming(
                    driverNumber = driverNumber,
                    lapNumber = (lap["lap_number"] as? Int) ?: 0,
                    lapDuration = (lap["lap_duration"] as? Number)?.toDouble() ?: 0.0,
                    sector1 = (lap["duration_sector_1"] as? Number)?.toDouble() ?: 0.0,
                    sector2 = (lap["duration_sector_2"] as? Number)?.toDouble() ?: 0.0,
                    sector3 = (lap["duration_sector_3"] as? Number)?.toDouble() ?: 0.0,
                    isPitOutLap = lap["is_pit_out_lap"] as? Boolean ?: false,
                    date = lap["date_start"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching live timing: ${e.message}")
            emptyList()
        }
    }

    @Cacheable(value = ["f1-live-intervals"], key = "'intervals'")
    @CircuitBreaker(name = "f1Service", fallbackMethod = "fallbackLiveIntervals")
    fun getIntervals(): List<LiveInterval> {
        logger.info("Fetching intervals")
        return try {
            val intervals = openF1Client.get()
                .uri("/intervals?session_key=latest")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block() ?: emptyList()
            val latestIntervals = intervals
                .groupBy { it["driver_number"] as? Int ?: 0 }
                .mapValues { it.value.last() }
            latestIntervals.map { (driverNumber, interval) ->
                LiveInterval(
                    driverNumber = driverNumber,
                    gapToLeader = interval["gap_to_leader"]?.toString() ?: "",
                    interval = interval["interval"]?.toString() ?: "",
                    date = interval["date"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching live intervals: ${e.message}")
            emptyList()
        }
    }

    // ───────────── Session/circuit/driver lookups — caching now matches real cadence ─────────────

    @Cacheable(value = ["f1-meetings"], key = "#year")
    fun getAllMeetingsForYear(year: Int): List<Map<String, Any?>> {
        logger.info("📅 Fetching all meetings for $year")
        return try {
            val response = openF1Client.get()
                .uri("/meetings?year=$year")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block() ?: emptyList()

            response.map {
                mapOf(
                    "circuitKey" to it["circuit_key"],
                    "circuitShortName" to it["circuit_short_name"],
                    "meetingName" to it["meeting_name"],
                    "countryName" to it["country_name"],
                    "dateStart" to it["date_start"]
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching meetings for $year: ${e.message}")
            emptyList()
        }
    }

    @Cacheable(value = ["circuit-layout"], key = "#circuitKey + '-' + #year")
    fun getCircuitLayout(circuitKey: Int, year: Int): CircuitLayout {
        logger.info("🗺️ Fetching circuit layout for circuit $circuitKey, year $year")
        val response = multiViewerClient.get()
            .uri("/circuits/$circuitKey/$year")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block() ?: throw RuntimeException("Circuit data not found")

        val rotation = (response["rotation"] as? Number)?.toDouble() ?: 0.0

        val trackX = response["x"] as? List<*>
        val trackY = response["y"] as? List<*>
        val trackPoints = trackX?.mapIndexed { i, x ->
            CircuitPoint(
                x = (x as Number).toDouble(),
                y = (trackY?.get(i) as Number).toDouble()
            )
        } ?: emptyList()

        val cornersData = response["corners"] as? List<*>
        val corners = cornersData?.map { corner ->
            val c = corner as Map<*, *>
            val tp = c["trackPosition"] as Map<*, *>
            CircuitPoint(
                x = (tp["x"] as Number).toDouble(),
                y = (tp["y"] as Number).toDouble(),
                number = (c["number"] as? Number)?.toInt()
            )
        } ?: emptyList()

        return CircuitLayout(
            circuitKey = circuitKey,
            circuitName = response["circuitName"]?.toString() ?: "",
            rotation = rotation,
            corners = corners,
            trackPoints = trackPoints
        )
    }

    @Cacheable(value = ["f1-current-drivers"], key = "'current-drivers'")
    fun getCurrentDrivers(): List<Map<String, Any?>> {
        logger.info("Fetching current driver grid")
        return try {
            val response = openF1Client.get()
                .uri("/drivers?session_key=latest")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block() ?: emptyList()

            response.map {
                mapOf(
                    "driverNumber" to it["driver_number"],
                    "fullName" to it["full_name"],
                    "nameAcronym" to it["name_acronym"],
                    "teamName" to it["team_name"],
                    "teamColour" to it["team_colour"],
                    "headshotUrl" to it["headshot_url"]
                )
            }
        } catch (e: Exception) {
            logger.error("Error fetching current drivers: ${e.message}")
            emptyList()
        }
    }

    // Sessions update once a day per OpenF1 docs — this cache (3 hours)
    // means we hit OpenF1's /sessions endpoint a handful of times a day
    // total, instead of every 4 seconds. This was the actual 429 cause.
    @Cacheable(value = ["f1-session-context"], key = "'session-context'")
    fun getCurrentSessionContext(): Map<String, Any?> {
        logger.info("🔍 Fetching session context")
        return try {
            val response = openF1Client.get()
                .uri("/sessions?session_key=latest")
                .retrieve()
                .bodyToFlux(Map::class.java)
                .collectList()
                .block()

            val session = response?.firstOrNull() ?: return mapOf(
                "circuitKey" to null, "year" to null, "meetingKey" to null
            )

            mapOf(
                "circuitKey" to session["circuit_key"],
                "year" to session["year"],
                "meetingKey" to session["meeting_key"],
                "circuitShortName" to session["circuit_short_name"],
                "sessionName" to session["session_name"]
            )
        } catch (e: Exception) {
            logger.error("Error fetching session context: ${e.message}")
            mapOf("circuitKey" to null, "year" to null, "meetingKey" to null)
        }
    }
    // Get full detail for one race (completed or upcoming)
    fun getRaceDetail(season: Int, round: Int): RaceDetail {
        // Fetch results + schedule in parallel
        val resultsResponse = ergastClient.get()
            .uri("/$season/$round/results.json")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        val scheduleResponse = ergastClient.get()
            .uri("/$season/$round.json")
            .retrieve()
            .bodyToMono(Map::class.java)
            .block()

        val race = extractRaceFromSchedule(scheduleResponse)
        val results = extractResultsFromResponse(resultsResponse)
        val fastestLap = extractFastestLap(resultsResponse)
        val sessions = extractSessions(scheduleResponse)
        val hasSprint = sessions.any { it.name == "Sprint" }

        // Only fetch pit stops for completed races (results exist)
        val fastestPitStop = if (results.isNotEmpty()) {
            fetchFastestPitStop(season, round)
        } else null

        return RaceDetail(
            raceName = race["raceName"]?.toString() ?: "",
            round = round.toString(),
            date = race["date"]?.toString() ?: "",
            circuit = (race["Circuit"] as? Map<*, *>)?.get("circuitName")?.toString() ?: "",
            country = ((race["Circuit"] as? Map<*, *>)
                ?.get("Location") as? Map<*, *>)
                ?.get("country")?.toString() ?: "",
            podium = results.take(3),
            fastestLap = fastestLap,
            fastestPitStop = fastestPitStop,
            sessions = sessions,
            hasSprint = hasSprint
        )
    }

    private fun extractRaceFromSchedule(response: Map<*, *>?): Map<*, *> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyMap<String, Any>()
        val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyMap<String, Any>()
        val races = raceTable["Races"] as? List<*> ?: return emptyMap<String, Any>()
        return (races.firstOrNull() as? Map<*, *>) ?: emptyMap<String, Any>()
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractResultsFromResponse(response: Map<*, *>?): List<PodiumEntry> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyList()
        val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyList()
        val races = raceTable["Races"] as? List<*> ?: return emptyList()
        val race = races.firstOrNull() as? Map<*, *> ?: return emptyList()
        val results = race["Results"] as? List<*> ?: return emptyList()

        return results.mapNotNull { result ->
            val r = result as? Map<*, *> ?: return@mapNotNull null
            val driver = r["Driver"] as? Map<*, *> ?: return@mapNotNull null
            val constructor = r["Constructor"] as? Map<*, *>
            val pos = r["position"]?.toString()?.toIntOrNull() ?: return@mapNotNull null
            val time = (r["Time"] as? Map<*, *>)?.get("time")?.toString()
                ?: r["status"]?.toString() ?: ""
            PodiumEntry(
                position = pos,
                driverName = "${driver["givenName"]} ${driver["familyName"]}",
                team = constructor?.get("name")?.toString() ?: "",
                time = time,
                points = r["points"]?.toString() ?: "0"
            )
        }
    }

    private fun extractFastestLap(response: Map<*, *>?): FastestLapEntry? {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return null
        val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return null
        val races = raceTable["Races"] as? List<*> ?: return null
        val race = races.firstOrNull() as? Map<*, *> ?: return null
        val results = race["Results"] as? List<*> ?: return null

        // Find result with FastestLap rank = 1
        val fastestResult = results.mapNotNull { it as? Map<*, *> }
            .firstOrNull { r ->
                val fl = r["FastestLap"] as? Map<*, *>
                fl?.get("rank")?.toString() == "1"
            } ?: return null

        val driver = fastestResult["Driver"] as? Map<*, *> ?: return null
        val constructor = fastestResult["Constructor"] as? Map<*, *>
        val fastestLap = fastestResult["FastestLap"] as? Map<*, *> ?: return null
        val lapTime = (fastestLap["Time"] as? Map<*, *>)?.get("time")?.toString() ?: ""

        return FastestLapEntry(
            driverName = "${driver["givenName"]} ${driver["familyName"]}",
            team = constructor?.get("name")?.toString() ?: "",
            lapTime = lapTime,
            lapNumber = fastestLap["lap"]?.toString() ?: ""
        )
    }

    private fun fetchFastestPitStop(season: Int, round: Int): PitStopEntry? {
        return try {
            val response = ergastClient.get()
                .uri("/$season/$round/pitstops.json?limit=100")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val mrData = response?.get("MRData") as? Map<*, *> ?: return null
            val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return null
            val races = raceTable["Races"] as? List<*> ?: return null
            val race = races.firstOrNull() as? Map<*, *> ?: return null
            val pitStops = race["PitStops"] as? List<*> ?: return null

            // Find shortest duration pit stop
            val fastest = pitStops
                .mapNotNull { it as? Map<*, *> }
                .minByOrNull { ps ->
                    ps["duration"]?.toString()?.toDoubleOrNull() ?: Double.MAX_VALUE
                } ?: return null

            PitStopEntry(
                driverName = fastest["driverId"]?.toString() ?: "",
                lap = fastest["lap"]?.toString() ?: "",
                duration = fastest["duration"]?.toString() ?: "",
                stop = fastest["stop"]?.toString() ?: ""
            )
        } catch (e: Exception) {
            logger.warn("Could not fetch pit stops: ${e.message}")
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractSessions(response: Map<*, *>?): List<SessionTime> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyList()
        val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyList()
        val races = raceTable["Races"] as? List<*> ?: return emptyList()
        val race = races.firstOrNull() as? Map<*, *> ?: return emptyList()

        val sessions = mutableListOf<SessionTime>()

        // Map Ergast field names to display names
        val sessionFields = listOf(
            "FirstPractice"  to "FP1",
            "SecondPractice" to "FP2",
            "ThirdPractice"  to "FP3",
            "SprintQualifying" to "Sprint Qualifying",
            "Sprint"         to "Sprint",
            "Qualifying"     to "Qualifying",
        )

        sessionFields.forEach { (field, name) ->
            val session = race[field] as? Map<*, *>
            if (session != null) {
                sessions.add(SessionTime(
                    name = name,
                    date = session["date"]?.toString() ?: "",
                    time = session["time"]?.toString() ?: ""
                ))
            }
        }

        // Race itself
        sessions.add(SessionTime(
            name = "Race",
            date = race["date"]?.toString() ?: "",
            time = race["time"]?.toString() ?: ""
        ))

        return sessions.filter { it.date.isNotEmpty() }
    }

    // ───────────── Circuit breaker fallbacks (unchanged) ─────────────

    fun fallbackDriverStandings(e: Exception): List<DriverStanding> {
        logger.error("Circuit OPEN for F1 standings! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackConstructorStandings(e: Exception): List<ConstructorStanding> {
        logger.error("Circuit OPEN for constructor standings! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackRaceResults(e: Exception): List<RaceResult> {
        logger.error("Circuit OPEN for race results! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackRaceSchedule(e: Exception): List<RaceSchedule> {
        logger.error("Circuit OPEN for race schedule! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackLivePositions(e: Exception): List<LiveDriverPosition> {
        logger.error("Circuit OPEN for live positions! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackLiveTiming(e: Exception): List<LiveTiming> {
        logger.error("Circuit OPEN for live timing! Error: ${e.message}")
        return emptyList()
    }

    fun fallbackLiveIntervals(e: Exception): List<LiveInterval> {
        logger.error("Circuit OPEN for live intervals! Error: ${e.message}")
        return emptyList()
    }

    // ───────────── Private helpers (unchanged, working) ─────────────

    private fun calculateDriverPodiums(): Map<String, Int> {
        return try {
            val response = ergastClient.get()
                .uri("/current/results.json?limit=1000")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyMap()
            val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyMap()
            val races = raceTable["Races"] as? List<*> ?: return emptyMap()

            val podiumCount = mutableMapOf<String, Int>()
            races.forEach { race ->
                val results = (race as? Map<*, *>)?.get("Results") as? List<*> ?: return@forEach
                results.forEach { result ->
                    val r = result as? Map<*, *> ?: return@forEach
                    val position = r["position"]?.toString()?.toIntOrNull() ?: return@forEach
                    if (position <= 3) {
                        val driver = r["Driver"] as? Map<*, *> ?: return@forEach
                        val name = "${driver["givenName"]} ${driver["familyName"]}"
                        podiumCount[name] = (podiumCount[name] ?: 0) + 1
                    }
                }
            }
            podiumCount
        } catch (e: Exception) {
            logger.error("Error calculating podiums: ${e.message}")
            emptyMap()
        }
    }

    private fun calculateConstructorPodiums(): Map<String, Int> {
        return try {
            val response = ergastClient.get()
                .uri("/current/results.json?limit=1000")
                .retrieve()
                .bodyToMono(Map::class.java)
                .block()

            val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyMap()
            val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyMap()
            val races = raceTable["Races"] as? List<*> ?: return emptyMap()

            val podiumCount = mutableMapOf<String, Int>()
            races.forEach { race ->
                val results = (race as? Map<*, *>)?.get("Results") as? List<*> ?: return@forEach
                results.forEach { result ->
                    val r = result as? Map<*, *> ?: return@forEach
                    val position = r["position"]?.toString()?.toIntOrNull() ?: return@forEach
                    if (position <= 3) {
                        val constructor = r["Constructor"] as? Map<*, *> ?: return@forEach
                        val name = constructor["name"]?.toString() ?: return@forEach
                        podiumCount[name] = (podiumCount[name] ?: 0) + 1
                    }
                }
            }
            podiumCount
        } catch (e: Exception) {
            logger.error("Error calculating constructor podiums: ${e.message}")
            emptyMap()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractStandings(response: Map<*, *>?, podiums: Map<String, Int> = emptyMap()): List<DriverStanding> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyList()
        val standingsTable = mrData["StandingsTable"] as? Map<*, *> ?: return emptyList()
        val standingList = standingsTable["StandingsLists"] as? List<*> ?: return emptyList()
        val standings =
            (standingList.firstOrNull() as? Map<*, *>)?.get("DriverStandings") as? List<*> ?: return emptyList()
        return standings.mapNotNull { standing ->
            val s = standing as? Map<*, *> ?: return@mapNotNull null
            val driver = s["Driver"] as? Map<*, *> ?: return@mapNotNull null
            val constructor = (s["Constructors"] as? List<*>)?.firstOrNull() as? Map<*, *>
            val name = "${driver["givenName"]} ${driver["familyName"]}"
            DriverStanding(
                position = s["position"]?.toString() ?: "",
                driverName = name,
                team = constructor?.get("name")?.toString() ?: "",
                points = s["points"]?.toString() ?: "",
                wins = s["wins"]?.toString() ?: "",
                nationality = driver["nationality"]?.toString() ?: "",
                podiums = podiums[name] ?: 0
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractConstructorStandings(
        response: Map<*, *>?,
        podiums: Map<String, Int> = emptyMap()
    ): List<ConstructorStanding> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyList()
        val standingsTable = mrData["StandingsTable"] as? Map<*, *> ?: return emptyList()
        val standingsLists = standingsTable["StandingsLists"] as? List<*> ?: return emptyList()
        val standings =
            (standingsLists.firstOrNull() as? Map<*, *>)?.get("ConstructorStandings") as? List<*> ?: return emptyList()

        return standings.mapNotNull { standing ->
            val s = standing as? Map<*, *> ?: return@mapNotNull null
            val constructor = s["Constructor"] as? Map<*, *> ?: return@mapNotNull null
            val name = constructor["name"]?.toString() ?: ""
            ConstructorStanding(
                position = s["position"]?.toString() ?: "",
                teamName = name,
                nationality = constructor["nationality"]?.toString() ?: "",
                points = s["points"]?.toString() ?: "",
                wins = s["wins"]?.toString() ?: "",
                podiums = podiums[name] ?: 0
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractRaceResults(response: Map<*, *>?): List<RaceResult> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyList()
        val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyList()
        val races = raceTable["Races"] as? List<*> ?: return emptyList()
        val results = (races.firstOrNull() as? Map<*, *>)?.get("Results") as? List<*> ?: return emptyList()

        return results.mapNotNull { result ->
            val r = result as? Map<*, *> ?: return@mapNotNull null
            val driver = r["Driver"] as? Map<*, *> ?: return@mapNotNull null
            val constructor = r["Constructor"] as? Map<*, *>
            val fastestLap = (r["FastestLap"] as? Map<*, *>)?.get("Time") as? Map<*, *>
            val time = (r["Time"] as? Map<*, *>)?.get("time")?.toString() ?: r["status"]?.toString() ?: ""
            RaceResult(
                position = r["Position"]?.toString() ?: "",
                driverName = "${driver["givenName"]} ${driver["familyName"]}",
                team = constructor?.get("name") as? String ?: "",
                time = time,
                fastestLap = fastestLap?.get("time")?.toString() ?: "",
                points = r["points"]?.toString() ?: ""
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractRaceSchedule(response: Map<*, *>?): List<RaceSchedule> {
        val mrData = response?.get("MRData") as? Map<*, *> ?: return emptyList()
        val raceTable = mrData["RaceTable"] as? Map<*, *> ?: return emptyList()
        val races = raceTable["Races"] as? List<*> ?: return emptyList()

        return races.mapNotNull { race ->
            val r = race as? Map<*, *> ?: return@mapNotNull null
            val circuit = r["Circuit"] as? Map<*, *>
            val location = circuit?.get("Location") as? Map<*, *>
            RaceSchedule(
                raceName = r["raceName"]?.toString() ?: "",
                circuit = circuit?.get("circuitName")?.toString() ?: "",
                country = location?.get("country")?.toString() ?: "",
                date = r["date"]?.toString() ?: "",
                time = r["time"]?.toString() ?: ""
            )
        }
    }
}