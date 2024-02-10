package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.kafka.model.EntityAffected
import com.nikik0.libproj.kafka.model.Event
import com.nikik0.libproj.kafka.model.EventType
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.StudioRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class StudioServiceImpl(
    private val studioRepository: StudioRepository,
    private val eventProducer: EventProducer
) : StudioService {
    override suspend fun saveStudioIfNotPresent(studio: MovieStudio) =
        studioRepository.findByName(studio.name) ?: studioRepository.save(studio).also {
            eventProducer.publish(
                Event(
                    it.id,
                    EventType.CREATE,
                    EntityAffected.STUDIO,
                    "Studio ${it.name} saved"
                )
            )
        }

    override suspend fun findStudioForMovie(movieId: Long) =
        studioRepository.findStudioForMovie(movieId).toList().first()

}