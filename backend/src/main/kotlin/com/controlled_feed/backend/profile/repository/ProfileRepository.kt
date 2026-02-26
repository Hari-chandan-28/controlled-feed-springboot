package com.controlled_feed.backend.profile.repository
import com.controlled_feed.backend.profile.model.Profile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
interface ProfileRepository : JpaRepository<Profile, Long> {
    fun findByUserId(userId: Long): Optional<Profile>
    fun existsByUserId(userId: Long): Boolean
}