package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.dtos.mapToEntity
import com.nikik0.libproj.entities.*
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.ManyToManyRepository
import com.nikik0.libproj.repositories.MovieRepository
import com.nikik0.libproj.services.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MovieServiceTest {
    @MockK
    lateinit var movieRepository: MovieRepository
    @MockK
    lateinit var eventProducer: EventProducer
    @MockK
    lateinit var actorService: ActorServiceImpl
    @MockK
    lateinit var tagService: TagServiceImpl
    @MockK
    lateinit var studioService: StudioServiceImpl
    @MockK
    lateinit var manyToManyRepository: ManyToManyRepository
    @InjectMockKs
    lateinit var movieService: MovieServiceImpl

    private lateinit var movieDto1: MovieDto

    private lateinit var movieEntity1: MovieEntity

    private lateinit var movieDto2: MovieDto

    private lateinit var movieEntity2: MovieEntity

    private lateinit var actor1 : Actor

    private lateinit var actor2: Actor

    private lateinit var actor3 : Actor

    private lateinit var actor4: Actor

    private lateinit var tag1: MovieTag

    private lateinit var tag2: MovieTag

    private lateinit var tag3: MovieTag

    private lateinit var studio1: MovieStudio

    private lateinit var studio2: MovieStudio

//    idk if this is need or not
//    @Before
//    fun setup1() {
//        MockKAnnotations.init(this, relaxUnitFun = true)
//    }


    private fun setupEntities() {
        actor1 = Actor(
            id = 1L,
            name = "First Actor name",
            surname = "First Actor surname",
            age = 20
        )
        actor2 = Actor(
            id = 2L,
            name = "Second Actor name",
            surname = "Second Actor surname",
            age = 30
        )
        actor3 = Actor(
            id = 3L,
            name = "Third Actor name",
            surname = "Third Actor surname",
            age = 20
        )
        actor4 = Actor(
            id = 4L,
            name = "Forth Actor name",
            surname = "Forth Actor surname",
            age = 30
        )
        tag2 = MovieTag(
            id = 2L,
            name = "Action"
        )
        tag1 = MovieTag(
            id = 1L,
            name = "Horror"
        )
        tag3 = MovieTag(
            id = 3L,
            name = "Mystic"
        )
        studio1 = MovieStudio(
            id = 1L,
            name = "First Test Studio",
            employees = 2000L,
            owner = "First Studio Owner"
        )
        studio2 = MovieStudio(
            id = 2L,
            name = "Second Test Studio",
            employees = 2000L,
            owner = "Second Studio Owner"
        )
        movieDto2 = MovieDto(
            id = 2L,
            name = "Second Test Movie",
            producer = "Second Test Producer",
            actors = listOf(actor3, actor4),
            tags = listOf(tag3, tag2),
            studio = studio2,
            budget = 2000000L,
            movieUrl = "someurl.com/url2"
        )
        movieDto1 = MovieDto(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = listOf(actor1, actor2),
            tags = listOf(tag1, tag2),
            studio = studio1,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )
        movieEntity1 = movieDto1.mapToEntity().apply {
            actors = emptyList()
            tags = emptyList()
            studio = null
        }
        movieEntity2 = movieDto2.mapToEntity().apply {
            actors = emptyList()
            tags = emptyList()
            studio = null
        }
    }


    @BeforeEach
    fun setup(){
        setupEntities()
    }

    @Test
    fun `saveOne returns correct movie dto when ok`() = runTest {
        // given
        coEvery { tagService.saveTagIfNotPresent(tag1) } returns tag1
        coEvery { tagService.saveTagIfNotPresent(tag2) } returns tag2
        coEvery { actorService.saveActorIfNotPresent(actor1) } returns actor1
        coEvery { actorService.saveActorIfNotPresent(actor2) } returns actor2
        coEvery { studioService.saveStudioIfNotPresent(studio1) } returns studio1
        coEvery { movieRepository.save(any(MovieEntity::class)) } returns movieEntity1.apply {
            actors = listOf(actor1, actor2)
            tags = listOf(tag1, tag2)
            studio = studio1
        }
        coEvery { manyToManyRepository.movieActorInsert(1, listOf(1, 2)) } returns Unit
        coEvery { manyToManyRepository.tagMovieInsert(listOf(1, 2), 1) } returns Unit
        coEvery { manyToManyRepository.studioMovieInsert(1, 1) } returns Unit
        coEvery { eventProducer.publish(any()) } returns Unit

        // when
        val result = movieService.saveOne(movieDto1)

        // then
        assertEquals(movieDto1, result)
    }

    @Test
    fun `getOneYeager returns correct movie dto when ok`() = runTest {
        // given
        coEvery { movieRepository.findById(1) } returns movieEntity1
        coEvery { actorService.findActorsForMovie(1) } returns listOf(actor1, actor2)
        coEvery { tagService.findTagsForMovie(1) } returns listOf(tag1, tag2)
        coEvery { studioService.findStudioForMovie(1) } returns studio1

        // when
        val result = movieService.getOneYeager(1)

        // then
        assertEquals(movieDto1, result)
    }

    @Test
    fun `getOneLazy returns correct movie entity when ok`() = runTest {
        // given
        coEvery { movieRepository.findById(1) } returns movieEntity1

        // when
        val result = movieService.getOneLazy(1)

        // then
        assertEquals(movieEntity1, result)
    }

    @Test
    fun `getAllYeager returns flow with correct dtos when ok`() = runTest {
        // given
        coEvery { actorService.findActorsForMovie(1) } returns listOf(actor1, actor2)
        coEvery { actorService.findActorsForMovie(2) } returns listOf(actor3, actor4)
        coEvery { tagService.findTagsForMovie(1) } returns listOf(tag1, tag2)
        coEvery { tagService.findTagsForMovie(2) } returns listOf(tag3, tag2)
        coEvery { studioService.findStudioForMovie(1) } returns studio1
        coEvery { studioService.findStudioForMovie(2) } returns studio2
        coEvery { movieRepository.findAll() } returns listOf(movieEntity1, movieEntity2).asFlow()

        // when
        val result = movieService.getAllYeager().toList()

        assertEquals(listOf(movieDto1, movieDto2), result)
    }

    @Test
    fun `getAllLazy returns flow with correct dtos with empty list of actors, tags and studio when ok`() = runTest {
        // given
        coEvery { movieRepository.findAll() } returns flowOf(movieEntity1, movieEntity2)
        val movieDtoLazy1 = MovieDto(
            id = movieDto1.id,
            name = movieDto1.name,
            producer = movieDto1.producer,
            actors = emptyList(),
            tags = emptyList(),
            studio = null,
            budget = movieDto1.budget,
            movieUrl = movieDto1.movieUrl
        )
        val movieDtoLazy2 = MovieDto(
            id = movieDto2.id,
            name = movieDto2.name,
            producer = movieDto2.producer,
            actors = emptyList(),
            tags = emptyList(),
            studio = null,
            budget = movieDto2.budget,
            movieUrl = movieDto2.movieUrl
        )

        // when
        val result = movieService.getAllLazy().toList()

        //then
        assertEquals(listOf(movieDtoLazy1, movieDtoLazy2), result)
    }


    @Test
    fun `findByTag returns correct dtos for different tags when ok`() = runTest {
        // given
        coEvery { tagService.findByName("Horror") } returns tag1
        coEvery { tagService.findByName("Action") } returns tag2
        coEvery { movieRepository.findMoviesByTagId(tag1.id) } returns flowOf(movieEntity1)
        coEvery { movieRepository.findMoviesByTagId(tag2.id) } returns flowOf(movieEntity1, movieEntity2)

        // when
        val result1 = movieService.findByTag("Horror")!!.map { it.id }.toList()
        val result2 = movieService.findByTag("Action")!!.map { it.id }.toList()

        // then
        assert(result1 == listOf(movieEntity1.id))
        assert(result2 == listOf(movieEntity1.id, movieEntity2.id))
    }

    @Test
    fun `findFavMoviesForCustomerId returns flow with correct dtos when ok`()= runTest{
        // given
        coEvery { movieRepository.findFavMoviesForCustomerId(1) } returns flowOf(movieEntity1)
        coEvery { movieRepository.findFavMoviesForCustomerId(2) } returns flowOf(movieEntity1, movieEntity2)

        // when
        val result1 = movieService.findFavMoviesForCustomerId(1).toList()
        val result2 = movieService.findFavMoviesForCustomerId(2).toList()

        // then
        assert(result1 == listOf(movieEntity1))
        assert(result2 == listOf(movieEntity1, movieEntity2))
    }

    @Test
    fun `findWatchedMoviesForCustomerId returns flow with correct dtos when ok`()= runTest{
        // given
        coEvery { movieRepository.findWatchedMoviesForCustomerId(1) } returns flowOf(movieEntity1)
        coEvery { movieRepository.findWatchedMoviesForCustomerId(2) } returns flowOf(movieEntity1, movieEntity2)

        // when
        val result1 = movieService.findWatchedMoviesForCustomerId(1).toList()
        val result2 = movieService.findWatchedMoviesForCustomerId(2).toList()

        // then
        assert(result1 == listOf(movieEntity1))
        assert(result2 == listOf(movieEntity1, movieEntity2))
    }


}