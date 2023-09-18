package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.mappers.mapToDto
import com.nikik0.libproj.mappers.mapToEntity
import com.nikik0.libproj.repositories.MovieRepository
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class MovieService (
    private val movieRepository: MovieRepository
        ){

    suspend fun getSingle(id: Long) = movieRepository.findById(id)?.mapToDto()

    suspend fun getAll() = movieRepository.findAll().map { it.mapToDto() }

    suspend fun save(movieDto: MovieDto): MovieDto {
        return movieRepository.findById(movieDto.id)
            ?.let {
                movieRepository.save(
                    movieDto.mapToEntity() //todo should be different for saving partly changed entity
                ).mapToDto()
            }
            ?: movieRepository.save(
                movieDto.mapToEntity()
            ).mapToDto()
    }

}