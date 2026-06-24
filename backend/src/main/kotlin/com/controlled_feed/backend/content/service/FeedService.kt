package com.controlled_feed.backend.content.service
import com.controlled_feed.backend.auth.repository.UserRepository
import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.repository.VideoRepository
import com.controlled_feed.backend.profile.repository.ProfileRepository
import com.controlled_feed.backend.common.ResourceNotFoundException
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class FeedService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val videoRepository: VideoRepository
) {
    // Existing: paginated feed by user genres
    @Cacheable(value = ["feed"], key = "#email + '-' + #page + '-' + #size")
    fun getFeed(email: String, page: Int, size: Int): List<Video> {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found!") }
        val profile = profileRepository.findByUserId(user.id)
            .orElseThrow { ResourceNotFoundException("Profile not found!") }
        val genres = profile.genres.map { VideoCategory.valueOf(it.name) }
        val pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending())
        return videoRepository.findByCategoryIn(genres, pageable)
    }

    // NEW: paginated feed by specific category — fixes frontend pagination bug
    fun getVideosByCategory(category: VideoCategory, page: Int, size: Int): List<Video> {
        val pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending())
        return videoRepository.findByCategory(category, pageable)
    }

    // NEW: random mix across all categories
    fun getRandomFeed(size: Int): List<Video> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found!") }
        val profile = profileRepository.findByUserId(user.id)
            .orElseThrow { ResourceNotFoundException("Profile not found!") }
        val genres = profile.genres.map { VideoCategory.valueOf(it.name) }
        val all = videoRepository.findByCategoryIn(genres)
        return all.shuffled().take(size)
    }

    @CacheEvict(value = ["feed"], allEntries = true)
    fun clearFeedCache() {}

    @CacheEvict(value = ["article-feed"], allEntries = true)
    fun clearArticleCache() {}
}