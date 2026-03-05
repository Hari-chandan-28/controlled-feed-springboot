package com.controlled_feed.backend.profile.service
import com.controlled_feed.backend.auth.repository.UserRepository
import com.controlled_feed.backend.common.*
import com.controlled_feed.backend.profile.model.Genre
import com.controlled_feed.backend.profile.model.Profile
import com.controlled_feed.backend.profile.repository.ProfileRepository
import org.springframework.stereotype.Service

@Service
class ProfileService(private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository) {
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
    fun updateProfilePicture(email: String, picturePath:String): Profile {
        val user = userRepository.findByEmail(email)
        .orElseThrow { ResourceNotFoundException("User not found!") }
        val profile = profileRepository.findByUserId(user.id)
        .orElseThrow { ResourceNotFoundException("Profile not found!")}

        val updatedProfile = profile.copy(profilePicturePath = picturePath)
        return profileRepository.save(updatedProfile)
    }

}