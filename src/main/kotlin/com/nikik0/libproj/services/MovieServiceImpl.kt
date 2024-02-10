package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.*
import com.nikik0.libproj.kafka.model.EntityAffected
import com.nikik0.libproj.kafka.model.Event
import com.nikik0.libproj.kafka.model.EventType
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.apache.commons.logging.LogFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MovieServiceImpl(
    private val movieRepository: MovieRepository,
    private val actorService: ActorService,
    private val tagService: TagService,
    private val studioService: StudioService,
    private val manyToManyRepository: ManyToManyRepository,
    private val eventProducer: EventProducer
) : MovieService {
    companion object{
        private val logger = LogFactory.getLog(MovieServiceImpl::class.java)
    }
    override suspend fun getOneYeager(id: Long) =
        movieRepository.findById(id)?.apply {
            this.actors = actorService.findActorsForMovie(this.id)
            this.studio = studioService.findStudioForMovie(this.id)
            this.tags = tagService.findTagsForMovie(this.id)
        }?.mapToDto().also {
            logger.info("Successfully retrieved movie ${it?.id}")
        }

    @Transactional
    override suspend fun saveOne(movieDto: MovieDto): MovieDto {
        val movieEntity = movieRepository.save(MovieEntity(
            id = movieDto.id,
            name = movieDto.name,
            producer = movieDto.producer,
            actors = movieDto.actors.map { actorService.saveActorIfNotPresent(it) },
            tags = movieDto.tags.map { tagService.saveTagIfNotPresent(it) },
            studio = if (movieDto.studio != null) studioService.saveStudioIfNotPresent(movieDto.studio) else null,
            budget = movieDto.budget,
            movieUrl = movieDto.movieUrl
        ))
        manyToManyRepository.movieActorInsert(movieEntity.id, movieEntity.actors.map { it.id })
        manyToManyRepository.tagMovieInsert(movieEntity.tags.map { it.id }, movieEntity.id)
        if (movieEntity.studio != null) manyToManyRepository.studioMovieInsert(movieEntity.studio!!.id, movieEntity.id)
        return movieEntity.mapToDto().also {
            logger.info("Successfully saved movie ${it.id}")
            eventProducer.publish(
                Event(
                    it.id,
                    EventType.CREATE,
                    EntityAffected.MOVIE,
                    "Movie saved"
                )
            )
        }
    }

    override suspend fun getAllYeager() = movieRepository.findAll().map {
        it.actors = actorService.findActorsForMovie(it.id)
        it.studio = studioService.findStudioForMovie(it.id)
        it.tags = tagService.findTagsForMovie(it.id)
        it.mapToDto()
    }.also {
        logger.info("Successfully retrieved movies (yeager)")
    }

    override suspend fun getAllLazy() = movieRepository.findAll().map { it.mapToDto() }.also {
        logger.info("Successfully retrieved movies (lazy)")
    }

    override suspend fun findByTag(tag: String) : Flow<MovieDto>? {
        val tagFromRepo = tagService.findByName(tag)
        return if (tagFromRepo != null) movieRepository.findMoviesByTagId(tagFromRepo.id).map { it.mapToDto() }.also {
            logger.info("Successfully retrieved movies by tag ${tagFromRepo.name}")
        } else null
    }

    override suspend fun findFavMoviesForCustomerId(customerId: Long) =
        movieRepository.findFavMoviesForCustomerId(customerId).also {
            logger.info("Successfully retrieved fav movies for customer $customerId")
        }

    override suspend fun findWatchedMoviesForCustomerId(customerId: Long) =
        movieRepository.findWatchedMoviesForCustomerId(customerId).also {
            logger.info("Successfully retrieved watched movies for customer $customerId")
        }

    override suspend fun getOneLazy(movieId: Long) =
        movieRepository.findById(movieId).also {
            logger.info("Successfully retrieved lazy movie ${it?.id}")
        }
}