package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.mapToDto
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
    private val actorRepository: ActorRepository,
    private val studioRepository: StudioRepository,
    private val tagRepository: TagRepository,
    private val manyToManyRepository: ManyToManyRepository
) {

    suspend fun getOne(id: Long) =
        movieRepository.findById(id)?.apply {
            this.actors = actorRepository.findActorsForMovie(this.id).toList()
            this.studio = studioRepository.findStudioForMovie(this.id).toList().first()
            this.tags = tagRepository.findTagsForMovie(this.id).toList()
        }?.mapToDto()


    @Transactional
    suspend fun saveOne(movieDto: MovieDto): MovieDto {
        val movieEntity = movieRepository.save(MovieEntity(
            id = movieDto.id,
            name = movieDto.name,
            producer = movieDto.producer,
            actors = actorRepository.saveAll(movieDto.actors).toList(), //todo check if entities exist
            tags = tagRepository.saveAll(movieDto.tags).toList(),
            studio = if (movieDto.studio != null) studioRepository.save(movieDto.studio) else null,
            budget = movieDto.budget,
            movieUrl = movieDto.movieUrl
        ))
        manyToManyRepository.movieActorInsert(movieEntity.id, movieEntity.actors.map { it.id })
        manyToManyRepository.tagMovieInsert(movieEntity.tags.map { it.id }, movieEntity.id)
        if (movieEntity.studio != null) manyToManyRepository.studioMovieInsert(movieEntity.studio!!.id, movieEntity.id)
        return movieEntity.mapToDto()
    }

    suspend fun getAllYeager() = movieRepository.findAll().map {
        it.actors = actorRepository.findActorsForMovie(it.id).toList()
        it.studio = studioRepository.findStudioForMovie(it.id).toList().first()
        it.tags = tagRepository.findTagsForMovie(it.id).toList()
        it.mapToDto()
    }

    suspend fun getAllLazy() = movieRepository.findAll().map { it.mapToDto() }
    suspend fun findByTag(tag: String) : Flow<MovieDto>{
        val tagFromRepo = tagRepository.findByName(tag).first()
        return movieRepository.findMoviesByTagId(tagFromRepo.id).map { it.mapToDto() }
    }



}