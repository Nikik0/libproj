package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.services.MovieService
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
    suspend fun getSingle(@PathVariable id: Long) = movieService.getOne(id)

    @GetMapping("/get/all")
    suspend fun getAll() = movieService.getAll()

    @PostMapping()
    suspend fun saveOne(@RequestBody movieDto: MovieDto) = movieService.saveOne(movieDto)

//    @PostMapping()
    suspend fun save(@RequestBody movieDto: MovieDto) = movieService.save(movieDto)
}