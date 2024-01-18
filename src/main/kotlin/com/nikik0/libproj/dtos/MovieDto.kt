package com.nikik0.libproj.dtos

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

fun MovieDto.mapToEntity() =
    MovieEntity(
        id = this.id,
        name = this.name,
        producer = this.producer,
        actors = this.actors,
        tags = this.tags,
        studio = this.studio,
        budget = this.budget,
        movieUrl = this.movieUrl
    )

data class MovieDto(
    val id: Long,
    @field:NotBlank(message = "{validation.movie.field.name.blank}")
    val name: String,
    @field:NotBlank(message = "{validation.movie.field.producer.blank}")
    val producer: String,
    @field:NotNull(message = "{validation.movie.field.actors.null}")
    val actors: List<Actor>,
    @field:NotNull(message = "{validation.movie.field.tags.null}")
    val tags: List<MovieTag>,
    val studio: MovieStudio?,
    val budget: Long,
    @field:NotBlank(message = "{validation.movie.field.url.blank}")
    val movieUrl: String
)
