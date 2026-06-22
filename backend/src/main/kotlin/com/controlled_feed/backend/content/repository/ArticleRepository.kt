package com.controlled_feed.backend.content.repository

import com.controlled_feed.backend.content.model.Article
import com.controlled_feed.backend.content.model.VideoCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Pageable

@Repository
interface ArticleRepository : JpaRepository<Article, Long> {
    fun existsByGuid(guid: String): Boolean
    fun findByCategoryIn(categories: List<VideoCategory>): List<Article>
    fun findBySourceOrderByPublishedAtDesc(source: String): List<Article>
    fun findByCategoryIn(
        categories: List<VideoCategory>,
        pageable: Pageable
    ): List<Article>
}