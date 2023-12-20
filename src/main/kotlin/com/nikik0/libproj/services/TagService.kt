package com.nikik0.libproj.services

import com.nikik0.libproj.entities.MovieTag

interface TagService {
    suspend fun saveTagIfNotPresent(tag: MovieTag): MovieTag

    suspend fun findTagsForMovie(movieId: Long): List<MovieTag>

    suspend fun findByName(name: String): MovieTag?
}