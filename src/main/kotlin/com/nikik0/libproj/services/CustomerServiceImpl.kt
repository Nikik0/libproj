package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.dtos.mapToAddress
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.entities.toDto
import com.nikik0.libproj.entities.toDtoYeager
import com.nikik0.libproj.repositories.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException
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
        }?.toDtoYeager()
    //todo dtoYeager is useless and should be removed

    override suspend fun getAllCustomers() = customerRepository.findAll().map { it.toDto() }


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

    override suspend fun deleteCustomer(customer: CustomerDto) =
        customerRepository.deleteById(customer.id)

    //todo shouldn't be able to add to fav if movie not in watched
    @Transactional
    override suspend fun addToWatched(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val movieEntity = movieService.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
        return if (customerEntity != null && movieEntity != null) {
            manyToManyRepository.customerWatchedMovieInsert(customerEntity.id, movieEntity.id)
            // todo unsure if the transaction works well with internal method
            this.getCustomer(customerEntity.id)
        } else {
            null //throw NotFoundEntityException()
        }
    }

    @Transactional
    override suspend fun addToFavourites(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val movieEntity = movieService.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
        return if (customerEntity != null && movieEntity != null) {
            manyToManyRepository.customerFavouriteMovieInsert(customerEntity.id, movieEntity.id)
            this.getCustomer(customerEntity.id)
        } else {
            null //throw NotFoundEntityException()
        }
    }

}