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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService (
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository,
    //private val movieRepository: MovieRepository,
    private val manyToManyRepository: ManyToManyRepository,
    private val movieService: MovieService
        ){


    suspend fun getCustomer(id: Long) = customerRepository.findById(id)?.apply {
        this.address = addressRepository.findAddressForCustomerId(this.id).toList().first()
    }?.toDtoYeager()

    suspend fun getAllCustomers() = customerRepository.findAll().map { it.toDto() }

    suspend fun saveNewCustomerTest(customer: CustomerDto): CustomerEntity {
        val address = addressRepository.save(customer.mapToAddress())
        val savedCustomer = customerRepository.findById(customer.id)  //todo might want to check if customer exists or id is null, otherwise throw exception
            ?.let {
                customerRepository.save(
                    CustomerEntity(
                        id = it.id,
                        name = it.name,
                        surname = it.surname
                    )
                )
            }
            ?: customerRepository.save(
                CustomerEntity(
                    id = customer.id,
                    name = customer.name,
                    surname = customer.surname
                )
            )
        savedCustomer.address = address
        manyToManyRepository.customerAddressInsert(savedCustomer.id, address.id)
        return savedCustomer
    }

    @Transactional
    suspend fun saveCustomer(customerDto: CustomerDto): CustomerDto? {
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
        } ?:
        let {
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

    suspend fun deleteCustomer(customer: CustomerDto) =
        customerRepository.deleteById(customer.id)

    @Transactional
    suspend fun addToWatched(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val movieEntity = movieService.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
        println("from service film found $movieEntity for customer $customerEntity")
        return if (customerEntity != null && movieEntity != null) {
            manyToManyRepository.customerWatchedMovieInsert(customerEntity.id, movieEntity.id)
            customerRepository.save(
                CustomerEntity(
                    id = customerEntity.id,
                    name = customerEntity.name,
                    surname = customerEntity.surname,
                    address = customerEntity.address,
                    watched = customerEntity.watched + movieEntity,
                    favorites = customerEntity.favorites
                )
            ).apply {
                this.address = addressRepository.findAddressForCustomerId(this.id).first()
            }.toDtoYeager()
        } else {
            null
        }
    }

    @Transactional
    suspend fun addToFavourites(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val movieEntity = movieService.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
        return if (customerEntity != null && movieEntity != null) {
            manyToManyRepository.customerFavouriteMovieInsert(customerEntity.id, movieEntity.id)
            customerRepository.save(
                CustomerEntity(
                    id = customerEntity.id,
                    name = customerEntity.name,
                    surname = customerEntity.surname,
                    address = customerEntity.address,
                    watched = customerEntity.watched,
                    favorites = customerEntity.favorites + movieEntity
                )
            ).apply {
                this.address = addressRepository.findAddressForCustomerId(this.id).first()
            }.toDtoYeager()
        } else {
            null
        }
    }


    suspend fun test() =
        movieService.findWatchedMoviesForCustomerId(1)

}