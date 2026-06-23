package com.controlled_feed.backend.content.config

import com.controlled_feed.backend.content.model.VideoCategory

data class ChannelConfig(
    val channelId: String,
    val channelName: String,
    val category: VideoCategory  // MIXED = categorize by keyword
)

data class SportConfig(
    val category: VideoCategory,
    val displayName: String,
    val icon: String,
    val channels: List<ChannelConfig>,
    val rssFeeds: List<Pair<String, String>>, // url to source name
    val keywords: List<String>  // used when category=MIXED to detect sport
)

object SportRegistry {

    val ALL: List<SportConfig> = listOf(

        SportConfig(
            category = VideoCategory.F1,
            displayName = "Formula 1",
            icon = "🏎️",
            channels = listOf(
                ChannelConfig("UCB_qr75-ydFVKSF9Dmo6izg", "Formula 1",       VideoCategory.F1),
                ChannelConfig("UC3kxJQ9RfaS5CKeYbbFMi4Q", "Sky Sports F1",    VideoCategory.F1),
                ChannelConfig("UCDxm-FbK9nmZKqHI19j-DOw", "WTF1",             VideoCategory.F1),
                ChannelConfig("UCxuksozHJD_f1w9nVa6UhAw", "Autosport",        VideoCategory.F1),
                ChannelConfig("UCd8iY-kEHtaB8qt8MH--zGw", "Driver61",         VideoCategory.F1),
            ),
            rssFeeds = listOf(
                "https://www.autosport.com/rss/f1/news/"         to "Autosport",
                "https://www.motorsport.com/rss/f1/news/"        to "Motorsport",
                "https://www.bbc.co.uk/sport/formula1/rss.xml"  to "BBC Sport F1",
            ),
            keywords = listOf("formula 1", "f1", "grand prix", "gp", "ferrari",
                "mclaren", "red bull", "mercedes", "verstappen",
                "hamilton", "norris", "leclerc")
        ),

        SportConfig(
            category = VideoCategory.CRICKET,
            displayName = "Cricket",
            icon = "🏏",
            channels = listOf(
                ChannelConfig("UCt2JXOLNxqry7B_4rRZME3Q", "ICC",              VideoCategory.CRICKET),
                ChannelConfig("UCz1D0n02BR3t51KuBOPmfTQ", "BCCI",             VideoCategory.CRICKET),
                ChannelConfig("UCujuVKmt_utAQZJghxlRMIQ", "ESPNCricinfo",     VideoCategory.CRICKET),
                ChannelConfig("UCkBY0aHJP9BwjZLDYxAQrKg", "Cricket Australia", VideoCategory.CRICKET),
                ChannelConfig("UCkd4takjjF1EGD1TKIK2QiA", "Sky Sports Cricket",VideoCategory.CRICKET),
            ),
            rssFeeds = listOf(
                "https://www.espncricinfo.com/rss/content/story/feeds/0.xml" to "ESPNCricinfo",
                "https://www.bbc.co.uk/sport/cricket/rss.xml"               to "BBC Sport Cricket"
            ),
            keywords = listOf("cricket", "test match", "odi", "t20", "ipl",
                "bcci", "icc", "ashes", "virat", "rohit",
                "babar", "stokes")
        ),

        SportConfig(
            category = VideoCategory.FOOTBALL,
            displayName = "Football",
            icon = "⚽",
            channels = listOf(

                ChannelConfig("UCpcTrCXblq78GZrTUTLWeBw", "Fifa",   VideoCategory.FOOTBALL),
                ChannelConfig("UCG5qGWdu8nIRZqJ_GgDwQ-w", "Premier League",   VideoCategory.FOOTBALL),
                ChannelConfig("UCyGa1YEx9ST66rYrJTGIKOw", "UEFA",             VideoCategory.FOOTBALL),
                ChannelConfig("UC6c1z7bA__85CIWZ_jpCK-Q", "ESPN FC",          VideoCategory.FOOTBALL),
            ),
            rssFeeds = listOf(
                "https://www.bbc.co.uk/sport/football/rss.xml"          to "BBC Sport Football",
                "https://www.theguardian.com/football/rss"              to "The Guardian Football",
            ),
            keywords = listOf("football", "soccer", "premier league", "la liga",
                "bundesliga", "serie a", "champions league", "fifa",
                "goal", "striker", "messi", "ronaldo", "mbappe")
        ),

        SportConfig(
            category = VideoCategory.TENNIS,
            displayName = "Tennis",
            icon = "🎾",
            channels = listOf(
                ChannelConfig("UCbcxFkd6B9xUU54InHv4Tig", "Tennis TV",         VideoCategory.TENNIS),
                ChannelConfig("UCY_5h5zaSwN7Or4kIJDYNXA", "ATP Tour",         VideoCategory.TENNIS),
                ChannelConfig("UCaBIVVpHjq6j3tSyxwTE-8Q", "WTA",              VideoCategory.TENNIS),
                ChannelConfig("UCeTKJSW1NTAkf27nNmjWt5A", "Wimbledon",        VideoCategory.TENNIS),
            ),
            rssFeeds = listOf(
                "https://www.bbc.co.uk/sport/tennis/rss.xml"           to "BBC Sport Tennis",
            ),
            keywords = listOf("tennis", "wimbledon", "us open", "french open",
                "australian open", "atp", "wta", "djokovic",
                "alcaraz", "sinner", "swiatek", "grand slam")
        ),

        SportConfig(
            category = VideoCategory.BADMINTON,
            displayName = "Badminton",
            icon = "🏸",
            channels = listOf(
                ChannelConfig("UChh-akEbUM8_6ghGVnJd6cQ", "BWF Badminton",    VideoCategory.BADMINTON),
            ),
            rssFeeds = listOf(
            ),
            keywords = listOf("badminton", "bwf", "shuttlecock", "smash",
                "singles", "doubles", "indonesia open",
                "all england", "chen long", "sindhu", "axelsen")
        ),
    )

    // Lookup by category — O(1) access
    val byCategory: Map<VideoCategory, SportConfig> =
        ALL.associateBy { it.category }

    // All channels across all sports (flattened)
    val allChannels: List<ChannelConfig> =
        ALL.flatMap { it.channels }

    // All RSS feeds across all sports (flattened)
    val allRssFeeds: List<Triple<String, String, VideoCategory>> =
        ALL.flatMap { sport ->
            sport.rssFeeds.map { (url, source) ->
                Triple(url, source, sport.category)
            }
        }

    // Keyword map for MIXED channel categorization
    private val keywordMap: List<Pair<VideoCategory, List<String>>> =
        ALL.map { it.category to it.keywords }

    fun detectCategory(title: String, description: String): VideoCategory {
        val text = (title + " " + description).lowercase()
        for ((category, keywords) in keywordMap) {
            if (keywords.any { text.contains(it) }) return category
        }
        return VideoCategory.MIXED
    }

    // Adding a new sport in the future = just add a SportConfig entry above.
    // Nothing else needs to change.
}