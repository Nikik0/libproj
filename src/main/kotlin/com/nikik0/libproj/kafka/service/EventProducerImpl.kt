package com.nikik0.libproj.kafka.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.nikik0.libproj.kafka.model.Event
import com.nikik0.libproj.kafka.service.EventProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import kotlinx.coroutines.future.await

@Service
class EventProducerImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${spring.kafka.topic}")
    private val topic: String
): EventProducer {
    companion object{
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    override suspend fun publish(event: Event) {
        logger.info("Publishing event: $event")
        val result = kafkaTemplate.send(topic, objectMapper.writeValueAsString(event)).await()
        logger.info("Published event: $result")
    }
}