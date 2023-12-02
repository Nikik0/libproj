package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.repositories.AddressRepository
import com.nikik0.libproj.repositories.CustomerRepository
import com.nikik0.libproj.services.CustomerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private val customerService: CustomerService
        ){
    @GetMapping("/get/{id}")
    suspend fun getCustomer(@PathVariable id: Long) = customerService.getCustomer(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.NOT_FOUND)


    @PostMapping("/save")
    suspend fun saveCustomer(@RequestBody customerDto: CustomerDto) =
        customerService.saveCustomer(customerDto)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.BAD_REQUEST)

    @DeleteMapping()
    suspend fun deleteCustomer(customerDto: CustomerDto) = customerService.deleteCustomer(customerDto).let { ResponseEntity.ok(HttpStatus.OK) }

    @GetMapping("/get/all")
    suspend fun getAllCustomers() = customerService.getAllCustomers().let { ResponseEntity.ok(it) }

    @PostMapping("/{id}/watched/add")
    suspend fun addToWatchedList(@PathVariable id: Long, @RequestBody movieDto: MovieDto) =
        customerService.addToWatched(id, movieDto)?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)

    //todo refactor customer to include yeager init for dto that includes watched and fav lists
    //todo add complicated saving for watched and favs in db


    @PostMapping("/{id}/favourites/add")
    suspend fun addToFavList(@PathVariable id: Long, @RequestBody movieDto: MovieDto): ResponseEntity<CustomerDto> {
        val tt = customerService.addToFavourites(id, movieDto)
        println(tt)
        return tt?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity(HttpStatus.BAD_REQUEST)
    }

//    @PostMapping("/{id}/favourites/add")
//    suspend fun addToFavList(@PathVariable id: Long, @RequestBody movieDto: MovieDto) =
//        customerService.addToFavourites(id, movieDto)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.BAD_REQUEST)


    @PostMapping("/ugh")
    suspend fun testSaving(@RequestBody customerDto: CustomerDto): CustomerDto {
            println(customerDto)
//            return customerService.saveNewCustomerTest(customerDto)
            return CustomerDto(
                 2,
             "Dad",
             "Dew",
             "dasd",
             "eew",
             "ee",
             "rr",
             "qwe",
             1,
             "asd",
             23,
             "asd",
                emptyList(),
                emptyList()
            )
        }
    @GetMapping("/testget")
    suspend fun testGet(): CustomerDto {
//            return customerService.saveNewCustomerTest(customerDto)
        return CustomerDto(
            2,
            "Dad",
            "Dew",
            "dasd",
            "eew",
            "ee",
            "rr",
            "qwe",
            1,
            "asd",
            23,
            "asd",
            emptyList(),
            emptyList()
        )
    }
}