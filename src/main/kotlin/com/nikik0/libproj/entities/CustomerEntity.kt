package com.nikik0.libproj.entities

import com.nikik0.libproj.dtos.CustomerDto
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

fun CustomerEntity.toDto(): CustomerDto {
    return CustomerDto(
        id = this.id,
        name = this.name,
        surname = this.surname,
        country = this.address?.country,
        state = this.address?.state,
        city = this.address?.city,
        district = this.address?.district,
        street = this.address?.street,
        building = this.address?.building,
        buildingLiteral = this.address?.buildingLiteral,
        apartmentNumber = this.address?.apartmentNumber,
        additionalInfo = this.address?.additionalInfo
    )
}

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
    val favorites: List<MovieEntity>
) {
    @PersistenceCreator
    constructor(
        id: Long,
        name: String,
        surname: String
    ) : this(id, name, surname, null, emptyList(), emptyList())
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