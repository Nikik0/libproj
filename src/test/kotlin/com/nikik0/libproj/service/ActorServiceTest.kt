package com.nikik0.libproj.service

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.repositories.ActorRepository
import com.nikik0.libproj.services.ActorServiceImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ActorServiceTest {
    @MockK
    lateinit var actorRepository: ActorRepository

    @InjectMockKs
    lateinit var actorService: ActorServiceImpl

    private lateinit var actorEntity1: Actor

    private lateinit var actorEntity2: Actor

    private lateinit var  movie: MovieEntity

    private fun setupEntities() {
        actorEntity1 = Actor(
            id = 1L,
            name = "First Actor name",
            surname = "First Actor surname",
            age = 20
        )
        actorEntity2 = Actor(
            id = 2L,
            name = "Second Actor name",
            surname = "Second Actor surname",
            age = 30
        )
        movie  = MovieEntity(
            id = 1L,
            name = "Movie name",
            producer = "Producer",
            actors = listOf(actorEntity1, actorEntity2),
            tags = emptyList(),
            studio = null,
            budget = 100L,
            movieUrl = "someurl.com/movie"
        )
    }

    @BeforeEach
    fun setup() {
        setupEntities()
    }

    @Test
    fun `saveActorIfNotPresent should return new saved actor if actor with name and surname wasnt found in db`() = runTest {
        // given
        coEvery { actorRepository.save(actorEntity1) } returns actorEntity1
        coEvery { actorRepository.findByNameAndSurname(actorEntity1.name, actorEntity1.surname) } returns null

        // when
        val result = actorService.saveActorIfNotPresent(actorEntity1)

        // then
        assertEquals(actorEntity1, result)
    }

    @Test
    fun `saveActorIfNotPresent should return found actor if actor with name and surname was found in db`() = runTest {
        // given
        coEvery { actorRepository.save(actorEntity1) } returns actorEntity1
        coEvery { actorRepository.findByNameAndSurname(actorEntity1.name, actorEntity1.surname) } returns actorEntity1

        // when
        val result = actorService.saveActorIfNotPresent(actorEntity1)

        // then
        assertEquals(actorEntity1, result)
    }

    @Test
    fun `findActorsForMovie should return list of actors in this movie`() = runTest {
        // given
        coEvery { actorRepository.findActorsForMovie(movie.id) } returns movie.actors.asFlow()

        // when
        val result = actorService.findActorsForMovie(movie.id)

        // then
        assertEquals(movie.actors, result)
    }
}