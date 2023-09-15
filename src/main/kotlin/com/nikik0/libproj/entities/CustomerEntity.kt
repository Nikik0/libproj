package com.nikik0.libproj.entities

import java.util.Objects

data class CustomerEntity(
    val id: Long,
    val name: String,
    val surname:String,
    val address: AddressEntity,
    val watched: List<FilmEntity>,
    val favorites: List<FilmEntity>
)
