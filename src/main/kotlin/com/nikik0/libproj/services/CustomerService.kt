package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.entities.MovieEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.transaction.annotation.Transactional

interface CustomerService {
    suspend fun getCustomer(id: Long): CustomerDto?

    suspend fun getAllCustomers(): Flow<CustomerDto>

    suspend fun saveCustomer(customerDto: CustomerDto): CustomerDto?

    suspend fun deleteCustomer(customer: CustomerDto)

    suspend fun addToWatched(customerId: Long, movieDto: MovieDto): CustomerDto?

    suspend fun addToFavourites(customerId: Long, movieDto: MovieDto): CustomerDto?

}