package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.dtos.mapToEntity
import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag
import com.nikik0.libproj.repositories.MovieRepository
import com.nikik0.libproj.services.MovieService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import reactor.test.StepVerifier

class MovieServiceTest(
    @InjectMocks
    private val movieService: MovieService,
    @Mock
    private val movieRepository: MovieRepository,
) {

    private lateinit var movieDto : MovieDto

    private lateinit var movieEntity: MovieEntity

    @BeforeEach
    private fun setup() {
        val actorOne = Actor(
            id = 1L,
            name = "First Actor name",
            surname = "First Actor surname",
            age = 20
        )
        val actorTwo = Actor(
            id = 2L,
            name = "Second Actor name",
            surname = "Second Actor surname",
            age = 30
        )
        val tagOne = MovieTag(
            id = 1L,
            name = "Horror"
        )
        val tagTwo = MovieTag(
            id = 2L,
            name = "Action"
        )
        val studio = MovieStudio(
            id = 1L,
            name = "First Test Studio",
            employees = 2000L,
            owner = "First Studio Owner"
        )
        movieDto = MovieDto(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = listOf(actorOne, actorTwo),
            tags = listOf(tagOne, tagTwo),
            studio = studio,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )

        movieEntity = movieDto.mapToEntity()
    }

    @Test
    @DisplayName("saveOne returns single corresponding dto for movieDto after saving")
    suspend fun saveOneShouldReturnDtoWhenOK() {
        BDDMockito.`when`(movieRepository.save(Mockito.any(MovieEntity::class.java)))
            .thenReturn(movieEntity)

    }
}