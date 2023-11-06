package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.CustomerDto
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
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus


@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class IntegrationTests(
    val client: TestRestTemplate,
    val jdbc: JdbcTemplate
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
            .withDatabaseName("db")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("sql/schema.sql")

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
        val entity = client.getForEntity<CustomerDto>("/ugh")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body?.name).contains("Dad")
    }
}