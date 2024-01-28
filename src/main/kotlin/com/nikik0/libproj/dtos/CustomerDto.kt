package com.nikik0.libproj.dtos

import com.nikik0.libproj.entities.AddressEntity
import jakarta.validation.constraints.NotBlank

fun CustomerDto.mapToAddress(addressId: Long?) =
    AddressEntity(
        id = addressId,
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

data class CustomerDto(
    val id: Long,
    @field:NotBlank(message = "{validation.customer.field.name.blank}")
    val name: String,
    @field:NotBlank(message = "{validation.customer.field.surname.blank}")
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
