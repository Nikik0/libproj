package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieStudio

interface StudioService {
    suspend fun saveStudioIfNotPresent(studio: MovieStudio): MovieStudio

    suspend fun findStudioForMovie(movieId: Long): MovieStudio
}