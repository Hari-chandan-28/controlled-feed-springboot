package com.controlled_feed.backend.auth.controller
import com.controlled_feed.backend.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
){
    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<Any>{
            val token = authService.signup(
                username = request.name,
                email = request.email,
                password = request.password
            )
        return ResponseEntity.ok(AuthResponse(token))
    }
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any>
    {
        val token = authService.login(
            email = request.email,
            password = request.password
        )
        return ResponseEntity.ok(AuthResponse(token))
    }
}
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)
data class LoginRequest(
    val email: String,
    val password: String
)
data class AuthResponse(
    val token: String
)