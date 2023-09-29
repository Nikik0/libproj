package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.Actor
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ActorRepository: CoroutineCrudRepository<Actor, Long> {
    @Query("select * from actor act join movie_actor ma on act.id = ma.actor_id where ma.movie_id = :movieId")
    fun findActorsForMovie(movieId: Long): Flow<Actor>
}