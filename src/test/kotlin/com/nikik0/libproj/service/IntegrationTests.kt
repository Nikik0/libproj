package com.nikik0.libproj.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.*
import com.nikik0.libproj.kafka.model.Event
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.admin.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.assertj.core.api.Assertions.*
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.await
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.testcontainers.containers.KafkaContainer
import java.time.Duration
import java.util.Properties


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class IntegrationTests(
    @Autowired
    val webClient: WebTestClient,
    @Autowired
    val client: DatabaseClient
) {
    companion object {
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:13.3"))
            .apply {
                this.withDatabaseName("movies-db").withUsername("test").withPassword("test123")
            }

        private val kafka: KafkaContainer = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0")).withEmbeddedZookeeper()

        private val mapper = jacksonObjectMapper()

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url", Companion::r2dbcUrl)
            registry.add("spring.r2dbc.username", postgres::getUsername)
            registry.add("spring.r2dbc.password", postgres::getPassword)
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
        }
        private fun r2dbcUrl(): String {
            return "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)}/${postgres.databaseName}"
        }

        private fun setupFlyway(){
            val flyway = Flyway.configure()
                .locations("filesystem:src/main/docker/docker-db/flyway/migrations/")
                .baselineOnMigrate(true)
                .schemas("public")
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .load()
            flyway.info()
            flyway.migrate()
        }


        private var consumerKafkaClient: KafkaConsumer<String, String>? = null

        private var adminKafkaClient: AdminClient? = null

        private fun setupKafka(){
            val consumerProps = Properties().apply {
                put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer")
                put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
                put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer::class.java)
                put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer::class.java)
            }
            consumerKafkaClient = KafkaConsumer<String, String>(consumerProps)
            val adminProps = Properties().apply {
                put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            }
            adminKafkaClient = KafkaAdminClient.create(adminProps)

            adminKafkaClient!!.createTopics(listOf(NewTopic("events", 1, 1))).all().get()
        }

        @JvmStatic
        @BeforeAll
        internal fun setUp(){
            postgres.start()
            setupFlyway()
            kafka.start()
            setupKafka()
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
            "additionalInfo": "First additional info",
            "watched": [],
            "favourites":[]
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

    private val customerUnsavedDummyWithMovies: String = """
                {
                    "name": "Third name",
                    "surname": "Third Surname",
                    "country": "Third country",
                    "state": "Third state",
                    "city": "Third city",
                    "district": "Third district",
                    "street": "Third street",
                    "building": 1,
                    "buildingLiteral": "Third literal",
                    "apartmentNumber": 23,
                    "additionalInfo": "Third additional info",
                    "watched": [
                        {
                            "id": 1,
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
                        },
                        {
                            "id": 2,
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
                    ],
                    "favourites":[
                        {
                            "id": 1,
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
                    ]
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
        "customer", "customer_favourite_movies",
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
            addressId = addressFirst.id!!,
            address = addressFirst,
            watched = emptyList(),
            favorites = emptyList()
        ).toDto()
        customerSavedSecondDummy = CustomerEntity(
            id = 2,
            name = "Second name",
            surname = "Second Surname",
            addressId = addressSecond.id!!,
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

    private fun setupKafkaListenerAndTopics() {
        println("topics up are 2 ${adminKafkaClient!!.listTopics().names().get()}")
        consumerKafkaClient!!.subscribe(listOf("events"))
        //todo either poll all the events before each test to cleanup topic or delete and create same empty topic, both aren't really working tho
        println(consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() })
//        val polled = consumerKafkaClient!!.poll(Duration.ofMillis(10)).map { it.value() }
//        consumerKafkaClient!!.subscribe(listOf("events"))
    }

    @BeforeEach
    fun setupBeforeTests(){
        setupTestMovie()
        setupTestCustomer()
        insertTestData()
        //todo kafka is inconsistent in tests, seems to be a testcontainers related bug, need to investigate later
        setupKafkaListenerAndTopics()
    }

    private fun convertToEvent(value: String) =
        mapper.readValue(value, Event::class.java)

    @Test
    fun `save should return correct new movieDto`() {
        val entity = webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        assertThat(entity).isEqualTo(movieSavedSecondDummy)

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(10000)).map { it.value() }
//        println(eventsOccurred)
//        println(consumerKafkaClient!!.poll(Duration.ofMillis(1)).map { it.value() })
//        assertThat(eventsOccurred).hasSize(5)
    }

    @Test
    fun `get single should return correct movieDto`() {
        val retrievedEntity = webClient.get().uri("/api/v1/movie/get/${movieSavedFirstDummy?.id}").exchange().returnResult<MovieDto>()
            .responseBody.blockLast()
        assertThat(retrievedEntity).isEqualTo(movieSavedFirstDummy)

//        println(adminKafkaClient!!.listTopics().names())
//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(0)
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

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(100000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(5)
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

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(3)
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

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(5)
    }

    @Test
    fun `save customer returns correct customer dto`(){
        val customerDto = webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedSecondDummy).exchange().returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto).isEqualTo(customerSavedSecondDummy)

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(1)
//        val event = convertToEvent(eventsOccurred[0])
//        assertThat(event).isEqualTo(customerSavedSecondDummy!!.id)
    }

    @Test
    fun `get customer returns single customerDto`(){
        val customerDto = webClient.get().uri("/api/v1/customer/get/${customerSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto).isEqualTo(customerSavedFirstDummy)

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(0)
    }

    @Test
    fun `get all customers returns correct number of dtos`(){
        println(adminKafkaClient!!.listTopics().names().get())

        webClient.get().uri("/api/v1/customer/get/all").exchange()
            .expectBodyList(CustomerDto::class.java).hasSize(1)
            .returnResult().responseBody
        webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedSecondDummy).exchange().returnResult<CustomerDto>()
        webClient.get().uri("/api/v1/customer/get/all").exchange()
            .expectBodyList(CustomerDto::class.java).hasSize(2)
            .returnResult().responseBody

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(1)
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

//        val eventsOccurred = consumerKafkaClient!!.poll(Duration.ofMillis(1000)).map { it.value() }
//        assertThat(eventsOccurred).hasSize(1)
//        val event = convertToEvent(eventsOccurred[0])
//        assertThat(event.id).isEqualTo(movieSavedFirstDummy!!.id)
    }

    @Test
    fun `add to favs returns dto with correct favourites list`(){
        val customer = webClient.get().uri("/api/v1/customer/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val movie = webClient.get().uri("/api/v1/movie/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<MovieDto>().responseBody.blockLast()
        webClient.post().uri("/api/v1/customer/${customer!!.id}/watched/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val customerUpdated = webClient.post().uri("/api/v1/customer/${customer.id}/favourites/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerUpdated!!.favourites).isNotNull.isNotEmpty.hasSize(1)
        assertThat(customerUpdated.favourites!![0].id).isEqualTo(movie!!.id)
    }

    @Test
    fun `add to favs returns ResponseException if movie is not present in watched`(){
        val customer = webClient.get().uri("/api/v1/customer/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val resultStatus = webClient.post().uri("/api/v1/customer/${customer?.id}/favourites/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<Any>().status

        assertThat(resultStatus).isEqualTo(HttpStatus.NOT_ACCEPTABLE)
    }

    @Test
    fun `add to watched returns ResponseException if movie is already present in watched`(){
        val customer = webClient.get().uri("/api/v1/customer/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        webClient.post().uri("/api/v1/customer/${customer!!.id}/watched/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val resultStatus = webClient.post().uri("/api/v1/customer/${customer!!.id}/watched/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<Any>().status

        assertThat(resultStatus).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `add to favs returns ResponseException if movie is already present in favs`(){
        val customer = webClient.get().uri("/api/v1/customer/get/${movieSavedFirstDummy?.id}").exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        webClient.post().uri("/api/v1/customer/${customer!!.id}/watched/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        webClient.post().uri("/api/v1/customer/${customer!!.id}/favourites/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<CustomerDto>().responseBody.blockLast()
        val resultStatus = webClient.post().uri("/api/v1/customer/${customer!!.id}/favourites/add")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieSavedFirstDummy!!).exchange()
            .returnResult<Any>().status

        assertThat(resultStatus).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `save customer should save new customer with multiple movies in favs and watched`(){
        webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        val customerDto = webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerUnsavedDummyWithMovies).exchange().returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto?.watched).isNotNull.hasSize(2)
        assertThat(customerDto?.favourites).isNotNull.hasSize(1)
    }

    @Test
    fun `save customer should save existing customer with multiple movies in favs and watched`(){
        val movieSecondDto = webClient.post().uri("/api/v1/movie/save").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(movieUnsavedSecondDummy).exchange().returnResult(MovieDto::class.java).responseBody.blockLast()
        val customerToSave = CustomerDto(
            id = customerSavedFirstDummy!!.id,
            name = customerSavedFirstDummy!!.name + " Changed",
            surname = customerSavedFirstDummy!!.surname,
            country = customerSavedFirstDummy!!.country,
            state = customerSavedFirstDummy!!.state,
            city = customerSavedFirstDummy!!.city,
            district = customerSavedFirstDummy!!.district,
            street = customerSavedFirstDummy!!.street,
            building = customerSavedFirstDummy!!.building,
            buildingLiteral = customerSavedFirstDummy!!.buildingLiteral,
            apartmentNumber = customerSavedFirstDummy!!.apartmentNumber,
            additionalInfo = customerSavedFirstDummy!!.additionalInfo,
            watched = listOf(movieSavedFirstDummy!!, movieSecondDto!!),
            favourites = listOf(movieSavedFirstDummy!!)
        )
        val customerDto = webClient.post().uri("/api/v1/customer/save")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
            .bodyValue(customerToSave).exchange().returnResult<CustomerDto>().responseBody.blockLast()
        assertThat(customerDto!!.name).isEqualTo(customerSavedFirstDummy!!.name + " Changed")
        assertThat(customerDto.watched).isNotNull.hasSize(2)
        assertThat(customerDto.favourites).isNotNull.hasSize(1)
    }
}