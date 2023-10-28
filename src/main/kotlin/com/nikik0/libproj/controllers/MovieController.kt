package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.services.MovieService
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
    @GetMapping("/get/{id}")
    suspend fun getSingle(@PathVariable id: Long) = movieService.getOne(id)?.let { ResponseEntity.ok(it) } ?: HttpStatus.NOT_FOUND

    @GetMapping("/get/all/yeager")
    suspend fun getAll() = movieService.getAllYeager()

    @GetMapping("/get/all/lazy")
    suspend fun getAllLazy() = movieService.getAllLazy()

    @PostMapping()
    suspend fun saveOne(@RequestBody movieDto: MovieDto) = movieService.saveOne(movieDto)

}