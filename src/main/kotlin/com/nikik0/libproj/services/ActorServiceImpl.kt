package com.nikik0.libproj.services

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.repositories.ActorRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class ActorServiceImpl(
    private val actorRepository: ActorRepository
): ActorService {

    override suspend fun saveActorIfNotPresent(actor: Actor) =
        actorRepository.findByNameAndSurname(actor.name, actor.surname)?:actorRepository.save(actor)

    override suspend fun findActorsForMovie(movieId: Long) =
        actorRepository.findActorsForMovie(movieId).toList()
}