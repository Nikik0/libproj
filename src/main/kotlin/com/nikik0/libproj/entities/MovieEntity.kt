package com.nikik0.libproj.entities

import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("movie")
data class MovieEntity(
    val id: Long,
    val name: String,
    val producer: String,
    @Transient
    val actors: List<Actor>,
    @Transient
    val tags: List<MovieTag>,
    @Transient
    val studio: MovieStudio,
    val budget: Long,
    @Column("movie_url")
    val movieUrl: String
)
