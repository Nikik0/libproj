package com.nikik0.libproj.entities

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("customer")
data class CustomerEntity(
    @Id
    val id: Long,
    val name: String,
    val surname:String,
    @Transient
    var address: AddressEntity?,
    @Transient
    val watched: List<MovieEntity>,
    @Transient
    var favorites: List<MovieEntity> = emptyList()
) {
    @PersistenceCreator
    constructor(
        id: Long,
        name: String,
        surname: String
    ) : this(id, name, surname, null, emptyList())
}

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