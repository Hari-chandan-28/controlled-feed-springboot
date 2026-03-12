package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.ConstructorStanding
import com.controlled_feed.backend.content.model.DriverStanding
import com.controlled_feed.backend.content.model.LiveDriverPosition
import com.controlled_feed.backend.content.model.LiveInterval
import com.controlled_feed.backend.content.model.LiveTiming
import com.controlled_feed.backend.content.model.RaceResult
import com.controlled_feed.backend.content.model.RaceSchedule
import org.slf4j.LoggerFactory
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
            logger.error(" Error calculating constructor podiums: ${e.message}")
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