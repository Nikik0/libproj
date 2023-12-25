package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.dtos.mapToEntity
import com.nikik0.libproj.entities.*
import com.nikik0.libproj.repositories.ActorRepository
import com.nikik0.libproj.repositories.ManyToManyRepository
import com.nikik0.libproj.repositories.MovieRepository
import com.nikik0.libproj.repositories.TagRepository
import com.nikik0.libproj.services.*
import io.mockk.*
import io.mockk.InternalPlatformDsl.toArray
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
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
        movieEntity1 = movieDto1.mapToEntity()
        movieEntity2 = movieDto2.mapToEntity()
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

    private fun setUpMocks(){

        //save
        coEvery { tagService.saveTagIfNotPresent(tag1) } returns tag1
        coEvery { tagService.saveTagIfNotPresent(tag2) } returns tag2
        coEvery { actorService.saveActorIfNotPresent(actor1) } returns actor1
        coEvery { actorService.saveActorIfNotPresent(actor2) } returns actor2
        coEvery { studioService.saveStudioIfNotPresent(studio1) } returns studio1
        coEvery { movieRepository.save(movieEntity1) } returns movieEntity1
        coEvery { manyToManyRepository.movieActorInsert(1, listOf(1, 2)) } returns Unit
        coEvery { manyToManyRepository.tagMovieInsert(listOf(1, 2), 1) } returns Unit
        coEvery { manyToManyRepository.studioMovieInsert(1, 1) } returns Unit

        //getOne
        coEvery { movieRepository.findById(1) } returns movieEntity1
        coEvery { actorService.findActorsForMovie(1) } returns listOf(actor1, actor2)
        coEvery { tagService.findTagsForMovie(1) } returns listOf(tag1, tag2)
        coEvery { studioService.findStudioForMovie(1) } returns studio1

        //getYeager
        coEvery { movieRepository.findById(2) } returns movieEntity2
        coEvery { actorService.findActorsForMovie(2) } returns listOf(actor3, actor4)
        coEvery { tagService.findTagsForMovie(2) } returns listOf(tag3, tag2)
        coEvery { movieRepository.findById(1) } returns movieEntity1
        coEvery { movieRepository.findById(2) } returns movieEntity2
        coEvery { movieRepository.findAll() } returns listOf(movieEntity1, movieEntity2).asFlow()
        coEvery { studioService.findStudioForMovie(2) } returns studio2

    }

    @BeforeEach
    fun setup(){
        setupEntities()
        setUpMocks()
    }

    //todo this is a disaster and should be rewritten ( but works smh)
    @Test
    @DisplayName("saveOne returns single corresponding dto for movieDto after saving")
    fun saveOneShouldReturnDtoWhenOK() = runTest {
        assertEquals(movieService.saveOne(movieDto1), movieDto1)
    }

    @Test
    fun getOneShouldReturnDtoWhenOk() = runTest {
        coEvery { movieRepository.findById(1) } returns movieEntity1
        coEvery { actorService.findActorsForMovie(1) } returns listOf(actor1, actor2)
        coEvery { tagService.findTagsForMovie(1) } returns listOf(tag1, tag2)
        coEvery { studioService.findStudioForMovie(1) } returns studio1

        assertEquals(movieService.getOne(1), movieDto1)
    }

    @Test
    fun findByIdReturnsEntityWhenOk() = runTest {
        coEvery { movieRepository.findById(1) } returns movieEntity1
        assertEquals(movieService.findById(1), movieEntity1)
    }

    @Test
    fun getAllYeagerReturnsMultipleMovieDto() = runTest{

        assertEquals(movieService.getAllYeager().toList(), listOf(movieDto1, movieDto2))
    }

    @Test
    fun getAllLazyReturnsMultipleMovieDtoWithoutActorsEtc() = runTest {
        //todo not working somehow?
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
        println(movieService.getAllLazy().toList())
        assertEquals(movieService.getAllLazy().toList(), listOf(movieDtoLazy1, movieDtoLazy2))
    }


    @Test
    fun findByTagReturnsCorrectDto() = runTest {
        // given
        coEvery { tagService.findByName("Horror") } returns tag1
        coEvery { tagService.findByName("Action") } returns tag2
        coEvery { movieRepository.findMoviesByTagId(tag1.id) } returns flowOf(movieEntity1)
        coEvery { movieRepository.findMoviesByTagId(tag2.id) } returns flowOf(movieEntity1, movieEntity2)

        // when
        val resultFirst = movieService.findByTag("Horror")!!.map { it.id }.toList()
        val resultSecond = movieService.findByTag("Action")!!.map { it.id }.toList()

        // then
        assert(resultFirst == listOf(movieEntity1.id))
        assert(resultSecond == listOf(movieEntity1.id, movieEntity2.id))
    }

    @Test
    fun tets(){
        assertEquals(12, 6+6)
    }
}