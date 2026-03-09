package com.controlled_feed.backend.content.model
import jakarta.persistence.Entity
import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "articles")
data class Article (
    @Id
    @GeneratedValue(GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    val guid: String = "",
    val title: String = "",
    @Column(columnDefinition = "TEXT")
    val description: String = "",
    val link: String = "",
    val imageUrl: String = "",
    val publishedAt: String = "",
    val source: String = "",
    @Enumerated(EnumType.STRING)
    val category: VideoCategory = VideoCategory.F1
): Serializable