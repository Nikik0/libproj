package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.MovieEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.transaction.annotation.Transactional

interface MovieService {
    suspend fun getOne(id: Long): MovieDto?

    @Transactional
    suspend fun saveOne(movieDto: MovieDto): MovieDto

    suspend fun getAllYeager(): Flow<MovieDto>

    suspend fun getAllLazy(): Flow<MovieDto>

    suspend fun findByTag(tag: String): Flow<MovieDto>?

    suspend fun findFavMoviesForCustomerId(customerId: Long): Flow<MovieEntity>

    suspend fun findWatchedMoviesForCustomerId(customerId: Long): Flow<MovieEntity>

    suspend fun findById(movieId: Long): MovieEntity?
}