package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.dtos.mapToAddress
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.entities.toDto
import com.nikik0.libproj.exceptions.AlreadyPresentResponseException
import com.nikik0.libproj.exceptions.MovieNotInWatchedResponseException
import com.nikik0.libproj.exceptions.NotFoundEntityResponseException
import com.nikik0.libproj.repositories.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerServiceImpl(
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository,
    private val manyToManyRepository: ManyToManyRepository,
    private val movieService: MovieService
) : CustomerService {

    override suspend fun getCustomer(id: Long) =
        customerRepository.findById(id)?.apply {
            address = addressRepository.findAddressForCustomerId(this.id).toList().first()
            watched = movieService.findWatchedMoviesForCustomerId(this.id).toList()
            favorites = movieService.findFavMoviesForCustomerId(this.id).toList()
        }?.toDto()
            ?: throw NotFoundEntityResponseException(
                HttpStatus.NOT_FOUND,
                "Customer with id $id wasn't found"
            )

    override suspend fun getAllCustomers() = customerRepository.findAll().map {
        it.address = addressRepository.findAddressForCustomerId(it.id).first()
        it.toDto()
    }

//working
    /*

        @Transactional
    override suspend fun saveCustomer(customerDto: CustomerDto): CustomerDto? {
        val address = addressRepository.save(customerDto.mapToAddress())
        val customerEntity = customerRepository.findById(customerDto.id)?.let {
            customerRepository.save(
                CustomerEntity(
                    id = it.id,
                    name = customerDto.name,
                    surname = customerDto.surname,
                    address = address,
                    watched = it.watched,
                    favorites = it.favorites
                    //todo save same movies to watched should be checked, multiple same movies are trash
                    //todo need to check if this actually saves watched and stuff
                )
            )
        } ?: let {
            customerRepository.save(
                CustomerEntity(
                    id = customerDto.id,
                    name = customerDto.name,
                    surname = customerDto.surname,
                    address = address,
                    watched = emptyList(),
                    favorites = emptyList()
                )
            )
        }

       manyToManyRepository.customerAddressInsert(customerEntity.id, address.id)
        return customerEntity.apply {
            this.address = addressRepository.findAddressForCustomerId(this.id).first()
        }.toDto()
    }
     */


    // todo save with watched and favs is useless, should be reworked
    @Transactional
    override suspend fun saveCustomer(customerDto: CustomerDto): CustomerDto? {
        val address = addressRepository.save(customerDto.mapToAddress())
        val customerEntity = customerRepository.findById(customerDto.id)?.let {
            customerRepository.save(
                CustomerEntity(
                    id = it.id,
                    name = customerDto.name,
                    surname = customerDto.surname,
                    address = address,
                    watched = it.watched,
                    favorites = it.favorites
                )
            )
        } ?: let {
            customerRepository.save(
                CustomerEntity(
                    id = customerDto.id,
                    name = customerDto.name,
                    surname = customerDto.surname,
                    address = address,
                    watched = emptyList(),
                    favorites = emptyList()
                )
            )
        }
        manyToManyRepository.customerAddressInsert(customerEntity.id, address.id) //todo this duplicates address
        customerDto.watched?.forEach { addToWatched(customerEntity.id, it) }
        customerDto.favourites?.forEach { addToFavourites(customerEntity.id, it) }
        return customerEntity.apply {
            this.address = addressRepository.findAddressForCustomerId(this.id).first()
            this.watched = movieService.findWatchedMoviesForCustomerId(this.id).toList()
            this.favorites = movieService.findFavMoviesForCustomerId(this.id).toList()
        }.toDto()
    }

    override suspend fun deleteCustomer(customer: CustomerDto) =
        customerRepository.deleteById(customer.id)

    @Transactional
    override suspend fun addToWatched(customerId: Long, movieDto: MovieDto): CustomerDto {
        val movieEntity = movieService.getOneLazy(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
        return if (customerEntity != null && movieEntity != null) {
            checkAndInsertToWatched(customerEntity.id, movieEntity.id)
        } else {
            throw NotFoundEntityResponseException(
                HttpStatus.NOT_FOUND,
                if (customerEntity == null) "Customer with id: $customerId not found" else "Movie with id: ${movieDto.id} not found"
            )
        }
    }

    private suspend fun checkAndInsertToWatched(customerId: Long, movieId: Long): CustomerDto {
        if (manyToManyRepository.checkIfCustomerWatchedMovie(
                customerId,
                movieId
            )
        ) throw AlreadyPresentResponseException(
            HttpStatus.CONFLICT,
            "Watched movie with id $movieId is already present in watched list for user $customerId"
        )
        manyToManyRepository.customerWatchedMovieInsert(customerId, movieId)
        return getCustomer(customerId)
    }


    @Transactional
    override suspend fun addToFavourites(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val movieEntity = movieService.getOneLazy(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
        return if (customerEntity != null && movieEntity != null) {
            checkAndInsertToFav(customerEntity.id, movieEntity.id)
        } else {
            throw NotFoundEntityResponseException(
                HttpStatus.NOT_FOUND,
                if (customerEntity == null) "Customer with id: $customerId not found" else "Movie with id: ${movieDto.id} not found"
            )
        }
    }

    private suspend fun checkAndInsertToFav(customerId: Long, movieId: Long): CustomerDto {
        if (!manyToManyRepository.checkIfCustomerWatchedMovie(
                customerId,
                movieId
            )
        ) throw MovieNotInWatchedResponseException(
            HttpStatus.NOT_ACCEPTABLE,
            "Movie with id $movieId wasn't added to watched, unable to add it to favourites"
        )
        if (manyToManyRepository.checkIfCustomerFavMovie(
                customerId,
                movieId
            )
        ) throw AlreadyPresentResponseException( //todo might want to ignore these if saving already present customer with watched list of old and new movies
            HttpStatus.CONFLICT,
            "Favourite movie with id $movieId is already present in favourites list for user $customerId"
        )
        manyToManyRepository.customerFavouriteMovieInsert(customerId, movieId)
        return getCustomer(customerId)
    }
}