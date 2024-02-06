package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.services.CustomerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import jakarta.validation.Valid
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    suspend fun getCustomer(@Parameter(description = "Id of the customer to be searched") @PathVariable id: Long) = withContext(MDCContext()){
            customerService.getCustomer(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.NOT_FOUND)
        }

    @Operation(summary = "save customer, could be saved with fav and watched movies in dto")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Customer saved successfully", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = CustomerDto::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Customer or movie wasn't found", content = [
                    Content()
                ]
            )
        ]
    )
    @PostMapping("/save")
    suspend fun saveCustomer(@Valid @RequestBody customerDto: CustomerDto) = withContext(MDCContext()){
            customerService.saveCustomer(customerDto)?.let { ResponseEntity.ok(it) }
        } //?: ResponseEntity(HttpStatus.BAD_REQUEST)

    @Operation(summary = "Delete customer")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Customer deleted successfully", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = CustomerDto::class))
                ]
            )
        ]
    )
    @DeleteMapping()
    suspend fun deleteCustomer(@RequestBody customerDto: CustomerDto) = withContext(MDCContext()){
        customerService.deleteCustomer(customerDto).let { ResponseEntity.ok(HttpStatus.OK) }
    }

    @Operation(summary = "Get all customers without favourite and watched movies for each (lazy load)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved a list of customer", content = [
                    Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = CustomerDto::class)))
                ]
            )
        ]
    )
    @GetMapping("/get/all")
    suspend fun getAllCustomers() = withContext(MDCContext()){
        customerService.getAllCustomers().let { ResponseEntity.ok(it) }
    }

    @Operation(summary = "Add a movie to customer's watched list")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Movie was added to watched", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = CustomerDto::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Movie or customer wasn't found", content = [
                    Content()
                ]
            ),
            ApiResponse(
                responseCode = "409", description = "Movie is already present in watched for this customer", content = [
                    Content()
                ]
            )
        ]
    )
    @PostMapping("/{id}/watched/add")
    suspend fun addToWatchedList(@PathVariable id: Long, @RequestBody movieDto: MovieDto) = withContext(MDCContext()){
            customerService.addToWatched(id, movieDto)?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    @Operation(summary = "Add a movie to customer's favourites list")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Movie was added to favourites", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = CustomerDto::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Movie or customer wasn't found", content = [
                    Content()
                ]
            ),
            ApiResponse(
                responseCode = "406", description = "Movie can't be added to favourites if it isn't present in watched", content = [
                    Content()
                ]
            ),
            ApiResponse(
                responseCode = "409", description = "Movie is already present in favourites for this customer", content = [
                    Content()
                ]
            )
        ]
    )
    @PostMapping("/{id}/favourites/add")
    suspend fun addToFavList(@PathVariable id: Long, @RequestBody movieDto: MovieDto) = withContext(MDCContext()){
            customerService.addToFavourites(id, movieDto)?.let { ResponseEntity.ok(it) }
                ?: ResponseEntity(HttpStatus.BAD_REQUEST)
        }

}