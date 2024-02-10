package com.nikik0.libproj.services

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.kafka.model.EntityAffected
import com.nikik0.libproj.kafka.model.Event
import com.nikik0.libproj.kafka.model.EventType
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.ActorRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ActorServiceImpl(
    private val actorRepository: ActorRepository,
    private val eventProducer: EventProducer
): ActorService {

    override suspend fun saveActorIfNotPresent(actor: Actor) =
        actorRepository.findByNameAndSurname(actor.name, actor.surname)?:actorRepository.save(actor).also {
            eventProducer.publish(
                Event(
                    it.id,
                    EventType.CREATE,
                    EntityAffected.ACTOR,
                    "Actor ${it.name + " " + it.surname} saved"
                )
            )
        }

    override suspend fun findActorsForMovie(movieId: Long) =
        actorRepository.findActorsForMovie(movieId).toList()
}