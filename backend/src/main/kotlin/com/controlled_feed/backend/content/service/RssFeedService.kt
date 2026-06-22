package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.auth.repository.UserRepository
import com.controlled_feed.backend.common.ResourceNotFoundException
import com.controlled_feed.backend.content.config.SportRegistry
import com.controlled_feed.backend.content.model.Article
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.repository.ArticleRepository
import com.controlled_feed.backend.profile.repository.ProfileRepository
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.net.URL

@Service
class RssFeedService(
    private val articleRepository: ArticleRepository,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // ── Fetch all sports' RSS feeds ───────────────────────────
    fun fetchAndStoreAllArticles(): List<Article> {
        logger.info("📰 Fetching RSS articles for all ${SportRegistry.allRssFeeds.size} feeds...")
        return SportRegistry.allRssFeeds.flatMap { (url, source, category) ->
            try {
                val saved = fetchAndStore(url, category, source)
                // Keep only 20 most recent per source
                pruneOldArticles(source)
                saved
            } catch (e: Exception) {
                logger.error("❌ Failed to fetch $source: ${e.message}")
                emptyList()
            }
        }
    }

    // ── Fetch one sport's feeds ───────────────────────────────
    fun fetchAndStoreSport(category: VideoCategory): List<Article> {
        val sport = SportRegistry.byCategory[category] ?: return emptyList()
        logger.info("📰 Fetching ${sport.displayName} articles from ${sport.rssFeeds.size} feeds...")
        return sport.rssFeeds.flatMap { (url, source) ->
            try {
                val saved = fetchAndStore(url, category, source)
                pruneOldArticles(source)
                saved
            } catch (e: Exception) {
                logger.error("❌ Failed RSS $source: ${e.message}")
                emptyList()
            }
        }
    }

    // ── Prune: keep 20 newest per source ──────────────────────
    private fun pruneOldArticles(source: String) {
        val allForSource = articleRepository.findBySourceOrderByPublishedAtDesc(source)
        if (allForSource.size > 20) {
            val toDelete = allForSource.drop(20)
            articleRepository.deleteAll(toDelete)
            logger.info("🗑️ Pruned ${toDelete.size} old articles from $source")
        }
    }

    // ── Core fetch + save ─────────────────────────────────────
    private fun fetchAndStore(
        url: String,
        category: VideoCategory,
        source: String
    ): List<Article> {
        val savedArticles = mutableListOf<Article>()
        try {
            logger.info("📡 Fetching from $source...")
            val feed = SyndFeedInput().build(XmlReader(URL(url)))
            feed.entries.forEach { entry ->
                val guid = entry.uri ?: entry.link ?: return@forEach
                if (articleRepository.existsByGuid(guid)) return@forEach

                val imgUrl = entry.enclosures?.firstOrNull()?.url
                    ?: entry.foreignMarkup?.firstOrNull()?.text ?: ""

                val article = Article(
                    guid = guid,
                    title = entry.title ?: "",
                    description = entry.description?.value ?: "",
                    link = entry.link ?: "",
                    imageUrl = imgUrl,
                    publishedAt = entry.publishedDate?.toString() ?: "",
                    source = source,
                    category = category
                )
                savedArticles.add(articleRepository.save(article))
            }
            logger.info("✅ Saved ${savedArticles.size} articles from $source")
        } catch (e: Exception) {
            logger.error("❌ Error fetching from $source: ${e.message}")
        }
        return savedArticles
    }

    // ── Read endpoints ────────────────────────────────────────
    fun getArticlesByCategory(category: VideoCategory): List<Article> =
        articleRepository.findByCategoryIn(listOf(category))

    fun getAllArticles(): List<Article> =
        articleRepository.findAll()

    @Cacheable(value = ["article-feed"], key = "#email")
    fun getArticlesByUserGenres(email: String): List<Article> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found!") }
        val profile = profileRepository.findByUserId(user.id)
            .orElseThrow { ResourceNotFoundException("Profile not found!") }
        val categories = profile.genres.map { VideoCategory.valueOf(it.name) }
        logger.info("📰 Fetching articles for: $categories")
        return articleRepository.findByCategoryIn(categories)
    }
}