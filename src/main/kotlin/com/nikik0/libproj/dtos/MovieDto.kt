package com.nikik0.libproj.dtos

import com.nikik0.libproj.entities.Actor
import com.nikik0.libproj.entities.MovieStudio
import com.nikik0.libproj.entities.MovieTag

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
