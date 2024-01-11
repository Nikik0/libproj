package com.nikik0.libproj.service

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.entities.*
import com.nikik0.libproj.exceptions.AlreadyPresentResponseException
import com.nikik0.libproj.exceptions.MovieNotInWatchedResponseException
import com.nikik0.libproj.repositories.AddressRepository
import com.nikik0.libproj.repositories.CustomerRepository
import com.nikik0.libproj.repositories.ManyToManyRepository
import com.nikik0.libproj.services.CustomerServiceImpl
import com.nikik0.libproj.services.MovieService
import com.nikik0.libproj.services.MovieServiceImpl
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CustomerServiceTest {
    @MockK
    lateinit var addressRepository: AddressRepository

    @MockK
    lateinit var customerRepository: CustomerRepository

    @MockK
    lateinit var manyToManyRepository: ManyToManyRepository

    @MockK
    lateinit var movieService: MovieService

    @InjectMockKs
    lateinit var customerService: CustomerServiceImpl

    private lateinit var address1: AddressEntity

    private lateinit var address2: AddressEntity

    private lateinit var customerEntity1: CustomerEntity

    private lateinit var customerEntity2: CustomerEntity

    private lateinit var customerDto1: CustomerDto

    private lateinit var customerDto2: CustomerDto

    private lateinit var movieEntity1: MovieEntity

    private lateinit var movieEntity2: MovieEntity

    @BeforeEach
    fun setup() {
        setupTestEntities()
    }

    private fun setupTestEntities() {
        address1 = AddressEntity(
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
        address2 = AddressEntity(
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

        customerEntity1 = CustomerEntity(
            id = 1,
            name = "First name",
            surname = "First Surname",
            address = address1,
            watched = emptyList(),
            favorites = emptyList()
        )
        customerEntity2 = CustomerEntity(
            id = 2,
            name = "Second name",
            surname = "Second Surname",
            address = address2,
            watched = emptyList(),
            favorites = emptyList()
        )
        customerDto1 = customerEntity1.toDto()
        customerDto2 = customerEntity2.toDto()
        movieEntity1 = MovieEntity(
            id = 1L,
            name = "First Test Movie",
            producer = "First Test Producer",
            actors = emptyList(),
            tags = emptyList(),
            studio = null,
            budget = 2000000L,
            movieUrl = "someurl.com/url"
        )
        movieEntity2 = MovieEntity(
            id = 2L,
            name = "Second Test Movie",
            producer = "Second Test Producer",
            actors = emptyList(),
            tags = emptyList(),
            studio = null,
            budget = 2000000L,
            movieUrl = "someurl.com/url2"
        )

    }

    @Test
    fun `getCustomer return correct dto for the customer`() = runTest {
        // given
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { addressRepository.save(address2) } returns address2
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1.apply {
            watched = listOf(movieEntity1, movieEntity2)
            favorites = listOf(movieEntity1)
        }
        coEvery { customerRepository.findById(customerEntity2.id) } returns customerEntity2
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity2.id, address2.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { addressRepository.findAddressForCustomerId(customerEntity2.id) } returns flowOf(address2)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1, movieEntity2)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity2.id) } returns flowOf()
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity2.id) } returns flowOf()

        // when
        val result1 = customerService.getCustomer(customerDto1.id)
        val result2 = customerService.getCustomer(customerDto2.id)

        // then
        assertEquals(customerEntity1.apply {
            watched = listOf(movieEntity1, movieEntity2)
            favorites = listOf(movieEntity1)
        }.toDto(), result1)
        assertEquals(customerDto2, result2)
    }

    @Test
    fun `getAll returns flow with correct dtos`() = runTest {
        // given
        coEvery { customerRepository.findAll() } returns flowOf(customerEntity1, customerEntity2)
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { addressRepository.findAddressForCustomerId(customerEntity2.id) } returns flowOf(address2)

        // when
        val result = customerService.getAllCustomers().toList()

        // then
        assertEquals(listOf(customerDto1, customerDto2), result)
    }

    @Test
    fun `saveCustomer returns correct dto of the saved customer`() = runTest {
        // given
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { addressRepository.save(address2) } returns address2
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1.apply {
            watched = listOf(movieEntity1, movieEntity2)
            favorites = listOf(movieEntity1)
        }
        coEvery { customerRepository.findById(customerEntity2.id) } returns null
        coEvery { customerRepository.save(customerEntity1) } returns customerEntity1
        coEvery { customerRepository.save(customerEntity2) } returns customerEntity2
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity2.id, address2.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { addressRepository.findAddressForCustomerId(customerEntity2.id) } returns flowOf(address2)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns emptyFlow()
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity2.id) } returns emptyFlow()
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns emptyFlow()
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity2.id) } returns emptyFlow()

        // when
        val result1 = customerService.saveCustomer(customerDto1)
        val result2 = customerService.saveCustomer(customerDto2)

        // then
        assertEquals(customerDto1, result1)
        assertEquals(customerDto2, result2)
    }

    //todo learn what to test in unit returning methods
    @Test
    fun deleteCustomer() = runTest {

    }

    @Test
    fun `addToWatched returns customer dto with movie added to watched`() = runTest {
        // given
        coEvery { movieService.getOneLazy(movieEntity1.id) } returns movieEntity1
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1
        coEvery { manyToManyRepository.customerWatchedMovieInsert(customerEntity1.id, movieEntity1.id) } returns Unit
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns flowOf()
        coEvery { manyToManyRepository.checkIfCustomerWatchedMovie(customerEntity1.id, movieEntity1.id) } returns false

        // when
        val result = customerService.addToWatched(customerEntity1.id, movieEntity1.mapToDto())

        // then
        assertEquals(customerEntity1.apply {
            watched = listOf(movieEntity1)
            favorites = listOf()
        }.toDto(), result)
    }

    @Test
    fun `addToWatched throws AlreadyPresentResponseException when movie is already present in watched`() = runTest {
        // given
        coEvery { movieService.getOneLazy(movieEntity1.id) } returns movieEntity1
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1.apply {
            watched = listOf(movieEntity1)
        }
        coEvery { manyToManyRepository.customerFavouriteMovieInsert(customerEntity1.id, movieEntity1.id) } returns Unit
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { manyToManyRepository.checkIfCustomerWatchedMovie(customerEntity1.id, movieEntity1.id) } returns true

        // then
        assertThrows<AlreadyPresentResponseException> { customerService.addToWatched(customerEntity1.id, movieEntity1.mapToDto()) }

    }

    @Test
    fun `addToFavourites returns correct customer dto with movie in favs if the movie was in watched`() = runTest {
        // given
        coEvery { movieService.getOneLazy(movieEntity1.id) } returns movieEntity1
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1.apply {
            watched = listOf(movieEntity1)
        }
        coEvery { manyToManyRepository.customerFavouriteMovieInsert(customerEntity1.id, movieEntity1.id) } returns Unit
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { manyToManyRepository.checkIfCustomerWatchedMovie(customerEntity1.id, movieEntity1.id) } returns true
        coEvery { manyToManyRepository.checkIfCustomerFavMovie(customerEntity1.id, movieEntity1.id) } returns false

        // when
        val result = customerService.addToFavourites(customerEntity1.id, movieEntity1.mapToDto())

        // then
        assertEquals(customerEntity1.apply {
            watched = listOf(movieEntity1)
            favorites = listOf(movieEntity1)
        }.toDto(), result)
    }

    //todo tests for adding multiple similar movies to watched and fav lists

    @Test
    fun `addToFavourites throws AlreadyPresentResponseException when movie is already present in favs`() = runTest {
        // given
        coEvery { movieService.getOneLazy(movieEntity1.id) } returns movieEntity1
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1.apply {
            watched = listOf(movieEntity1)
        }
        coEvery { manyToManyRepository.customerFavouriteMovieInsert(customerEntity1.id, movieEntity1.id) } returns Unit
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns flowOf(movieEntity1)
        coEvery { manyToManyRepository.checkIfCustomerWatchedMovie(customerEntity1.id, movieEntity1.id) } returns true
        coEvery { manyToManyRepository.checkIfCustomerFavMovie(customerEntity1.id, movieEntity1.id) } returns true

        // then
        assertThrows<AlreadyPresentResponseException> { customerService.addToFavourites(customerEntity1.id, movieEntity1.mapToDto()) }

    }

    @Test
    fun `addToFavourites throws MovieNotInWatchedResponseException with movie in favs if the movie was not in watched`() = runTest {
        // given
        coEvery { movieService.getOneLazy(movieEntity1.id) } returns movieEntity1
        coEvery { customerRepository.findById(customerEntity1.id) } returns customerEntity1.apply {
            watched = listOf(movieEntity1)
        }
        coEvery { manyToManyRepository.customerFavouriteMovieInsert(customerEntity1.id, movieEntity1.id) } returns Unit
        coEvery { addressRepository.save(address1) } returns address1
        coEvery { manyToManyRepository.customerAddressInsert(customerEntity1.id, address1.id) } returns Unit
        coEvery { addressRepository.findAddressForCustomerId(customerEntity1.id) } returns flowOf(address1)
        coEvery { movieService.findWatchedMoviesForCustomerId(customerEntity1.id) } returns flowOf()
        coEvery { movieService.findFavMoviesForCustomerId(customerEntity1.id) } returns flowOf()
        coEvery { manyToManyRepository.checkIfCustomerWatchedMovie(customerEntity1.id, movieEntity1.id) } returns false

        // then
        assertThrows<MovieNotInWatchedResponseException> { customerService.addToFavourites(customerEntity1.id, movieEntity1.mapToDto()) }

    }
}