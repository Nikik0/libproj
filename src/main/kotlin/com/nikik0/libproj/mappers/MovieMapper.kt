package com.nikik0.libproj.mappers

import com.nikik0.libproj.dtos.MovieDto
import com.nikik0.libproj.entities.MovieEntity
import org.springframework.stereotype.Component

fun MovieDto.mapToEntity() =
    MovieEntity(
        id = this.id,
        name = this.name,
        producer = this.producer,
        actors = this.actors,
        budget = this.budget,
        movieUrl = this.movieUrl
    )

fun MovieEntity.mapToDto() =
    MovieDto(
        id = this.id,
        name = this.name,
        producer = this.producer,
        actors = this.actors,
        budget = this.budget,
        movieUrl = this.movieUrl
    )

@Component
class FilmMapper {
}