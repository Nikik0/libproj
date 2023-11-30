package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag
import kotlinx.coroutines.flow.Flow
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
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserter
import org.springframework.web.reactive.function.BodyInserters


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class IntegrationTests(
    @Autowired
    val webClient: WebTestClient
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
//        @Container
//        val container = PostgreSQLContainer(DockerImageName.parse("postgres:13-alpine"))
//            .withDatabaseName("movies-db")
//            .withUsername("dev")
//            .withPassword("dev123")
//            .withInitScript("init.sql")
//
//
//        @JvmStatic
//        @DynamicPropertySource
//        fun datasourceConfig(registry: DynamicPropertyRegistry) {
//            registry.add("spring.datasource.url", container::getJdbcUrl)
//            registry.add("spring.datasource.password", container::getPassword)
//            registry.add("spring.datasource.username", container::getUsername)
//        }
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:13.3"))
            .apply {
                this.withDatabaseName("movies-db").withUsername("dev").withPassword("dev123").withInitScript("init.sql")
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url", Companion::r2dbcUrl)
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
        }
        fun r2dbcUrl(): String {
            return "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/${postgres.databaseName}"
        }

        @JvmStatic
        @BeforeAll
        internal fun setUp(): Unit {
            postgres.start()
        }
    }

    private var movieSavedDummy: MovieDto? = null

    private val movieUnsavedDummy: String = """
            {
                "name": "First Test Movie",
                "producer": "First Test Producer",
                "actors": [
                    {
                        "name": "First Actor name",
                        "surname": "First Actor surname",
                        "age": 20
                    },
                    {
                        "name": "Second Actor name",
                        "surname": "Second Actor surname",
                        "age": 30
                    }
                ],
                "tags": [
                    {
                        "name": "Horror"
                    },
                    {
                        "name": "Action"
                    }
                ],
                "studio": {
                    "name": "First Test Studio",
                    "employees": 2000,
                    "owner": "First Studio Owner"
                },
                "budget": 2000000,
                "movieUrl": "someurl.com/url"
            }
        """.trimIndent()

    @BeforeEach
    fun setupTestMovie() {
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
        movieSavedDummy = MovieDto(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = listOf(actorOne, actorTwo),
            tags = listOf(tagOne, tagTwo),
            studio = studio,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )
    }

    @Test
    @Order(1)
    fun `save should return correct new movieDto`() {
        val entity = webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        assertThat(entity).isEqualTo(movieSavedDummy)
    }

    @Test
    @Order(2)
    fun `get single should return correct movieDto`() {
        val retrievedEntity = webClient.get().uri("/api/v1/movie/get/1").exchange().returnResult<MovieDto>()
            .responseBody.blockLast()
        assertThat(retrievedEntity).isEqualTo(movieSavedDummy)
    }
    @Test
    @Order(3)
    fun `get all yeager should return list of movieDto with nonnull actors and tags`(){
        val retrievedListOfMovies = webClient.get().uri("/api/v1/movie/get/all/yeager")
            .exchange().expectBodyList(MovieDto::class.java)
            .consumeWith<WebTestClient.ListBodySpec<MovieDto>>(System.out::println).hasSize(1)
            .returnResult()
        val dto = retrievedListOfMovies.responseBody?.get(0)
        val actors = dto?.actors
        val tags = dto?.tags
        assertThat(actors).isNotNull.isNotEmpty
        assertThat(tags).isNotNull.isNotEmpty
    }

    @Test
    @Order(4)
    fun `get all lazy should return list of movieDto with null actors and tags`(){
        val retrievedListOfMovies = webClient.get().uri("/api/v1/movie/get/all/lazy").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(1)
            .consumeWith<WebTestClient.ListBodySpec<MovieDto>>(System.out::println).hasSize(1)
            .returnResult()
        val dto = retrievedListOfMovies.responseBody?.get(0)
        val actors = dto?.actors
        val tags = dto?.tags
        assertThat(actors).isEmpty()
        assertThat(tags).isEmpty()
    }
}