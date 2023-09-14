package com.nikik0.libproj.mappers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity
import org.springframework.stereotype.Component
import reactor.util.function.Tuple2

fun CustomerDto.mapToCustomerEntityAndAddress(): Pair<CustomerEntity, AddressEntity> {
    val address = AddressEntity(
        id = this.id,
        country = this.country,
        state = this.state,
        city = this.city,
        district = this.district,
        street = this.street,
        building = this.building,
        buildingLiteral = this.buildingLiteral,
        apartmentNumber = this.apartmentNumber,
        additionalInfo = this.additionalInfo
    )
    val customerEntity = CustomerEntity(
        id = this.id,
        name = this.name,
        surname = this.surname,
        address = address
    )
    return Pair(customerEntity, address)
}

fun CustomerDto.mapToAddress() =
    AddressEntity(
        id = this.id,
        country = this.country,
        state = this.state,
        city = this.city,
        district = this.district,
        street = this.street,
        building = this.building,
        buildingLiteral = this.buildingLiteral,
        apartmentNumber = this.apartmentNumber,
        additionalInfo = this.additionalInfo
    )

fun CustomerEntity.toDto() =
    CustomerDto(
        id = this.id,
        name = this.name,
        surname = this.surname,
        country = this.address.country,
        state = this.address.state,
        city = this.address.city,
        district = this.address.district,
        street = this.address.street,
        building = this.address.building,
        buildingLiteral = this.address.buildingLiteral,
        apartmentNumber = this.address.apartmentNumber,
        additionalInfo = this.address.additionalInfo
    )

class CustomerMapper {
}