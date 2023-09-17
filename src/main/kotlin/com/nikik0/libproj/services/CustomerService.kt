package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.FilmDto
import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.mappers.mapToAddress
import com.nikik0.libproj.mappers.mapToCustomerEntityAndAddress
import com.nikik0.libproj.mappers.toDto
import com.nikik0.libproj.repositories.AddressRepository
import com.nikik0.libproj.repositories.CustomerRepository
import com.nikik0.libproj.repositories.FilmRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.springframework.stereotype.Service
import kotlin.math.log

@Service
class CustomerService (
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository,
    private val filmRepository: FilmRepository
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

    suspend fun addToWatched(customer: CustomerDto, filmDto: FilmDto): CustomerDto? {
        val film = filmRepository.findById(filmDto.id)
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

    suspend fun addToFavourites(customer: CustomerDto, filmDto: FilmDto): CustomerDto? {
        val film1 = filmRepository.findById(filmDto.id)
        val film2 = filmRepository.findById(filmDto.id)


        val film = filmRepository.findById(filmDto.id)
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