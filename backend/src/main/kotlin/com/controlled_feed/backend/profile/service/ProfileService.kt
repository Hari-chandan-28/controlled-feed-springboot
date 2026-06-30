package com.controlled_feed.backend.profile.service
import com.controlled_feed.backend.auth.repository.UserRepository
import com.controlled_feed.backend.common.*
import com.controlled_feed.backend.content.service.FeedService
import com.controlled_feed.backend.profile.controller.ProfileController
import com.controlled_feed.backend.profile.model.Genre
import com.controlled_feed.backend.profile.model.Profile
import com.controlled_feed.backend.profile.repository.ProfileRepository
import org.springframework.stereotype.Service
import com.controlled_feed.backend.profile.controller.ProfileUpdateRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict

@Service
class ProfileService(private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,private val feedService: FeedService
) {
    val logger = LoggerFactory.getLogger(ProfileService::class.java)
    fun createProfile(email: String,bio : String?,genres: List<Genre>): Profile {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found!") }
        if(profileRepository.existsByUserId(user.id)){
            throw AlreadyExistsException("Profile already exists!")
        }

        val profile = Profile(
            user = user,
            bio = bio,
            genres = genres
        )
        return profileRepository.save(profile)
    }
    fun getProfile(email:String): Profile {
        val user = userRepository.findByEmail(email)
        .orElseThrow { ResourceNotFoundException("User not found!") }

        return profileRepository.findByUserId(user.id)
                .orElseThrow{ResourceNotFoundException("Profile not found!")}
    }

    fun updateProfile(email: String, request: ProfileUpdateRequest): Profile {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found!") }
        val profile = profileRepository.findByUserId(user.id)
            .orElseThrow { ResourceNotFoundException("Profile not found!") }
        val updated = profile.copy(
            bio = request.bio,
            genres = request.genres
        )
        feedService.clearFeedCache()        // clears all feed cache entries
        feedService.clearArticleCache()     // add this — clears all article cache
        return profileRepository.save(updated)
    }

    fun updateProfilePicture(email: String, picturePath:String): Profile {
        val user = userRepository.findByEmail(email)
        .orElseThrow { ResourceNotFoundException("User not found!") }
        val profile = profileRepository.findByUserId(user.id)
        .orElseThrow { ResourceNotFoundException("Profile not found!")}


        val oldRelativePath = profile.profilePicturePath
        if (!oldRelativePath.isNullOrBlank()) {
            try {
                // Same base dir resolution as controller
                val baseDir = java.io.File(System.getProperty("user.dir"))

                val oldFile = java.io.File(baseDir, oldRelativePath)
                if (oldFile.exists()) {
                    oldFile.delete()
                    logger.info("🗑️ Deleted old profile picture: ${oldFile.absolutePath}")
                } else {
                    logger.warn("Old profile picture not found at: ${oldFile.absolutePath}")
                }
            } catch (e: Exception) {
                logger.warn("Could not delete old profile picture: ${e.message}")
            }
        }

        val updatedProfile = profile.copy(profilePicturePath = picturePath)
        return profileRepository.save(updatedProfile)
    }
    fun updateName(email: String, newName: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val updated = user.copy(name = newName)
        userRepository.save(updated)
    }
}