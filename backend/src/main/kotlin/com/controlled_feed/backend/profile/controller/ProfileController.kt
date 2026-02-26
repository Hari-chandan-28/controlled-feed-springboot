package com.controlled_feed.backend.profile.controller
import com.controlled_feed.backend.profile.model.Genre
import com.controlled_feed.backend.profile.model.Profile
import com.controlled_feed.backend.profile.service.ProfileService
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File

@RequestMapping("/api/profile")
class ProfileController (private val profileService: ProfileService) {
    @PostMapping("/create")
    fun create(@AuthenticationPrincipal email: String,
               @RequestBody request : CreateProfileRequest
    ): ResponseEntity<Profile> {
        val profile = profileService.createProfile(
            email = email,
            bio= request.bio,
            genres = request.genres
        )
        return ResponseEntity.ok(profile)
    }
    @GetMapping("/me")
    fun getProfile(
        @AuthenticationPrincipal email: String
    ): ResponseEntity<Profile> {
        val profile = profileService.getProfile(email)
        return ResponseEntity.ok(profile)
    }
    @PostMapping("/upload-picture")
    fun uploadProfilePicture(
        @AuthenticationPrincipal email: String,
        @RequestParam("file") file: MultipartFile
    ):ResponseEntity<Profile> {
        val uploadDir = File("uploads/profile-picture")
        if (!uploadDir.exists())uploadDir.mkdirs()
        val fileName = "${System.currentTimeMillis()}.${file.originalFilename}"
        val filePath = "uploads/profile-picture/$fileName"
        file.transferTo(File(filePath))
        val profile = profileService.updateProfilePicture(email,filePath)
        return ResponseEntity.ok(profile)
    }
}
data class CreateProfileRequest(
    val bio: String?,
    val genres: List<Genre>
)