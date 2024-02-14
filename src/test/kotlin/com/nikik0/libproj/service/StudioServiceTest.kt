package com.nikik0.libproj.service

import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.StudioRepository
import com.nikik0.libproj.services.StudioServiceImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class StudioServiceTest {
    @MockK
    lateinit var eventProducer: EventProducer

    @MockK
    lateinit var studioRepository: StudioRepository

    @InjectMockKs
    lateinit var studioService: StudioServiceImpl

    private lateinit var studioEntity1: MovieStudio

    private lateinit var studioEntity2: MovieStudio

    private lateinit var movieEntity: MovieEntity

    private fun setupEntities() {
        studioEntity1 = MovieStudio(
            id = 1L,
            name = "First Test Studio",
            employees = 2000L,
            owner = "First Studio Owner"
        )
        studioEntity2 = MovieStudio(
            id = 2L,
            name = "Second Test Studio",
            employees = 2000L,
            owner = "Second Studio Owner"
        )
        movieEntity = MovieEntity(
            id = 1L,
            name = "Movie name",
            producer = "Producer",
            actors = emptyList(),
            tags = emptyList(),
            studio = studioEntity1,
            budget = 100L,
            movieUrl = "someurl.com/movie"
        )
    }

    @BeforeEach
    fun setup() {
        setupEntities()
    }

    @Test
    fun `saveStudioIfNotPresent should return new saved studio if studio with name wasnt found in db`() = runTest {
        // given
        coEvery { studioRepository.save(studioEntity1) } returns studioEntity1
        coEvery { studioRepository.findByName(studioEntity1.name) } returns null
        coEvery { eventProducer.publish(any()) } returns Unit

        // when
        val result = studioService.saveStudioIfNotPresent(studioEntity1)

        // then
        Assertions.assertEquals(studioEntity1, result)
    }

    @Test
    fun `saveStudioIfNotPresent should return found studio if studio with name was found in db`() = runTest {
        // given
        coEvery { studioRepository.save(studioEntity1) } returns studioEntity1
        coEvery { studioRepository.findByName(studioEntity1.name) } returns studioEntity1

        // when
        val result = studioService.saveStudioIfNotPresent(studioEntity1)

        // then
        Assertions.assertEquals(studioEntity1, result)
    }

    @Test
    fun `findStudioForMovie should return list of actors in this movie`() = runTest {
        // given
        coEvery { studioRepository.findStudioForMovie(movieEntity.id) } returns flowOf(studioEntity1)

        // when
        val result = studioService.findStudioForMovie(movieEntity.id)

        // then
        Assertions.assertEquals(movieEntity.studio, result)
    }
}