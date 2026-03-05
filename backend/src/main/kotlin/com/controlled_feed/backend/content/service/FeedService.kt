package com.controlled_feed.backend.content.service

import com.controlled_feed.backend.auth.repository.UserRepository
import com.controlled_feed.backend.content.model.Video
import com.controlled_feed.backend.content.model.VideoCategory
import com.controlled_feed.backend.content.repository.VideoRepository
import com.controlled_feed.backend.profile.repository.ProfileRepository
import jakarta.validation.constraints.Email
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.data.domain.Pageable

@Service
class FeedService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val videoRepository: VideoRepository) {
    fun getFeed(email: String,page:Int,size:Int): List<Video> {
        val user=userRepository.findByEmail(email)
            .orElseThrow{ Exception("User not found email address") }
        val profile = profileRepository.findByUserId(user.id)
            .orElseThrow{RuntimeException("Profile not found!")}
        val genre = profile.genres.map{genre ->
            VideoCategory.valueOf(genre.name)
        }
        val pageable = PageRequest.of(page,size, Sort.by("publishedAt").descending())
        return videoRepository.findByCategoryIn(genre, pageable)
    }
}