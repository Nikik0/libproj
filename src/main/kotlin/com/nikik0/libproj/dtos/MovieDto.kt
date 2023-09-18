package com.nikik0.libproj.dtos

data class MovieDto(
    val id: Long,
    val name: String,
    val producer: String,
    val actors: List<String>,
    val budget: Long,
    val movieUrl: String
)
