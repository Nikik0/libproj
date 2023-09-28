package com.nikik0.libproj.mappers

import com.nikik0.libproj.dtos.CustomerDto
import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity
import com.nikik0.libproj.repositories.AddressRepository
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import reactor.util.function.Tuple2

fun CustomerDto.mapToCustomerEntityAndAddress(): Pair<CustomerEntity, AddressEntity> {
    val address = AddressEntity(            //todo refactor for null checks
        id = this.id,
        country = this.country!!,
        state = this.state!!,
        city = this.city!!,
        district = this.district!!,
        street = this.street!!,
        building = this.building!!,
        buildingLiteral = this.buildingLiteral!!,
        apartmentNumber = this.apartmentNumber!!,
        additionalInfo = this.additionalInfo!!
    )
    val customerEntity = CustomerEntity(
       id = this.id,
        name = this.name,
        surname = this.surname
    )
    return Pair(customerEntity, address)
}

fun CustomerDto.mapToAddress() = //todo refactor for null checks
    AddressEntity(
        id = this.id,
        country = this.country!!,
        state = this.state!!,
        city = this.city!!,
        district = this.district!!,
        street = this.street!!,
        building = this.building!!,
        buildingLiteral = this.buildingLiteral!!,
        apartmentNumber = this.apartmentNumber!!,
        additionalInfo = this.additionalInfo!!
    )

suspend fun CustomerEntity.toDto(): CustomerDto {
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

class CustomerMapper {
}