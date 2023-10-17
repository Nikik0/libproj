package com.nikik0.libproj.services

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.entities.CustomerEntityUpd
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.mappers.mapToAddress
import com.nikik0.libproj.mappers.toDto
import com.nikik0.libproj.repositories.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import java.util.stream.Collectors

@Service
class CustomerService (
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository,
    private val movieRepository: MovieRepository,
    private val customerRepositoryUpd: CustomerRepositoryUpd,
    private val customAddressRepository: CustomAddressRepository,
    private val manyToManyRepository: ManyToManyRepository
        ){

    suspend fun test() =
        manyToManyRepository.customerFavouriteMovieInsert(2, listOf(1, 2))

    suspend fun getTestCustomer(): CustomerEntityUpd? {
        val customer = customerRepositoryUpd.findById(1)
        val address1 = customer?.let { addressRepository.findById(it.addressId) }
        customer?.address = address1

        val custom = customAddressRepository.findForCustomerId(1)
        println("from custom $custom")



        val bruh = addressRepository.findAddressForCustomerId(2)
        //bruh.onEach { println(it) }
        val list = bruh.toList().first()
        println("list taken from flow $list")
        println(bruh.javaClass)


        val smth = customerRepositoryUpd.findById(1)?.let {
            it.address = addressRepository.findById(it.addressId)
            it
         }

        val wtf = customerRepositoryUpd.findById(2)?.apply { address = addressRepository.findById(addressId) }
        return wtf
    }//?.let { it -> it.address = addressRepository.findById(it.addressId) }.let { println(it) }

    suspend fun getCustomer(id: Long) = customerRepository.findById(id)?.apply {
        this.address = addressRepository.findAddressForCustomerId(this.id).toList().first()
    }?.toDto()

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
        return customerEntity.toDto()
//        val entity = CustomerEntity(
//            id = customerDto.id,
//            name = customerDto.name,
//            surname = customerDto.surname,
//            address = addressRepository.save(address),
//            watched = emptyList(),
//            favorites = emptyList()
//        )
//        return customerRepository.save(entity).toDto()
    }

//    suspend fun saveCustomer(customer: CustomerDto): CustomerDto {
//        val address = customer.mapToAddress()
//        return customerRepository.findById(customer.id)
//            ?.let {
//                customerRepository.save(
//                    CustomerEntity(
//                        id = it.id,
//                        name = customer.name,
//                        surname = customer.surname,
//                        address = addressRepository.save(address),
//                        watched = it.watched,
//                        favorites = it.favorites
//                    )
//                ).toDto()
//            }
//            ?: customerRepository.save(
//                CustomerEntity(
//                    id = customer.id,
//                    name = customer.name,
//                    surname = customer.surname,
//                    address = addressRepository.save(address),
//                    watched = emptyList(),
//                    favorites = emptyList()
//                )
//            ).toDto()
//    }

    suspend fun deleteCustomer(customer: CustomerDto) =
        customerRepository.deleteById(customer.id)

    suspend fun addToWatched(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val film = movieRepository.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
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

    suspend fun addToFavourites(customerId: Long, movieDto: MovieDto): CustomerDto? {
        val film1 = movieRepository.findById(movieDto.id)
        val film2 = movieRepository.findById(movieDto.id)
        val film = movieRepository.findById(movieDto.id)
        val customerEntity = customerRepository.findById(customerId)
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