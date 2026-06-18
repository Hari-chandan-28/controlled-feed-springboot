package com.controlled_feed.backend.content.model

data class CircuitLayout(
    val circuitKey: Int,
    val circuitName: String,
    val rotation: Double,
    val corners: List<CircuitPoint>,
    val trackPoints: List<CircuitPoint>
)

data class CircuitPoint(
    val x: Double,
    val y: Double,
    val number: Int? = null // corner number, null for plain track points
)