package com.nikik0.libproj.dtos

import com.nikik0.libproj.entities.AddressEntity
import com.nikik0.libproj.entities.CustomerEntity

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

data class CustomerDto(
    val id: Long,
    val name: String,
    val surname:String,
    val country: String?,
    val state: String?,
    val city: String?,
    val district: String?,
    val street: String?,
    val building: Int?,
    val buildingLiteral: String?,
    val apartmentNumber: Int?,
    val additionalInfo: String?,
    val watched: List<MovieDto>?,
    val favourites: List<MovieDto>?
)
