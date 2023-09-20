package com.nikik0.libproj.entities

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("movie")
data class MovieEntity(
    val id: Long,
    val name: String,
    val producer: String,
    val actors: List<String>,
    val tags: List<MovieTag>,
    val studio: MovieStudio,
    val budget: Long,
    @Column("movie_url")
    val movieUrl: String
)
