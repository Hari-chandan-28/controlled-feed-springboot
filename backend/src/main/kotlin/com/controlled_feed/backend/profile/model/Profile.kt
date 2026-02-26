package com.controlled_feed.backend.profile.model

import com.controlled_feed.backend.auth.model.User
import jakarta.persistence.*

enum class Genre {
    F1, CRICKET
}

@Entity
@Table(name = "profiles")
data class Profile(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = true)
    val bio: String? = null,

    @Column(nullable = true)
    val profilePicturePath: String? = null,

    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name = "profile_genres", joinColumns = [JoinColumn(name = "profile_id")])
    @Column(name = "genre")
    val genres: List<Genre> = emptyList()
)