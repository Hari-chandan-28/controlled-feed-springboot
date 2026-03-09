package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.content.model.Article
import com.controlled_feed.backend.content.repository.ArticleRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import com.controlled_feed.backend.content.model.VideoCategory
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import java.net.URL

@Service
class RssFeedService (private val articleRepository: ArticleRepository){
    private val logger = LoggerFactory.getLogger(javaClass)
    private val f1FeedUrls = listOf(
        "https://www.autosport.com/rss/f1/news/" to "Autosport",
        "https://www.motorsport.com/rss/f1/news/" to "Motorsport",
        "https://www.bbc.co.uk/sport/formula1/rss.xml" to "BBC Sport F1",
        "https://feeds.skysports.com/skysports/f1" to "Sky Sports F1"
    )
    private val cricketFeedUrls = listOf(
        "https://www.espncricinfo.com/rss/content/story/feeds/0.xml" to "ESPNCricinfo",
        "https://www.cricbuzz.com/rss-feeds/cricket-news" to "Cricbuzz",
        "https://www.bbc.co.uk/sport/cricket/rss.xml" to "BBC Sport Cricket",
        "https://feeds.skysports.com/skysports/cricket" to "Sky Sports Cricket"
    )
    fun fetchAndStoreF1Articles():List<Article> {
        logger.info("📰 Fetching F1 RSS articles from ${f1FeedUrls.size} sources...")
        return f1FeedUrls.flatMap{(url,source)->
            fetchAndStore(url,VideoCategory.F1,source)
        }
    }
    fun fetchAndStoreCricketArticles(): List<Article> {
        logger.info("📰 Fetching Cricket RSS articles from ${cricketFeedUrls.size} sources...")
        return cricketFeedUrls.flatMap { (url, source) ->
            fetchAndStore(url, VideoCategory.CRICKET, source)
        }
    }
    private fun fetchAndStore(url: String,category: VideoCategory,source: String): List<Article> {
        val savedArticles = mutableListOf<Article>()
        try{
         logger.info("Fetching from $source ...")
         val feed = SyndFeedInput().build(XmlReader(URL(url)))
            feed.entries.forEach {
                entry ->
                val guid = entry.uri?:entry.link?: return@forEach
                if(articleRepository.existsByGuid(guid)){
                    logger.info("Found existing article ${entry.uri}")
                    return@forEach
                }
                val imgUrl=entry.enclosures?.firstOrNull()?.url?:entry.foreignMarkup?.firstOrNull()?.text?:""
                val article = Article(
                    guid = guid,
                    title = entry.title,
                    description = entry.description?.value?:"",
                    link = entry.link,
                    imageUrl = imgUrl,
                    publishedAt = entry.publishedDate?.toString()?:"",
                    source = source,
                    category = category
                )
                savedArticles.add(articleRepository.save(article))
                logger.info("Saved article: ${article.title} [$source]")
            }
        }
        catch (e: Exception){
            logger.error("Error fetching articles from $source:${e.message}")
        }
        return savedArticles
    }
}