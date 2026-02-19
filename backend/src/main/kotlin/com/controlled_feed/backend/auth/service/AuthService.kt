package com.controlled_feed.backend.auth.service
import com.controlled_feed.backend.auth.model.User
import com.controlled_feed.backend.auth.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
){
    fun signup(username: String, email: String, password: String): String {
        if(userRepository.existsByEmail(email))
        {
            throw RuntimeException("Email already exists")
        }
        val user =User(
            name = username,
            email = email,
            password = passwordEncoder.encode(password)
        )
        userRepository.save(user)

        return jwtService.generateToken(email)
    }
    fun login(email: String, password: String): String {
            val user = userRepository.findByEmail(email)
                .orElseThrow{ RuntimeException("User not found") }
            if(!passwordEncoder.matches(password, user.password)){
                throw RuntimeException("Passwords do not match")
            }
        return jwtService.generateToken(user.email)
    }
}