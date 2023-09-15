package com.nikik0.libproj.entities

data class FilmEntity(
    val id: Long,
    val name: String,
    val producer: String,
    val actors: List<String>,
    val budget: Long,
    val filmUrl: String
)
