package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.services.CustomerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/customer")
class CustomerController (
    private val customerService: CustomerService
        ){
    @GetMapping("/get/{id}")
    suspend fun getCustomer(@PathVariable id: Long) = customerService.getCustomer(id)

    @PostMapping()
    suspend fun saveCustomer(customerDto: CustomerDto) = customerService.saveCustomer(customerDto)

    suspend fun deleteCustomer(customerDto: CustomerDto) = customerService.deleteCustomer(customerDto)

    suspend fun getAllCustomers() = customerService.getAllCustomers()

}