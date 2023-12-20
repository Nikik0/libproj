package com.nikik0.libproj.services

import com.nikik0.libproj.entities.Actor
import kotlinx.coroutines.flow.toList

interface ActorService {
    suspend fun saveActorIfNotPresent(actor: Actor): Actor

    suspend fun findActorsForMovie(movieId: Long): List<Actor>
}