package com.controlled_feed.backend.auth.controller
import com.controlled_feed.backend.auth.service.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
){
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<Any>{
            val token = authService.signup(
                username = request.name,
                email = request.email,
                password = request.password
            )
        return ResponseEntity.ok(AuthResponse(token))
    }
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any>
    {
        val token = authService.login(
            email = request.email,
            password = request.password
        )
        return ResponseEntity.ok(AuthResponse(token))
    }
}
data class SignupRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 2, message = "Name must be between 2 characters")
    val name: String,
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 6, message = "Password must be at least 6 characters")
    val password: String
)
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String
)
data class AuthResponse(
    val token: String
)