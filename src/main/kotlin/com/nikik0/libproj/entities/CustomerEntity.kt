package com.nikik0.libproj.entities

import org.springframework.data.relational.core.mapping.Table

@Table("customer")
data class CustomerEntity(
    val id: Long,
    val name: String,
    val surname:String,
    val address: AddressEntity,
    val watched: List<MovieEntity>,
    val favorites: List<MovieEntity>
)

/*


create table customer(
    id serial primary key,
    name varchar(100),
    surname varchar(100),
    address
                         val id: Long,
                         val name: String,
                         val surname:String,
                         val address: AddressEntity,
                         val watched: List<MovieEntity>,
                         val favorites: List<MovieEntity>
)
 */