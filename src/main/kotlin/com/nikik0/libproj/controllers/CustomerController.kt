package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.entities.CustomerEntityUpd
import com.nikik0.libproj.repositories.AddressRepository
import com.nikik0.libproj.repositories.CustomerRepository
import com.nikik0.libproj.services.CustomerService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/customer")
class CustomerController (
    private val customerService: CustomerService,
    private val customerRepository: CustomerRepository,
    private val addressRepository: AddressRepository
        ){
    @GetMapping("/get/{id}")
    suspend fun getCustomer(@PathVariable id: Long) = customerService.getCustomer(id)

//    @PostMapping()
//    suspend fun saveCustomer(customerDto: CustomerDto) = customerService.saveCustomer(customerDto)

    @DeleteMapping()
    suspend fun deleteCustomer(customerDto: CustomerDto) = customerService.deleteCustomer(customerDto)

    @GetMapping("/get/all")
    suspend fun getAllCustomers() = customerService.getAllCustomers()

    @PostMapping("/add/watched")
    suspend fun addToWatchedList() = null

    @GetMapping("/test")
    suspend fun test() = customerService.test()
        //CustomerDto(1,"s","a","a","b","a","a","q",1,"a",1, "a")
    @PostMapping("/ugh")
    suspend fun testSaving(@RequestBody customerDto: CustomerDto): CustomerEntity {
            println(customerDto)
            return customerService.saveNewCustomerTest(customerDto)
//            return CustomerDto(
//                 2,
//             "Dad",
//             "Dew",
//             "dasd",
//             "eew",
//             "ee",
//             "rr",
//             "qwe",
//             1,
//             "asd",
//             23,
//             "asd"
//            )
        }

}