package com.controlled_feed.backend.profile.controller
import com.controlled_feed.backend.profile.model.Genre
import com.controlled_feed.backend.profile.model.Profile
import com.controlled_feed.backend.profile.service.ProfileService
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RestController
@RequestMapping("/api/profile")
class ProfileController (private val profileService: ProfileService) {
    @PostMapping("/create")
    fun create(
        @RequestBody request: CreateProfileRequest
    ): ResponseEntity<Profile> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        val profile = profileService.createProfile(
            email = email,
            bio = request.bio,
            genres = request.genres
        )
        return ResponseEntity.ok(profile)
    }

    @GetMapping("/me")
    fun getProfile(): ResponseEntity<Profile> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        val profile = profileService.getProfile(email)
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/update")
    fun updateProfile(@RequestBody request: ProfileUpdateRequest): ResponseEntity<Profile> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")
        val profile = profileService.updateProfile(email, request)
        return ResponseEntity.ok(profile)
    }

    @PostMapping("/upload-picture")
    fun uploadProfilePicture(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Profile> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")

        // Use absolute path
        val uploadDir = File(System.getProperty("user.dir") + "/uploads/profile-pictures")
        if (!uploadDir.exists()) uploadDir.mkdirs()

        val fileName = "${System.currentTimeMillis()}_${file.originalFilename}"
        val destFile = File(uploadDir, fileName)
        file.transferTo(destFile.absoluteFile)

        val filePath = "uploads/profile-pictures/$fileName"
        val profile = profileService.updateProfilePicture(email, filePath)
        return ResponseEntity.ok(profile)
    }

    @PutMapping("/update-name")
    fun updateName(@RequestBody request: UpdateNameRequest): ResponseEntity<String> {
        val email = SecurityContextHolder.getContext().authentication?.name
            ?: throw RuntimeException("Not authenticated")

        profileService.updateName(email, request.name)
        return ResponseEntity.ok("Name updated successfully")
    }
}
data class UpdateNameRequest(val name: String)
data class CreateProfileRequest(
    val bio: String?,
    val genres: List<Genre>
)
data class ProfileUpdateRequest(
    val bio: String? = null,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val genres: List<Genre> = emptyList()  // ← default prevents null crash
)