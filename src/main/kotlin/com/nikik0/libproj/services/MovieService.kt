package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.*
import com.nikik0.libproj.repositories.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MovieService(
    private val movieRepository: MovieRepository,
    private val actorService: ActorService,
    private val studioRepository: StudioRepository,
    private val tagService: TagService,
    private val manyToManyRepository: ManyToManyRepository
) {

    suspend fun getOne(id: Long) =
        movieRepository.findById(id)?.apply {
            this.actors = actorService.findActorsForMovie(this.id)
            this.studio = studioRepository.findStudioForMovie(this.id).toList().first()
            this.tags = tagService.findTagsForMovie(this.id)
        }?.mapToDto()

    suspend fun saveStudioIfNotPresent(studio: MovieStudio) =
        studioRepository.findByName(studio.name)?:studioRepository.save(studio)

    @Transactional
    suspend fun saveOne(movieDto: MovieDto): MovieDto {
        val movieEntity = movieRepository.save(MovieEntity(
            id = movieDto.id,
            name = movieDto.name,
            producer = movieDto.producer,
            actors = movieDto.actors.map { actorService.saveActorIfNotPresent(it) },
            tags = movieDto.tags.map { tagService.saveTagIfNotPresent(it) },
            studio = if (movieDto.studio != null) saveStudioIfNotPresent(movieDto.studio) else null,
            budget = movieDto.budget,
            movieUrl = movieDto.movieUrl
        ))
        manyToManyRepository.movieActorInsert(movieEntity.id, movieEntity.actors.map { it.id })
        manyToManyRepository.tagMovieInsert(movieEntity.tags.map { it.id }, movieEntity.id)
        if (movieEntity.studio != null) manyToManyRepository.studioMovieInsert(movieEntity.studio!!.id, movieEntity.id)
        return movieEntity.mapToDto()
    }

    suspend fun getAllYeager() = movieRepository.findAll().map {
        it.actors = actorService.findActorsForMovie(it.id)
        it.studio = studioRepository.findStudioForMovie(it.id).toList().first()
        it.tags = tagService.findTagsForMovie(it.id)
        it.mapToDto()
    }

    suspend fun getAllLazy() = movieRepository.findAll().map { it.mapToDto() }
    suspend fun findByTag(tag: String) : Flow<MovieDto>? {
        val tagFromRepo = tagService.findByName(tag)
        return if (tagFromRepo != null) movieRepository.findMoviesByTagId(tagFromRepo.id).map { it.mapToDto() } else null
    }



}