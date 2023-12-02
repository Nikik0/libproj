package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.*
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

    private var customerSavedDummy: CustomerDto? = null

    private val customerUnsavedFirstDummy: String = """
        {
            "name": "First name",
            "surname": "First Surname",
            "country": "First country",
            "state": "First state",
            "city": "First city",
            "district": "First district",
            "street": "First street",
            "building": 1,
            "buildingLiteral": "First literal",
            "apartmentNumber": 23,
            "additionalInfo": "First additional info"
        }
    """.trimIndent()

    private val customerUnsavedSecondDummy: String = """
                {
                    "name": "Second name",
                    "surname": "Second Surname",
                    "country": "Second country",
                    "state": "Second state",
                    "city": "Second city",
                    "district": "Second district",
                    "street": "Second street",
                    "building": 1,
                    "buildingLiteral": "Second literal",
                    "apartmentNumber": 23,
                    "additionalInfo": "Second additional info",
                    "watched": [],
                    "favourites":[]
                }
    """.trimIndent()

    private var movieSavedDummy: MovieDto? = null

    private val movieUnsavedFirstDummy: String = """
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

    private val movieUnsavedSecondDummy: String = """
            {
                "name": "Second Test Movie",
                "producer": "Second Test Producer",
                "actors": [
                    {
                        "name": "Third Actor name",
                        "surname": "Third Actor surname",
                        "age": 20
                    },
                    {
                        "name": "Forth Actor name",
                        "surname": "Forth Actor surname",
                        "age": 30
                    }
                ],
                "tags": [
                    {
                        "name": "Mystic"
                    },
                    {
                        "name": "Action"
                    }
                ],
                "studio": {
                    "name": "Second Test Studio",
                    "employees": 2000,
                    "owner": "Second Studio Owner"
                },
                "budget": 2000000,
                "movieUrl": "someurl.com/url2"
            }
        """.trimIndent()

    @BeforeEach
    fun setupTestCustomer(){
        val address = AddressEntity(
            id = 1,
            country = "First country",
            state = "First state",
            city = "First city",
            district = "First district",
            street = "First street",
            building = 1,
            buildingLiteral = "First literal",
            apartmentNumber = 23,
            additionalInfo = "First additional info"
        )
        customerSavedDummy = CustomerEntity(
            id = 1,
            name = "First name",
            surname = "First Surname",
            address = address,
            watched = emptyList(),
            favorites = emptyList()
        ).toDto()
    }

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
            .bodyValue(movieUnsavedFirstDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        assertThat(entity).isEqualTo(movieSavedDummy)
        webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()

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
            .exchange().expectBodyList(MovieDto::class.java).hasSize(2)
            //.consumeWith<WebTestClient.ListBodySpec<MovieDto>>(System.out::println)
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
            .expectBodyList(MovieDto::class.java).hasSize(2)
            //.consumeWith<WebTestClient.ListBodySpec<MovieDto>>(System.out::println)
            .returnResult()
        val dto = retrievedListOfMovies.responseBody?.get(0)
        val actors = dto?.actors
        val tags = dto?.tags
        assertThat(actors).isEmpty()
        assertThat(tags).isEmpty()
    }

    @Test
    @Order(5)
    fun `get by tag returns correct amount of dtos`(){
        webClient.get().uri("/api/v1/movie/find/tag/Horror").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(1)
//            .consumeWith<WebTestClient.ListBodySpec<MovieDto>>(System.out::println)
            .returnResult()
        webClient.get().uri("/api/v1/movie/find/tag/Action").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(2)
//            .consumeWith<WebTestClient.ListBodySpec<MovieDto>>(System.out::println)
            .returnResult()
    }

    @Test
    @Order(6)
    fun `save customer returns correct customer dto`(){
        val customerDto = webClient.post().uri("/api/v1/customer/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedFirstDummy).exchange().returnResult<CustomerDto>().responseBody.blockLast()
//        println(customerDto)
            assertThat(customerDto).isEqualTo(customerSavedDummy)
        webClient.post().uri("/api/v1/customer/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedSecondDummy).exchange().returnResult<CustomerDto>()
    }

    @Test
    @Order(7)
    fun `get customer returns single customerDto`(){
        val customerDto = webClient.get().uri("/api/v1/customer/get/1").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto).isEqualTo(customerSavedDummy)
    }

    @Test
    @Order(8)
    fun `get all customers returns correct number of dtos`(){
        webClient.get().uri("/api/v1/customer/get/all").exchange()
            .expectBodyList(CustomerDto::class.java).hasSize(2)
            .returnResult().responseBody
    }

    @Test
    @Order(9)
    fun `add to watched returns dto with correct watched list`(){
        val cust = webClient.get().uri("/api/v1/customer/get/1").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val movie = webClient.get().uri("/api/v1/movie/get/${movieSavedDummy?.id}").exchange()
            .returnResult<MovieDto>().responseBody.blockLast()
        println("for cust $cust adding movie $movie")
        val customer = webClient.post().uri("/api/v1/customer/1/watched/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedDummy!!).exchange()
            .returnResult<Any>().responseBody.blockLast()
        println("watched added for customer $customer")
    }


}