package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.mappers.mapToAddress
import com.nikik0.libproj.mappers.toDto
import com.nikik0.libproj.repositories.AddressRepository
import com.nikik0.libproj.repositories.CustomerRepository
import com.nikik0.libproj.repositories.MovieRepository
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class CustomerService (
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository,
    private val movieRepository: MovieRepository
        ){

    suspend fun getCustomer(id: Long) = customerRepository.findById(id)?.toDto()

    suspend fun getAllCustomers() = customerRepository.findAll().map { it.toDto() }

    suspend fun saveCustomer(customer: CustomerDto): CustomerDto {
        val address = customer.mapToAddress()
        return customerRepository.findById(customer.id)
            ?.let {
                customerRepository.save(
                    CustomerEntity(
                        id = it.id,
                        name = customer.name,
                        surname = customer.surname,
                        address = addressRepository.save(address),
                        watched = it.watched,
                        favorites = it.favorites
                    )
                ).toDto()
            }
            ?: customerRepository.save(
                CustomerEntity(
                    id = customer.id,
                    name = customer.name,
                    surname = customer.surname,
                    address = addressRepository.save(address),
                    watched = emptyList(),
                    favorites = emptyList()
                )
            ).toDto()
    }

    suspend fun deleteCustomer(customer: CustomerDto) =
        customerRepository.deleteById(customer.id)

    suspend fun addToWatched(customer: CustomerDto, movieDto: MovieDto): CustomerDto? {
        val film = movieRepository.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customer.id)
        return if (customerEntity != null && film != null) {
            customerRepository.save(
                CustomerEntity(
                    id = customerEntity.id,
                    name = customerEntity.name,
                    surname = customerEntity.surname,
                    address = customerEntity.address,
                    watched = customerEntity.watched + film,
                    favorites = customerEntity.favorites
                )
            ).toDto()
        } else {
            null
        }
    }

    suspend fun addToFavourites(customer: CustomerDto, movieDto: MovieDto): CustomerDto? {
        val film1 = movieRepository.findById(movieDto.id)
        val film2 = movieRepository.findById(movieDto.id)


        val film = movieRepository.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customer.id)
        return if (customerEntity != null && film != null) {
            customerRepository.save(
                CustomerEntity(
                    id = customerEntity.id,
                    name = customerEntity.name,
                    surname = customerEntity.surname,
                    address = customerEntity.address,
                    watched = customerEntity.watched,
                    favorites = customerEntity.favorites + film
                )
            ).toDto()
        } else {
            null
        }
    }
}