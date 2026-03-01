package com.controlled_feed.backend.content.model
import jakarta.persistence.*

enum class VideoCategory {
    F1,CRICKET
}
@Entity
@Table(name = "videos")
data class Video (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long=0,
    @Column(nullable = false , unique=true)
    val videoId: String = "",
    @Column(nullable=false)
    val title: String = "",
    @Column(columnDefinition = "TEXT")
    val description: String = "",
    @Column(nullable = false)
    val thumbnailUrl: String = "",
    @Column(nullable = false)
    val publishedAt: String = "",
    @Column(nullable = false)
    val channelTitle: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val category: VideoCategory = VideoCategory.F1
)