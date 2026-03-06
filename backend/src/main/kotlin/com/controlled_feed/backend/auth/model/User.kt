package com.controlled_feed.backend.auth.model
import jakarta.persistence.*
@Entity
@Table(name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email",unique = true)
    ]
)
data class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column (nullable = false ,unique = true)
    var email: String = "",
    @Column(nullable = false)
    var password: String? = "",
    @Column(nullable = false)
    var name: String = "",
    @Column(nullable = false)
    var role: String = "USER"
    )