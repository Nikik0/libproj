package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.repositories.StudioRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class StudioService(
    private val studioRepository: StudioRepository
) {
    suspend fun saveStudioIfNotPresent(studio: MovieStudio) =
        studioRepository.findByName(studio.name) ?: studioRepository.save(studio)

    suspend fun findStudioForMovie(movieId: Long) =
        studioRepository.findStudioForMovie(movieId).toList().first()

}