package com.nikik0.libproj.controllers

import com.nikik0.libproj.dtos.FilmDto
import com.nikik0.libproj.services.FilmService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("api/v1/film")
class FilmController (
    private val filmService: FilmService
        ){
    @GetMapping("/get/{id}")
    suspend fun getSingle(@PathVariable id: Long) = filmService.getSingle(id)

    @GetMapping("/get/all")
    suspend fun getAll() = filmService.getAll()

    @PostMapping()
    suspend fun save(@RequestBody filmDto: FilmDto) = filmService.save(filmDto)
}