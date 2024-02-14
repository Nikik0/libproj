package com.nikik0.libproj.kafka.model


data class Event(
    val id: Long,
    val type: EventType,
    val entityAffected: EntityAffected,
    val message: String
)
