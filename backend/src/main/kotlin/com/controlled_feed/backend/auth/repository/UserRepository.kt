package com.controlled_feed.backend.auth.repository
import com.controlled_feed.backend.auth.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long>{
    fun findByUsername(username: String): Optional<User>
    fun existsByEmail(email: String): Boolean
}