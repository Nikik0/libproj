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
import org.json.JSONObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType


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
        val stringMovie = """
            {
                "name": "Car Crash",
                "producer": "Kate Stolk",
                "actors": [
                    {
                        "name": "Deri",
                        "surname": "Aaron",
                        "age": 43
                    },
                    {
                        "name": "Lizzy",
                        "surname": "Allen",
                        "age": 23
                    }
                ],
                "tags": [
                    {
                        "name": "Horror"
                    },
                    {
                        "name": "Documentary"
                    }
                ],
                "studio": {
                    "name": "Top studio",
                    "employees": 333,
                    "owner": "Neear D.R."
                },
                "budget": 23000000,
                "movieUrl": "smth.com/hherErw"
            }
        """.trimIndent()
        println(stringMovie)
        val json = JSONObject(stringMovie)
        println("json is $json")
//        JSONObject("sd")
//        val entity = client.postForEntity("/api/v1/movie/save", json, String::class.java)
//        println(entity)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val response = HttpEntity<String>(json.toString(),headers)
        val entity = client.postForObject("/api/v1/movie/save", response, MovieDto::class.java)
        println(entity)
//        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
//        println("saved this $entity")
        val retrievedEntity = client.getForEntity<MovieDto>("/api/v1/movie/get/1")
        println("got this $retrievedEntity")
        assertThat(entity).isEqualTo(retrievedEntity.body)
    }
}