package com.nikik0.libproj.mappers

import com.nikik0.libproj.dtos.FilmDto
import com.nikik0.libproj.entities.FilmEntity
import org.springframework.stereotype.Component

fun FilmDto.mapToEntity() =
    FilmEntity(
        id = this.id,
        name = this.name,
        producer = this.producer,
        actors = this.actors,
        budget = this.budget,
        filmUrl = this.filmUrl
    )

fun FilmEntity.mapToDto() =
    FilmDto(
        id = this.id,
        name = this.name,
        producer = this.producer,
        actors = this.actors,
        budget = this.budget,
        filmUrl = this.filmUrl
    )

@Component
class FilmMapper {
}