package com.nikik0.libproj.dtos

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieEntity
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag

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
    val name: String,
    val producer: String,
    val actors: List<Actor>,
    val tags: List<MovieTag>,
    val studio: MovieStudio?,
    val budget: Long,
    val movieUrl: String
)
