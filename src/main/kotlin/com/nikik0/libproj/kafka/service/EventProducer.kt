package com.nikik0.libproj.kafka.service

import com.nikik0.libproj.kafka.model.Event

interface EventProducer {
    suspend fun publish(event: Event)
}