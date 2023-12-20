package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.repositories.StudioRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service

@Service
class StudioServiceImpl(
    private val studioRepository: StudioRepository
) : StudioService {
    override suspend fun saveStudioIfNotPresent(studio: MovieStudio) =
        studioRepository.findByName(studio.name) ?: studioRepository.save(studio)

    override suspend fun findStudioForMovie(movieId: Long) =
        studioRepository.findStudioForMovie(movieId).toList().first()

}