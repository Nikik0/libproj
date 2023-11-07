package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class IntegrationTests(
    @Autowired
    val client: TestRestTemplate
//    @Autowired
//    val jdbc: JdbcTemplate
) {
    companion object {
        /*
                @Container
        val container = postgres("13-alpine") {
            withDatabaseName("db")
            withUsername("user")
            withPassword("password")
            withInitScript("sql/schema.sql")
        }
         */

        // Kotlin 1.5.30
        @Container
        val container = PostgreSQLContainer(DockerImageName.parse("postgres:13-alpine"))
            .withDatabaseName("movies-db")
            .withUsername("dev")
            .withPassword("dev123")
            .withInitScript("init.sql")

        @JvmStatic
        @DynamicPropertySource
        fun datasourceConfig(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.username", container::getUsername)
        }
    }

    @Test
    fun `test hello endpoint`() {
        val entity = client.getForEntity<CustomerDto>("/api/v1/customer/testget")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        println(entity)
        assertThat(entity.body?.name).contains("Dad")
    }

    @Test
    fun `test saving`() {
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
        val movieDto = MovieDto(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = listOf(actorOne, actorTwo),
            tags = listOf(tagOne, tagTwo),
            studio = studio,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )
        val entity = client.postForEntity("/api/v1/movie/save", movieDto, MovieDto::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        println("saved this $entity")
        val retrievedEntity = client.getForEntity<MovieDto>("/api/v1/movie/get/1")
        println("got this $retrievedEntity")
        assertThat(entity).isEqualTo(retrievedEntity)
    }
}