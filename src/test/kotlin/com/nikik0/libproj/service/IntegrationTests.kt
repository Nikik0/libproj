package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class IntegrationTests(
    @Autowired
    val webClient: WebTestClient
    ,
    @Autowired
    val client: DatabaseClient
) {
    companion object {
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:13.3"))
            .apply {
                this.withDatabaseName("movies-db").withUsername("test").withPassword("test123").withInitScript("init.sql")
            }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url", Companion::r2dbcUrl)
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
        }
        private fun r2dbcUrl(): String {
            return "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/${postgres.databaseName}"
        }

        @JvmStatic
        @BeforeAll
        internal fun setUp(){
            postgres.start()
        }
    }

    private var customerSavedFirstDummy: CustomerDto? = null

    private var customerSavedSecondDummy: CustomerDto? = null

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

    private var movieSavedFirstDummy: MovieDto? = null

    private var movieSavedSecondDummy: MovieDto? = null

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


    private val tables = listOf(
        "actor", "address",
        "customer", "customer_address", "customer_favourite_movies",
        "customer_watched_movies", "movie", "movie_actor",
        "studio", "studio_movie", "tag", "tag_movie"
    )

    @AfterEach
    fun cleanDb(){
        runBlocking {
            for (table in tables)
                client.sql("truncate $table restart identity cascade ").await()

        }
    }

    private fun setupTestCustomer(){
        val addressSecond = AddressEntity(
            id = 2,
            country = "Second country",
            state = "Second state",
            city = "Second city",
            district = "Second district",
            street = "Second street",
            building = 1,
            buildingLiteral = "Second literal",
            apartmentNumber = 23,
            additionalInfo = "Second additional info"
        )
        val addressFirst = AddressEntity(
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
        customerSavedFirstDummy = CustomerEntity(
            id = 1,
            name = "First name",
            surname = "First Surname",
            address = addressFirst,
            watched = emptyList(),
            favorites = emptyList()
        ).toDto()
        customerSavedSecondDummy = CustomerEntity(
            id = 2,
            name = "Second name",
            surname = "Second Surname",
            address = addressSecond,
            watched = emptyList(),
            favorites = emptyList()
        ).toDto()
    }

    private fun setupTestMovie() {
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
        val actorThree = Actor(
            id = 3L,
            name = "Third Actor name",
            surname = "Third Actor surname",
            age = 20
        )
        val actorFour = Actor(
            id = 4L,
            name = "Forth Actor name",
            surname = "Forth Actor surname",
            age = 30
        )
        val tagThree = MovieTag(
            id = 3L,
            name = "Mystic"
        )
        val tagTwo = MovieTag(
            id = 2L,
            name = "Action"
        )
        val tagOne = MovieTag(
            id = 1L,
            name = "Horror"
        )
        val studioOne = MovieStudio(
            id = 1L,
            name = "First Test Studio",
            employees = 2000L,
            owner = "First Studio Owner"
        )
        val studioTwo = MovieStudio(
            id = 2L,
            name = "Second Test Studio",
            employees = 2000L,
            owner = "Second Studio Owner"
        )
        movieSavedSecondDummy = MovieDto(
            id = 2L,
            name = "Second Test Movie",
            producer = "Second Test Producer",
            actors = listOf(actorThree, actorFour),
            tags = listOf(tagThree, tagTwo),
            studio = studioTwo,
            budget = 2000000L,
            movieUrl = "someurl.com/url2"
        )
        movieSavedFirstDummy = MovieDto(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = listOf(actorOne, actorTwo),
            tags = listOf(tagOne, tagTwo),
            studio = studioOne,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )
    }

    private fun insertTestData(){
        //first movie insert
        webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedFirstDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()

        //first customer insert
        webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedFirstDummy).exchange().returnResult<CustomerDto>().responseBody.blockLast()
    }

    @BeforeEach
    fun setupBeforeTests(){
        setupTestMovie()
        setupTestCustomer()
        insertTestData()
    }

    @Test
    fun `save should return correct new movieDto`() {
        val entity = webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        assertThat(entity).isEqualTo(movieSavedSecondDummy)
    }

    @Test
    fun `get single should return correct movieDto`() {
        val retrievedEntity = webClient.get().uri("/api/v1/movie/get/${movieSavedFirstDummy?.id}").exchange().returnResult<MovieDto>()
            .responseBody.blockLast()
        assertThat(retrievedEntity).isEqualTo(movieSavedFirstDummy)
    }

    @Test
    fun `get all yeager should return list of movieDto with nonnull actors and tags`(){
        webClient.get().uri("/api/v1/movie/get/all/yeager")
            .exchange().expectBodyList(MovieDto::class.java).hasSize(1)
            .returnResult()
        webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        val retrievedListOfMovies = webClient.get().uri("/api/v1/movie/get/all/yeager")
            .exchange().expectBodyList(MovieDto::class.java).hasSize(2)
            .returnResult()
        val dto = retrievedListOfMovies.responseBody?.get(0)
        val actors = dto?.actors
        val tags = dto?.tags
        assertThat(actors).isNotNull.isNotEmpty
        assertThat(tags).isNotNull.isNotEmpty
    }

    @Test
    fun `get all lazy should return list of movieDto with null actors and tags`(){
        webClient.get().uri("/api/v1/movie/get/all/lazy").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(1)
            .returnResult()
        webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        val retrievedListOfMovies = webClient.get().uri("/api/v1/movie/get/all/lazy").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(2)
            .returnResult()
        val dto = retrievedListOfMovies.responseBody?.get(0)
        val actors = dto?.actors
        val tags = dto?.tags
        assertThat(actors).isEmpty()
        assertThat(tags).isEmpty()
    }

    @Test
    fun `get by tag returns correct amount of dtos`(){
        webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        webClient.get().uri("/api/v1/movie/find/tag/Horror").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(1)
            .returnResult()
        webClient.get().uri("/api/v1/movie/find/tag/Action").exchange()
            .expectBodyList(MovieDto::class.java).hasSize(2)
            .returnResult()
    }

    @Test
    fun `save customer returns correct customer dto`(){
        val customerDto = webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedSecondDummy).exchange().returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto).isEqualTo(customerSavedSecondDummy)
    }

    @Test
    fun `get customer returns single customerDto`(){
        val customerDto = webClient.get().uri("/api/v1/customer/get/${customerSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto).isEqualTo(customerSavedFirstDummy)
    }

    @Test
    fun `get all customers returns correct number of dtos`(){
        webClient.get().uri("/api/v1/customer/get/all").exchange()
            .expectBodyList(CustomerDto::class.java).hasSize(1)
            .returnResult().responseBody
        webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedSecondDummy).exchange().returnResult<CustomerDto>()
        webClient.get().uri("/api/v1/customer/get/all").exchange()
            .expectBodyList(CustomerDto::class.java).hasSize(2)
            .returnResult().responseBody
    }

    @Test
    fun `add to watched returns dto with correct watched list`(){
        val customer = webClient.get().uri("/api/v1/customer/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val movie = webClient.get().uri("/api/v1/movie/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<MovieDto>().responseBody.blockLast()
        val customerUpdated = webClient.post().uri("/api/v1/customer/${customer!!.id}/watched/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerUpdated!!.watched).isNotNull.isNotEmpty.hasSize(1)
        assertThat(customerUpdated.watched!![0].id).isEqualTo(movie!!.id)
    }

    @Test
    fun `add to watched returns dto with correct favourites list`(){
        val customer = webClient.get().uri("/api/v1/customer/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val movie = webClient.get().uri("/api/v1/movie/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<MovieDto>().responseBody.blockLast()
        val customerUpdated = webClient.post().uri("/api/v1/customer/${customer!!.id}/favourites/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerUpdated!!.favourites).isNotNull.isNotEmpty.hasSize(1)
        assertThat(customerUpdated.favourites!![0].id).isEqualTo(movie!!.id)
    }

}