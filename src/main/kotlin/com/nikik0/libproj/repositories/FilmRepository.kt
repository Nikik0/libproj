package com.nikik0.libproj.repositories

import com.nikik0.libproj.entities.FilmEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface FilmRepository: CoroutineCrudRepository<FilmEntity,Long> {
}