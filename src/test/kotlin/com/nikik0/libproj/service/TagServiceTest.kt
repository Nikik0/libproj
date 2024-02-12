package com.nikik0.libproj.service

import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag
import com.nikik0.libproj.kafka.service.EventProducer
import com.nikik0.libproj.repositories.StudioRepository
import com.nikik0.libproj.repositories.TagRepository
import com.nikik0.libproj.services.StudioServiceImpl
import com.nikik0.libproj.services.TagService
import com.nikik0.libproj.services.TagServiceImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TagServiceTest {
    @MockK
    lateinit var eventProducer: EventProducer

    @MockK
    lateinit var tagRepository: TagRepository

    @InjectMockKs
    lateinit var tagService: TagServiceImpl

    private lateinit var movieTag1: MovieTag

    private lateinit var movieTag2: MovieTag

    private lateinit var movieEntity: MovieEntity

    private fun setupEntities() {
        movieTag1 = MovieTag(
            id = 1L,
            name = "Horror"
        )
        movieTag2 = MovieTag(
            id = 2L,
            name = "Mystic"
        )
        movieEntity = MovieEntity(
            id = 1L,
            name = "Movie name",
            producer = "Producer",
            actors = emptyList(),
            tags = listOf(movieTag1, movieTag2),
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
    fun `saveTagIfNotPresent should return new saved tag if tag with name wasnt found in db`() = runTest {
        // given
        coEvery { tagRepository.save(movieTag1) } returns movieTag1
        coEvery { tagRepository.findByName(movieTag1.name) } returns null
        coEvery { eventProducer.publish(any()) } returns Unit

        // when
        val result = tagService.saveTagIfNotPresent(movieTag1)

        // then
        Assertions.assertEquals(movieTag1, result)
    }

    @Test
    fun `saveTagIfNotPresent should return found tag if tag with name was found in db`() = runTest {
        // given
        coEvery { tagRepository.save(movieTag1) } returns movieTag1
        coEvery { tagRepository.findByName(movieTag1.name) } returns movieTag1

        // when
        val result = tagService.saveTagIfNotPresent(movieTag1)

        // then
        Assertions.assertEquals(movieTag1, result)
    }

    @Test
    fun `findTagsForMovie should return list of tags in this movie`() = runTest {
        // given
        coEvery { tagRepository.findTagsForMovie(movieEntity.id) } returns movieEntity.tags.asFlow()

        // when
        val result = tagService.findTagsForMovie(movieEntity.id)

        // then
        Assertions.assertEquals(movieEntity.tags, result)
    }

    @Test
    fun `findByName should return tag with the name case insensitive`() = runTest {
        // given
        coEvery { tagRepository.findByNameIgnoreCase(movieTag1.name.uppercase()) } returns movieTag1

        // when
        val result = tagService.findByName(movieTag1.name.uppercase())

        // then
        Assertions.assertEquals(movieTag1, result)
    }
}