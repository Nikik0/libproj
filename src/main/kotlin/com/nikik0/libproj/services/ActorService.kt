package com.nikik0.libproj.services

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.repositories.ActorRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ActorService(
    private val actorRepository: ActorRepository
) {

    suspend fun saveActorIfNotPresent(actor: Actor) =
        actorRepository.findByNameAndSurname(actor.name, actor.surname)?:actorRepository.save(actor)

    suspend fun findActorsForMovie(movieId: Long) =
        actorRepository.findActorsForMovie(movieId).toList()
}