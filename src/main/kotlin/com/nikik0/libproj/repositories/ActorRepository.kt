package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.Actor
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ActorRepository: CoroutineCrudRepository<Actor, Long> {
}