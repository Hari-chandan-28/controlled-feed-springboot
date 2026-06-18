package com.controlled_feed.backend.content.model

import java.io.Serializable

data class CircuitLayout(
    val circuitKey: Int,
    val circuitName: String,
    val rotation: Double,
    val corners: List<CircuitPoint>,
    val trackPoints: List<CircuitPoint>
) : Serializable

data class CircuitPoint(
    val x: Double,
    val y: Double,
    val number: Int? = null
) : Serializable