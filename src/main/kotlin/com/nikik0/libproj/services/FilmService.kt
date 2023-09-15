package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.FilmDto
import com.nikik0.libproj.mappers.mapToDto
import com.nikik0.libproj.mappers.mapToEntity
import com.nikik0.libproj.repositories.FilmRepository
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class FilmService (
    private val filmRepository: FilmRepository
        ){

    suspend fun getSingle(id: Long) = filmRepository.findById(id)?.mapToDto()

    suspend fun getAll() = filmRepository.findAll().map { it.mapToDto() }

    suspend fun save(filmDto: FilmDto): FilmDto {
        return filmRepository.findById(filmDto.id)
            ?.let {
                filmRepository.save(
                    filmDto.mapToEntity()
                ).mapToDto()
            }
            ?: filmRepository.save(
                filmDto.mapToEntity()
            ).mapToDto()
    }

    suspend fun
}