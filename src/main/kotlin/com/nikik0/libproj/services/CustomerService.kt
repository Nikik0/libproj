package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.mappers.mapToAddress
import com.nikik0.libproj.mappers.mapToCustomerEntityAndAddress
import com.nikik0.libproj.mappers.toDto
import com.nikik0.libproj.repositories.AddressRepository
import com.nikik0.libproj.repositories.CustomerRepository
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Service

@Service
class CustomerService (
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository
        ){
    suspend fun getCustomer(id: Long) = customerRepository.findById(id)?.toDto()

    suspend fun getAllCustomers() = customerRepository.findAll().map { it.toDto() }

    suspend fun saveCustomer(customer: CustomerDto) : CustomerDto {
        val address = customer.mapToAddress()
        val customerToSave = CustomerEntity(
            id = customer.id,
            name = customer.name,
            surname = customer.surname,
            address = addressRepository.save(address)
        )
        return customerRepository.save(customerToSave).toDto()
    }

    suspend fun deleteCustomer(customer: CustomerDto) =
        customerRepository.deleteById(customer.id)
}