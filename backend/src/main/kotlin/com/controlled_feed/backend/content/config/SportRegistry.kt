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
                ChannelConfig("UCuhiJuCMEIBMHwdQWaXeGIg", "Sky Sports F1",    VideoCategory.F1),
                ChannelConfig("UCqB5cV4WGHwOcz4bpyXB0fQ", "WTF1",             VideoCategory.F1),
                ChannelConfig("UCzgB2HNMCBmMaUy0Y5rFMnQ", "Autosport",        VideoCategory.F1),
                ChannelConfig("UCGn-O-KWCQF8vMSxkKABGlQ", "Driver61",         VideoCategory.F1),
            ),
            rssFeeds = listOf(
                "https://www.autosport.com/rss/f1/news/"         to "Autosport",
                "https://www.motorsport.com/rss/f1/news/"        to "Motorsport",
                "https://www.bbc.co.uk/sport/formula1/rss.xml"  to "BBC Sport F1",
                "https://feeds.skysports.com/skysports/f1"       to "Sky Sports F1",
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
                ChannelConfig("UCiWrjBhlICf_L_RK5y6Xo_g", "ICC",              VideoCategory.CRICKET),
                ChannelConfig("UCXCuNMEOmOdnkB9YIDRN_5A", "BCCI",             VideoCategory.CRICKET),
                ChannelConfig("UCmFMvD7oqb3HJCzFUK7oBXQ", "ESPNCricinfo",     VideoCategory.CRICKET),
                ChannelConfig("UCnzNETBjxKSqFRwsGvgBqhQ", "Cricket Australia", VideoCategory.CRICKET),
                ChannelConfig("UCJqFMPfE0JzZiT4WBKL8MSQ", "Sky Sports Cricket",VideoCategory.CRICKET),
            ),
            rssFeeds = listOf(
                "https://www.espncricinfo.com/rss/content/story/feeds/0.xml" to "ESPNCricinfo",
                "https://www.cricbuzz.com/rss-feeds/cricket-news"            to "Cricbuzz",
                "https://www.bbc.co.uk/sport/cricket/rss.xml"               to "BBC Sport Cricket",
                "https://feeds.skysports.com/skysports/cricket"             to "Sky Sports Cricket",
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
                ChannelConfig("UCL-8fBJHSfOdpFMQiBR-3pg", "Premier League",   VideoCategory.FOOTBALL),
                ChannelConfig("UCTOJBsplCZCVlXMWHb7TCCA", "UEFA",             VideoCategory.FOOTBALL),
                ChannelConfig("UCHbgNFFHrTzqXnGcqQCB0CA", "Sky Sports Football",VideoCategory.FOOTBALL),
                ChannelConfig("UCRm_0Z16bBHT_1R-Rc_VVOQ", "ESPN FC",          VideoCategory.FOOTBALL),
            ),
            rssFeeds = listOf(
                "https://www.bbc.co.uk/sport/football/rss.xml"          to "BBC Sport Football",
                "https://feeds.skysports.com/skysports/football"        to "Sky Sports Football",
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
                ChannelConfig("UCG2qQBOKMFlqotFwsgBi0Vw", "ATP Tour",         VideoCategory.TENNIS),
                ChannelConfig("UCylmzQxbgczRzTqY4QSRK7A", "WTA",              VideoCategory.TENNIS),
                ChannelConfig("UCaZez3LKBV6IKW7FhfVDMEA", "Wimbledon",        VideoCategory.TENNIS),
            ),
            rssFeeds = listOf(
                "https://www.bbc.co.uk/sport/tennis/rss.xml"           to "BBC Sport Tennis",
                "https://feeds.skysports.com/skysports/tennis"         to "Sky Sports Tennis",
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
                ChannelConfig("UCOWPJtAyRGKaS9dHJqp8BdQ", "BWF Badminton",    VideoCategory.BADMINTON),
                ChannelConfig("UCJdLIcHMRNfxN2hMXMQg0zA", "Badminton World",  VideoCategory.BADMINTON),
            ),
            rssFeeds = listOf(
                "https://bwfbadminton.com/feed/"                        to "BWF Badminton",
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