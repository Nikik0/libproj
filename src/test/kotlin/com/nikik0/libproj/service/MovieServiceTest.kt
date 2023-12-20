package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.dtos.mapToEntity
import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag
import com.nikik0.libproj.repositories.ActorRepository
import com.nikik0.libproj.repositories.ManyToManyRepository
import com.nikik0.libproj.repositories.MovieRepository
import com.nikik0.libproj.repositories.TagRepository
import com.nikik0.libproj.services.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.extension.ExtendWith

//@SpringBootTest
@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MovieServiceTest {
    @MockK
    lateinit var tagRepository: TagRepository
    @MockK
    lateinit var movieRepository: MovieRepository
    @MockK
    lateinit var actorRepository: ActorRepository
    @MockK
    lateinit var actorService: ActorServiceImpl
    @MockK
    lateinit var tagService: TagServiceImpl
    @MockK
    lateinit var studioService: StudioServiceImpl
    @MockK
    lateinit var manyToManyRepository: ManyToManyRepository
    //todo this should point to interface for injection
    @InjectMockKs
    lateinit var movieService: MovieServiceImpl

    private lateinit var movieDto: MovieDto

    private lateinit var movieEntity: MovieEntity

    private lateinit var actor1 : Actor

    private lateinit var actor2: Actor

    private lateinit var tag1: MovieTag

    private lateinit var tag2: MovieTag

    private lateinit var studio: MovieStudio


//    idk if this is need or not
//    @Before
//    fun setup1() {
//        MockKAnnotations.init(this, relaxUnitFun = true)
//    }

    @BeforeEach
    private fun setup() {
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
        tag1 = MovieTag(
            id = 1L,
            name = "Horror"
        )
        tag2 = MovieTag(
            id = 2L,
            name = "Action"
        )
        studio = MovieStudio(
            id = 1L,
            name = "First Test Studio",
            employees = 2000L,
            owner = "First Studio Owner"
        )
        movieDto = MovieDto(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = listOf(actor1, actor2),
            tags = listOf(tag1, tag2),
            studio = studio,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )

        movieEntity = movieDto.mapToEntity()
    }

//    @OptIn(DelicateCoroutinesApi::class)
//    private val mainThreadSurrogate = newSingleThreadContext("test thread")
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Before
//    fun setUp() {
//        Dispatchers.setMain(mainThreadSurrogate)
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain() // reset the main dispatcher to the original Main dispatcher
//        mainThreadSurrogate.close()
//    }
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testtest() = runTest  {
//        launch(Dispatchers.Main) {
//            println(movieService.saveOne(movieDto))
//            assertEquals(movieService.saveOne(movieDto), movieDto)
//        }
//    }


    //todo this is a disaster and should be rewritten ( but works smh)
    @Test
    @DisplayName("saveOne returns single corresponding dto for movieDto after saving")
    fun saveOneShouldReturnDtoWhenOK() = runTest {
        coEvery { tagService.saveTagIfNotPresent(tag1) } returns tag1
        coEvery { tagService.saveTagIfNotPresent(tag2) } returns tag2
        coEvery { actorService.saveActorIfNotPresent(actor1) } returns actor1
        coEvery { actorService.saveActorIfNotPresent(actor2) } returns actor2
        coEvery { studioService.saveStudioIfNotPresent(studio) } returns studio
        coEvery { movieRepository.save(movieEntity) } returns movieEntity
        coEvery { manyToManyRepository.movieActorInsert(1, listOf(1, 2)) } returns Unit
        coEvery { manyToManyRepository.tagMovieInsert(listOf(1, 2), 1) } returns Unit
        coEvery { manyToManyRepository.studioMovieInsert(1, 1) } returns Unit

        assertEquals(movieService.saveOne(movieDto), movieDto)
    }

    @Test
    fun getOneShouldReturnDtoWhenOk() = runTest {

    }

    @Test
    fun tets(){
        assertEquals(12, 6+6)
    }
}