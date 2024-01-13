package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.services.CustomerService
import com.nikik0.libproj.services.CustomerServiceImpl
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import kotlinx.coroutines.flow.Flow
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
    @Operation(summary = "Get single customer by its id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Found corresponding customer", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = CustomerDto::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Corresponding customer not found", content = [
                    Content()
                ]
            )
        ]
    )
    @GetMapping("/get/{id}")
    suspend fun getCustomer(@Parameter(description = "Id of the customer to be searched") @PathVariable id: Long) =
        customerService.getCustomer(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.NOT_FOUND)

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

    @PostMapping("/{id}/favourites/add")
    suspend fun addToFavList(@PathVariable id: Long, @RequestBody movieDto: MovieDto) =
        customerService.addToFavourites(id, movieDto)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity(HttpStatus.BAD_REQUEST)

}