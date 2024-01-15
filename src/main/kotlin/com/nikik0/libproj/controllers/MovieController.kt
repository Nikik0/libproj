package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.services.MovieService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/movie")
class MovieController (
    private val movieService: MovieService
        ){
    @Operation(summary = "Get movie by id with tags, actors and studio (yeager load)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Movie was retrieved successfully", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = MovieDto::class))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Movie wasn't found", content = [
                    Content()
                ]
            )
        ]
    )
    @GetMapping("/get/{id}")
    suspend fun getSingle(@PathVariable id: Long) = movieService.getOneYeager(id)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.NOT_FOUND)

    @Operation(summary = "Get all movies with tags, actors and studios (yeager load)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "List of movies was retrieved successfully", content = [
                    Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = MovieDto::class)))
                ]
            )
        ]
    )
    @GetMapping("/get/all/yeager")
    suspend fun getAll() = movieService.getAllYeager().let { ResponseEntity.ok(it) }

    @Operation(summary = "Get all movies without additional info (lazy load)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "List of movies was retrieved successfully", content = [
                    Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = MovieDto::class)))
                ]
            )
        ]
    )
    @GetMapping("/get/all/lazy")
    suspend fun getAllLazy() = movieService.getAllLazy().let { ResponseEntity.ok(it) }

    @Operation(summary = "Save new movie with tags, actors and studio")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Movie saved successfully", content = [
                    Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = MovieDto::class)))
                ]
            )
        ]
    )
    @PostMapping("/save")
    suspend fun saveOne(@RequestBody movieDto: MovieDto) = movieService.saveOne(movieDto).let { ResponseEntity.ok(it) }

    @Operation(summary = "Find all movies by tag (case insensitive)")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved list of movies with corresponding tag", content = [
                    Content(mediaType = "application/json", array = ArraySchema(schema = Schema(implementation = MovieDto::class)))
                ]
            ),
            ApiResponse(
                responseCode = "404", description = "Tag wasn't found", content = [
                    Content()
                ]
            )
        ]
    )
    @GetMapping("/find/tag/{tag}")
    suspend fun findByTag(@PathVariable tag: String) = movieService.findByTag(tag)?.let { ResponseEntity.ok(it) } ?: ResponseEntity(HttpStatus.NOT_FOUND)

}